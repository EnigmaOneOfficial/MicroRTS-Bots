package tournament;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MatchResultAnalyzer {
    private final Map<String, Map<String, List<MatchResult>>> tournamentResults;
    private final Map<String, Long> matchDurations;
    private final Map<String, Map<String, List<MatchResult>>> tournamentResultsSwitched;
    private final String mapPath;
    private static final String RESULTS_DIRECTORY = "tournament_results";

    public MatchResultAnalyzer(Map<String, Map<String, List<MatchResult>>> tournamentResults,
            Map<String, Long> matchDurations,
            Map<String, Map<String, List<MatchResult>>> tournamentResultsSwitched,
            String mapPath) {
        this.tournamentResults = tournamentResults;
        this.matchDurations = matchDurations;
        this.tournamentResultsSwitched = tournamentResultsSwitched;
        this.mapPath = mapPath;
    }

    public void exportTournamentResults() {
        String tournamentDirectory = createTournamentDirectory();
        exportMatchupResults(tournamentDirectory);
        exportTournamentSummary(tournamentDirectory);
    }

    private String createTournamentDirectory() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String tournamentDirectory = String.format("%s/tournament_%s", RESULTS_DIRECTORY, timestamp);
        File directory = new File(tournamentDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return tournamentDirectory;
    }

    private void exportMatchupResults(String tournamentDirectory) {
        String matchupsFile = String.format("%s/matchups_summary.csv", tournamentDirectory);
        try (FileWriter writer = new FileWriter(matchupsFile)) {
            writer.write("Player 1,Player 2,Wins,Losses,Draws\n");
            for (Map.Entry<String, Map<String, List<MatchResult>>> entry : tournamentResults.entrySet()) {
                String player1 = entry.getKey();
                Map<String, List<MatchResult>> opponentsResults = entry.getValue();
                for (Map.Entry<String, List<MatchResult>> opponentEntry : opponentsResults.entrySet()) {
                    String player2 = opponentEntry.getKey();
                    List<MatchResult> results = opponentEntry.getValue();
                    long wins = results.stream().filter(r -> r == MatchResult.WIN).count();
                    long losses = results.stream().filter(r -> r == MatchResult.LOSE).count();
                    long draws = results.stream().filter(r -> r == MatchResult.DRAW).count();

                    writer.write(String.format("%s,%s,%d,%d,%d\n", player1, player2, wins, losses, draws));

                    String matchupsDirectory = String.format("%s/matchups", tournamentDirectory);
                    exportMatchupMatchData(matchupsDirectory, player1, player2, results);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportMatchupMatchData(String matchupsDirectory, String player1, String player2,
            List<MatchResult> results) {
        File directory = new File(matchupsDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String matchDataFile = String.format("%s/%s_vs_%s.csv", matchupsDirectory, player1, player2);
        try (FileWriter writer = new FileWriter(matchDataFile)) {
            writer.write("Match,Result\n");
            for (int i = 0; i < results.size(); i++) {
                String result = getResultString(results.get(i));
                writer.write(String.format("%d,%s\n", i + 1, result));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getResultString(MatchResult result) {
        switch (result) {
            case WIN:
                return "Win";
            case LOSE:
                return "Loss";
            case DRAW:
                return "Draw";
            default:
                return "";
        }
    }

    private void exportTournamentSummary(String tournamentDirectory) {
        String summaryFile = String.format("%s/tournament_summary.csv", tournamentDirectory);
        try (FileWriter writer = new FileWriter(summaryFile)) {
            writer.write("Bot,Wins,Losses,Draws,Win Rate (%)\n");
            Map<String, Map<String, List<MatchResult>>> combinedResults = combineAndAggregateResults(tournamentResults,
                    tournamentResultsSwitched);
            Map<String, Double> rankings = calculateRankings(combinedResults);
            for (Map.Entry<String, Double> entry : rankings.entrySet()) {
                String botName = entry.getKey();
                double winPercentage = entry.getValue();
                int wins = combinedResults.get(botName).values().stream()
                        .flatMap(List::stream)
                        .mapToInt(result -> result == MatchResult.WIN ? 1 : 0)
                        .sum();
                int losses = combinedResults.get(botName).values().stream()
                        .flatMap(List::stream)
                        .mapToInt(result -> result == MatchResult.LOSE ? 1 : 0)
                        .sum();
                int draws = combinedResults.get(botName).values().stream()
                        .flatMap(List::stream)
                        .mapToInt(result -> result == MatchResult.DRAW ? 1 : 0)
                        .sum();
                writer.write(String.format("%s,%d,%d,%d,%.2f\n", botName, wins, losses, draws, winPercentage));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void summarizeTournament() {
        System.out.printf("\nTournament Results (%s)\n", mapPath);
        System.out.println("================================================");

        Map<String, Map<String, List<MatchResult>>> combinedResults = combineAndAggregateResults(tournamentResults,
                tournamentResultsSwitched);

        Map<String, Double> rankings = calculateRankings(combinedResults);
        displayRankings(rankings, combinedResults);
        summarizeTournamentResults(combinedResults, matchDurations);
    }

    public void summarizeMatchup(MatchStats matchStats) {
        String player1 = matchStats.getPlayer1();
        String player2 = matchStats.getPlayer2();
        List<Integer> winners = matchStats.getWinners();
        List<Integer> winnersSwitched = matchStats.getWinnersSwitched();
        List<Integer> cycles = matchStats.getCycles();
        long duration = matchStats.getDuration();

        System.out.println(player1 + " vs " + player2);
        System.out.println("================================================");
        summarizeResults(winners, winnersSwitched, player1, player2);
        System.out.printf("\t%-25s %dms%n", "Matchup Duration:", duration);
        double averageCycles = cycles.stream().mapToInt(Integer::intValue).average().orElse(0);
        int minCycles = cycles.stream().mapToInt(Integer::intValue).min().orElse(0);
        int maxCycles = cycles.stream().mapToInt(Integer::intValue).max().orElse(0);
        System.out.printf("\t%-25s %.2f%n", "Average game cycles:", averageCycles);
        System.out.printf("\t%-25s %d%n", "Minimum game cycles:", minCycles);
        System.out.printf("\t%-25s %d%n", "Maximum game cycles:", maxCycles);
        System.out.println("================================================\n");
    }

    private void summarizeResults(List<Integer> winnersList, List<Integer> winnersListSwitched, String ai1,
            String ai2) {
        if (winnersListSwitched.size() > 0) {
            summarizeForRole(winnersList, ai1, ai2, ai1 + " as P1");
            summarizeForRole(winnersListSwitched, ai2, ai1, ai2 + " as P1");
            List<Integer> combinedResults = new ArrayList<>(winnersList);
            combinedResults.addAll(winnersListSwitched.stream().map(winner -> {
                return (winner == 1) ? 0 : (winner == 0) ? 1 : 2;
            }).collect(Collectors.toList()));

            summarizeForRole(combinedResults, ai1, ai2, "Combined Results (" + ai1 + " vs " + ai2 + ")");
        } else {
            summarizeForRole(winnersList, ai1, ai2, ai1 + " as P1");
        }
    }

    private void summarizeForRole(List<Integer> winnersList, String ai1, String ai2, String description) {
        int winsAI1 = 0, winsAI2 = 0, draws = 0;
        for (int winner : winnersList) {
            if (winner == 0)
                winsAI1++;
            else if (winner == 1)
                winsAI2++;
            else
                draws++;
        }

        double totalGames = winnersList.size();
        double winRateAI1 = (winsAI1 / totalGames) * 100;
        double winRateAI2 = (winsAI2 / totalGames) * 100;
        double drawRate = (draws / totalGames) * 100;

        System.out.println(description);
        System.out.println("------------------------------------------------");
        System.out.printf("\t%-25s %d%n", "Total simulations:", winnersList.size());
        System.out.printf("\t%-25s %d (%.2f%%)%n", ai1 + " wins:", winsAI1, winRateAI1);
        System.out.printf("\t%-25s %d (%.2f%%)%n", ai2 + " wins:", winsAI2, winRateAI2);
        System.out.printf("\t%-25s %d (%.2f%%)%n", "Draws:", draws, drawRate);
        System.out.println("------------------------------------------------");
    }

    private Map<String, Map<String, List<MatchResult>>> combineAndAggregateResults(
            Map<String, Map<String, List<MatchResult>>> original,
            Map<String, Map<String, List<MatchResult>>> switched) {
        Map<String, Map<String, List<MatchResult>>> combined = new HashMap<>();

        original.forEach((player, opponents) -> combined.put(player, new HashMap<>(opponents)));
        switched.forEach((player, opponents) -> {
            opponents.forEach((opponent, results) -> combined.computeIfAbsent(player, k -> new HashMap<>())
                    .merge(opponent, results, (oldResults, newResults) -> {
                        List<MatchResult> mergedList = new ArrayList<>(oldResults);
                        mergedList.addAll(newResults);
                        return mergedList;
                    }));
        });

        return combined;
    }

    private Map<String, Double> calculateRankings(Map<String, Map<String, List<MatchResult>>> combinedResults) {
        Map<String, Double> winPercentages = new HashMap<>();

        combinedResults.forEach((bot, opponentsResults) -> {
            int wins = opponentsResults.values().stream()
                    .flatMap(List::stream)
                    .mapToInt(result -> result == MatchResult.WIN ? 1 : 0)
                    .sum();
            int totalMatches = opponentsResults.values().stream()
                    .flatMap(List::stream)
                    .mapToInt(result -> 1) // Count every result as a match
                    .sum();

            double winPercentage = totalMatches > 0 ? (double) wins / totalMatches * 100 : 0;
            winPercentages.put(bot, winPercentage);
        });

        return winPercentages.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
    }

    private void displayRankings(Map<String, Double> rankings,
            Map<String, Map<String, List<MatchResult>>> combinedResults) {
        int currentRank = 1;
        int botsProcessed = 0;
        double previousWinPercentage = -1.0;

        for (var entry : rankings.entrySet()) {
            if (entry.getValue() != previousWinPercentage) {
                currentRank = botsProcessed + 1;
                previousWinPercentage = entry.getValue();
            }

            String botName = entry.getKey();
            double winPercentage = entry.getValue();
            int wins = combinedResults.get(botName).values().stream()
                    .flatMap(List::stream)
                    .mapToInt(result -> result == MatchResult.WIN ? 1 : 0)
                    .sum();
            int losses = combinedResults.get(botName).values().stream()
                    .flatMap(List::stream)
                    .mapToInt(result -> result == MatchResult.LOSE ? 1 : 0)
                    .sum();
            int draws = combinedResults.get(botName).values().stream()
                    .flatMap(List::stream)
                    .mapToInt(result -> result == MatchResult.DRAW ? 1 : 0)
                    .sum();

            System.out.printf("\tRank %d: %-20s %d Wins | %d Losses | %d Draws | %.2f%% Win Rate\n", currentRank,
                    botName, wins, losses, draws, winPercentage);

            botsProcessed++;
        }
        System.out.println("------------------------------------------------");
    }

    private void summarizeTournamentResults(Map<String, Map<String, List<MatchResult>>> combinedResults,
            Map<String, Long> matchDurations) {
        combinedResults.forEach((ai1, opponents) -> {
            final boolean[] isFirstOpponent = { true };

            opponents.forEach((ai2, results) -> {
                long wins = results.stream().filter(r -> r == MatchResult.WIN).count();
                long losses = results.stream().filter(r -> r == MatchResult.LOSE).count();
                long draws = results.stream().filter(r -> r == MatchResult.DRAW).count();

                if (isFirstOpponent[0]) {
                    System.out.printf("\t%-20s vs %-20s %d Wins | %d Losses | %d Draws\n", ai1, ai2, wins, losses,
                            draws);
                    isFirstOpponent[0] = false;
                } else {
                    System.out.printf("\t\t\t\t%-20s %d Wins | %d Losses | %d Draws\n", ai2,
                            wins, losses, draws);
                }
            });
            System.out.println("------------------------------------------------");
        });

        long totalDuration = matchDurations.values().stream().mapToLong(Long::longValue).sum();
        System.out.printf("\tTotal Tournament Duration: %dms\n", totalDuration);
        System.out.println("================================================");
    }
}