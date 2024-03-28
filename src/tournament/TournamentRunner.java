package tournament;

import java.util.*;

import ai.core.AI;
import rts.units.UnitTypeTable;

public class TournamentRunner {

    private static List<AI> players = new ArrayList<>();
    private static UnitTypeTable utt = new UnitTypeTable();

    public static void runTournament(String MAP_PATH, int MAX_CYCLES, int UPDATE_PERIOD, int WINDOW_SIZE,
            boolean DISPOSE_WINDOW, boolean CHECK_FOR_ADVANTAGE, int MAX_DURATION_PER_MATCHUP, boolean VISUALIZE,
            int SIMULATIONS) {
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
        // analyzer.exportTournamentResults();
    }

}