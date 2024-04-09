package tournament;

import java.util.*;
import ai.core.AI;

public class TournamentRunner {
    public static void runTournament(TournamentConfig config) {
        List<AI> players;
        try {
            players = Tournament.getBots(config);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (players.size() < 2) {
            System.out.println("There must be at least two players to run a tournament.");
            return;
        }

        TournamentResults results = new TournamentResults();
        MatchResultAnalyzer analyzer = new MatchResultAnalyzer(results, config, System.currentTimeMillis());

        for (String map : config.getMaps()) {
            MatchRunner match = new MatchRunner(config, map, analyzer);
            if (config.getType() == TournamentType.ROUND_ROBIN) {
                runRoundRobin(match, players, results, map);
            } else if (config.getType() == TournamentType.BRACKET) {
                runBracket(match, players, results, map);
            }
        }
        analyzer.printSummary();
    }

    private static void runRoundRobin(MatchRunner match, List<AI> players, TournamentResults results, String map) {
        for (int i = 0; i < players.size(); i++) {
            for (int j = i + 1; j < players.size(); j++) {
                match.runMatches(i, j, players, results);
            }
        }
    }

    private static void runBracket(MatchRunner match, List<AI> players, TournamentResults results, String map) {
        List<AI> remaining = new ArrayList<>(players);
        int round = 1;

        while (remaining.size() > 1) {
            List<AI> winners = new ArrayList<>();

            System.out.println("Round " + round + ":");

            for (int i = 0; i < remaining.size(); i += 2) {
                AI p1 = remaining.get(i);
                AI p2 = (i + 1 < remaining.size()) ? remaining.get(i + 1) : null;

                if (p2 != null) {
                    int p1Index = players.indexOf(p1);
                    int p2Index = players.indexOf(p2);

                    match.runMatches(p1Index, p2Index, players, results);

                    String p1Name = p1.getClass().getSimpleName();
                    String p2Name = p2.getClass().getSimpleName();
                    int winner = results.determineWinner(map, p1Name, p2Name);

                    if (winner == 0) {
                        winners.add(p1);
                        System.out.println(p1Name + " wins against " + p2Name);
                    } else if (winner == 1) {
                        winners.add(p2);
                        System.out.println(p2Name + " wins against " + p1Name);
                    } else {
                        winners.add(p1);
                        System.out.println(p1Name + " and " + p2Name + " draw");
                    }
                } else {
                    winners.add(p1);
                    System.out.println(p1.getClass().getSimpleName() + " gets a bye");
                }
            }

            remaining = winners;
            round++;
            System.out.println();
        }

        System.out.println("Tournament winner: " + remaining.get(0).getClass().getSimpleName());
    }
}