package tournament;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MatchResultAnalyzer {
    private final TournamentResults results;
    private final TournamentConfig config;
    private final long startTime;
    private final String tournamentId;

    public MatchResultAnalyzer(TournamentResults results, TournamentConfig config, long startTime) {
        this.results = results;
        this.config = config;
        this.startTime = startTime;
        this.tournamentId = generateTournamentId();
        createTournamentFolder();
    }

    private String generateTournamentId() {
        // Generate a unique tournament ID
        return "tournament_" + System.currentTimeMillis();
    }

    private void createTournamentFolder() {
        File tournamentFolder = new File(tournamentId);
        if (!tournamentFolder.exists()) {
            tournamentFolder.mkdir();
        }
    }

    public void printSummary() {
        System.out.println("Tournament Summary");
        System.out.println("==================");
        System.out.println();

        for (String map : results.getMaps()) {
            printMapResults(map);
        }

        printCombinedSummary();
    }

    private void printMapResults(String map) {
        System.out.println("Map: " + map);
        System.out.println("------------------");

        Map<String, Map<String, List<MatchResult>>> combinedResults = combineResults(map);
        Map<String, Double> rankings = calculateRankings(combinedResults);
        printRankings(rankings, combinedResults);
        System.out.println();

        // Parse the map name to remove the "/" and all the chars before the final "/"
        String parsedMapName = parseMapName(map);

        // Create a folder for the map
        File mapFolder = new File(tournamentId + File.separator + parsedMapName);
        if (!mapFolder.exists()) {
            mapFolder.mkdirs();
        }

        // Export matchup summaries for each player on the map
        exportMatchupSummaries(combinedResults, mapFolder);

        // Export map tournament results to a CSV
        exportMapResults(rankings, combinedResults, parsedMapName);
    }

    private String parseMapName(String map) {
        // Remove the "/" and all the chars before the final "/"
        int lastSlashIndex = map.lastIndexOf("/");
        if (lastSlashIndex != -1) {
            map = map.substring(lastSlashIndex + 1);
        }
        // Replace any remaining "/" with "_"
        map = map.replace("/", "_");
        return map;
    }

    // ... (rest of the code remains the same)

    private void exportMapResults(Map<String, Double> rankings,
            Map<String, Map<String, List<MatchResult>>> combinedResults, String map) {
        String fileName = map + "_Results.csv";
        File file = new File(tournamentId, fileName);
        // Create parent directories if they don't exist
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Name,Win Rate,Wins,Losses,Draws\n");
            rankings.forEach((bot, winRate) -> {
                int wins = countWins(combinedResults.get(bot));
                int losses = countLosses(combinedResults.get(bot));
                int draws = countDraws(combinedResults.get(bot));
                try {
                    writer.write(bot + "," + String.format("%.2f%%", winRate) + "," + wins + "," + losses + ","
                            + draws + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportMatchupSummaries(Map<String, Map<String, List<MatchResult>>> combinedResults, File mapFolder) {
        combinedResults.forEach((player1, opponentResults) -> opponentResults.forEach((player2, matchResults) -> {
            String fileName = player1 + "_vs_" + player2 + ".csv";
            File file = new File(mapFolder, fileName);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Player1,Player2,Result\n");
                matchResults.forEach(result -> {
                    try {
                        writer.write(player1 + "," + player2 + "," + result + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    private Map<String, Map<String, List<MatchResult>>> combineResults(String map) {
        Map<String, Map<String, List<MatchResult>>> combinedResults = new HashMap<>();

        mergeResults(combinedResults, results.getResults(map));
        mergeResults(combinedResults, results.getResultsSwitched(map));

        return combinedResults;
    }

    private void mergeResults(Map<String, Map<String, List<MatchResult>>> combinedResults,
            Map<String, Map<String, List<MatchResult>>> resultsToMerge) {
        resultsToMerge.forEach((p1, opponents) -> opponents
                .forEach((p2, matchResults) -> combinedResults.computeIfAbsent(p1, k -> new HashMap<>())
                        .merge(p2, matchResults, (old, added) -> {
                            List<MatchResult> merged = new ArrayList<>(old);
                            merged.addAll(added);
                            return merged;
                        })));
    }

    private Map<String, Double> calculateRankings(Map<String, Map<String, List<MatchResult>>> combinedResults) {
        Map<String, Double> winRates = new HashMap<>();

        combinedResults.forEach((bot, opponentResults) -> {
            int wins = countWins(opponentResults);
            int matches = countMatches(opponentResults);
            double winRate = calculateWinRate(wins, matches);
            winRates.put(bot, winRate);
        });

        return sortByValue(winRates);
    }

    private int countWins(Map<String, List<MatchResult>> opponentResults) {
        return opponentResults.values().stream()
                .mapToInt(results -> (int) results.stream().filter(r -> r == MatchResult.WIN).count())
                .sum();
    }

    private int countLosses(Map<String, List<MatchResult>> opponentResults) {
        return opponentResults.values().stream()
                .mapToInt(results -> (int) results.stream().filter(r -> r == MatchResult.LOSE).count())
                .sum();
    }

    private int countDraws(Map<String, List<MatchResult>> opponentResults) {
        return opponentResults.values().stream()
                .mapToInt(results -> (int) results.stream().filter(r -> r == MatchResult.DRAW).count())
                .sum();
    }

    private int countMatches(Map<String, List<MatchResult>> opponentResults) {
        return opponentResults.values().stream().mapToInt(List::size).sum();
    }

    private double calculateWinRate(int wins, int matches) {
        return matches > 0 ? (double) wins / matches * 100 : 0;
    }

    private <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<K, V>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
    }

    private void printRankings(Map<String, Double> rankings,
            Map<String, Map<String, List<MatchResult>>> combinedResults) {
        System.out.println("Rankings");
        System.out.println("--------");
        System.out.printf("%-20s %-10s %-10s %-10s %-10s%n", "Name", "Win Rate", "Wins", "Losses", "Draws");
        System.out.println("--------------------------------------------------------------");

        rankings.forEach((bot, winRate) -> {
            int wins = countWins(combinedResults.get(bot));
            int losses = countLosses(combinedResults.get(bot));
            int draws = countDraws(combinedResults.get(bot));

            System.out.printf("%-20s %6.2f%%   %5d %8d %8d%n", bot, winRate, wins, losses, draws);
        });
    }

    private void printCombinedSummary() {
        System.out.println("Tournament Results");
        System.out.println("------------------");

        Map<String, Map<String, List<MatchResult>>> combinedResults = new HashMap<>();
        for (String map : results.getMaps()) {
            mergeResults(combinedResults, combineResults(map));
        }

        int totalMatches = combinedResults.values().stream()
                .flatMap(opponentResults -> opponentResults.values().stream())
                .mapToInt(List::size)
                .sum();

        System.out.println("Type: " + config.getType().toString().toLowerCase());
        System.out.println("Maps (%d): %s".formatted(results.getMaps().size(), String.join(", ", results.getMaps())));
        System.out.println("Bots: " + combinedResults.size());
        System.out.println("Matches: " + totalMatches);
        printDuration("Tournament");
        System.out.println();

        Map<String, Double> rankings = calculateRankings(combinedResults);
        printRankings(rankings, combinedResults);
        System.out.println();

        // Export the final combined tournament summary to a CSV
        exportFinalResults(rankings, combinedResults);
    }

    private void exportFinalResults(Map<String, Double> rankings,
            Map<String, Map<String, List<MatchResult>>> combinedResults) {
        String fileName = "Final_Results.csv";
        File file = new File(tournamentId, fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Name,Win Rate,Wins,Losses,Draws\n");
            rankings.forEach((bot, winRate) -> {
                int wins = countWins(combinedResults.get(bot));
                int losses = countLosses(combinedResults.get(bot));
                int draws = countDraws(combinedResults.get(bot));
                try {
                    writer.write(bot + "," + String.format("%.2f%%", winRate) + "," + wins + "," + losses + ","
                            + draws + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printDuration(String durationOf) {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.printf("%s Duration: %d ms%n", durationOf, duration);
    }

    public void printMatchSummary(MatchStats stats, String map) {
        String p1 = stats.getPlayer1();
        String p2 = stats.getPlayer2();
        List<Integer> results = stats.getResults();
        List<Integer> resultsSwitched = stats.getResultsSwitched();
        List<Integer> cycles = stats.getCycles();
        long duration = stats.getDuration();

        System.out.println("Match Summary");
        System.out.println("=============");
        System.out.printf("Players: %s vs %s%n", p1, p2);
        System.out.printf("Map: %s%n", map);
        printMatchStats(cycles, duration);
        System.out.println();
        printMatchResults(results, resultsSwitched, p1, p2);
        System.out.println();
    }

    private void printMatchResults(List<Integer> results, List<Integer> resultsSwitched, String p1, String p2) {
        int p1Wins = countWins(results, 0);
        int p2Wins = countWins(results, 1);
        int draws = countDraws(results);

        System.out.println("Results");
        System.out.println("-------");
        System.out.printf("%s Wins: %d%n", p1, p1Wins);
        System.out.printf("%s Wins: %d%n", p2, p2Wins);
        System.out.printf("Draws: %d%n", draws);
        System.out.println();

        if (config.isCheckAdvantage()) {
            int p1WinsSwitched = countWins(resultsSwitched, 0);
            int p2WinsSwitched = countWins(resultsSwitched, 1);
            int drawsSwitched = countDraws(resultsSwitched);

            System.out.println("Switched Results");
            System.out.println("----------------");
            System.out.printf("%s Wins: %d%n", p1, p2WinsSwitched);
            System.out.printf("%s Wins: %d%n", p2, p1WinsSwitched);
            System.out.printf("Draws: %d%n", drawsSwitched);
        }
    }

    private int countWins(List<Integer> results, int playerIndex) {
        return (int) results.stream().filter(r -> r == playerIndex).count();
    }

    private int countDraws(List<Integer> results) {
        return (int) results.stream().filter(r -> r == -1).count();
    }

    private void printMatchStats(List<Integer> cycles, long duration) {
        double avgCycles = cycles.stream().mapToInt(Integer::intValue).average().orElse(0);
        int minCycles = cycles.stream().mapToInt(Integer::intValue).min().orElse(0);
        int maxCycles = cycles.stream().mapToInt(Integer::intValue).max().orElse(0);

        System.out.printf("Avg Cycles: %.2f%n", avgCycles);
        System.out.printf("Min Cycles: %d%n", minCycles);
        System.out.printf("Max Cycles: %d%n", maxCycles);
        System.out.printf("Duration: %d ms%n", duration);
    }
}