package tournament;

import java.util.*;

import ai.core.AI;
import rts.units.UnitTypeTable;

public class TournamentRunner {
    private static final String MAP_PATH = "maps/16x16/basesWorkers16x16.xml";
    private static final int MAX_CYCLES = 5000;
    private static final int UPDATE_PERIOD = 10;
    private static final int WINDOW_SIZE = 1000;
    private static final boolean DISPOSE_WINDOW = true;
    private static final boolean CHECK_FOR_ADVANTAGE = true;
    private static final int MAX_DURATION_PER_MATCHUP = 30000;
    private static final boolean VISUALIZE = false;
    private static final int SIMULATIONS = 10;

    private static List<AI> players = new ArrayList<>();
    private static UnitTypeTable utt = new UnitTypeTable();

    public static void runTournament() {
        try {
            Tournament.createPlayers(players, utt);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (players.size() < 2) {
            System.out.println("There must be at least two players to run a tournament.");
            return;
        }

        Map<String, Map<String, List<MatchResult>>> tournamentResults = new HashMap<>();
        Map<String, Map<String, List<MatchResult>>> tournamentResultsSwitched = new HashMap<>();
        List<Integer> gameCycles = new ArrayList<>();
        Map<String, Long> matchDurations = new HashMap<>();

        MatchRunner match = new MatchRunner(utt, MAX_CYCLES, UPDATE_PERIOD, WINDOW_SIZE, DISPOSE_WINDOW,
                CHECK_FOR_ADVANTAGE, MAX_DURATION_PER_MATCHUP, VISUALIZE, SIMULATIONS, MAP_PATH);

        for (int i = 0; i < players.size(); i++) {
            for (int j = i + 1; j < players.size(); j++) {
                match.runMatches(i, j, players, tournamentResults, tournamentResultsSwitched, gameCycles,
                        matchDurations);
            }
        }

        MatchResultAnalyzer analyzer = new MatchResultAnalyzer(tournamentResults, matchDurations,
                tournamentResultsSwitched, MAP_PATH);
        analyzer.summarizeTournament();
        analyzer.exportTournamentResults();
    }

}