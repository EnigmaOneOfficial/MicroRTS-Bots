package tournament;

import java.util.*;

import javax.swing.*;

import ai.core.AI;
import gui.PhysicalGameStatePanel;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

public class MatchRunner {
    private final UnitTypeTable utt;
    private final int maxCycles;
    private final int updatePeriod;
    private final int windowSize;
    private final boolean disposeWindow;
    private final boolean checkForAdvantage;
    private final int maxDurationPerMatchup;
    private final boolean visualize;
    private final int simulations;
    private final String mapPath;

    public MatchRunner(UnitTypeTable utt, int maxCycles, int updatePeriod, int windowSize, boolean disposeWindow,
            boolean checkForAdvantage, int maxDurationPerMatchup, boolean visualize, int simulations, String mapPath) {
        this.utt = utt;
        this.maxCycles = maxCycles;
        this.updatePeriod = updatePeriod;
        this.windowSize = windowSize;
        this.disposeWindow = disposeWindow;
        this.checkForAdvantage = checkForAdvantage;
        this.maxDurationPerMatchup = maxDurationPerMatchup;
        this.visualize = visualize;
        this.simulations = simulations;
        this.mapPath = mapPath;
    }

    public void runMatches(int playerIndex1, int playerIndex2, List<AI> players,
            Map<String, Map<String, List<MatchResult>>> tournamentResults,
            Map<String, Map<String, List<MatchResult>>> tournamentResultsSwitched,
            List<Integer> gameCycles, Map<String, Long> matchDurations) {
        String ai1Name = players.get(playerIndex1).getClass().getSimpleName();
        String ai2Name = players.get(playerIndex2).getClass().getSimpleName();
        List<Integer> winners = new ArrayList<>();
        List<Integer> winnersSwitched = new ArrayList<>();

        long start = System.currentTimeMillis();

        for (int simulation = 0; simulation < simulations; simulation++) {
            if (System.currentTimeMillis() - start > maxDurationPerMatchup) {
                break;
            }
            simulateMatch(ai1Name, ai2Name, playerIndex1, playerIndex2, winners, gameCycles, tournamentResults, false,
                    players);
            if (checkForAdvantage) {
                simulateMatch(ai2Name, ai1Name, playerIndex2, playerIndex1, winnersSwitched, gameCycles,
                        tournamentResultsSwitched, true, players);
            }
        }

        long duration = System.currentTimeMillis() - start;
        matchDurations.put(ai1Name + " vs " + ai2Name, duration);
        MatchResultAnalyzer analyzer = new MatchResultAnalyzer(tournamentResults, matchDurations,
                tournamentResultsSwitched, mapPath);
        analyzer.summarizeMatchup(new MatchStats(winners, winnersSwitched, gameCycles, duration, ai1Name, ai2Name));
    }

    private void simulateMatch(String ai1Name, String ai2Name, int playerIndex1, int playerIndex2,
            List<Integer> winners, List<Integer> gameCycles,
            Map<String, Map<String, List<MatchResult>>> resultsStorage, boolean switched,
            List<AI> players) {
        GameState gs = setupGame();
        JFrame window = visualize ? setupVisualizer(gs) : null;
        AI player1 = players.get(playerIndex1);
        AI player2 = players.get(playerIndex2);

        int winner = runMatch(gs, player1, player2, gameCycles, window);
        winners.add(winner);
        recordResult(resultsStorage, ai1Name, ai2Name, winner);

        if (window != null && disposeWindow)
            window.dispose();
    }

    private int runMatch(GameState gs, AI ai1, AI ai2, List<Integer> gameCycles, JFrame window) {
        int winner = -1;
        try {
            long nextUpdateTime = System.currentTimeMillis() + (visualize ? updatePeriod : 0);
            while (!gs.gameover() && gs.getTime() < maxCycles) {
                if (System.currentTimeMillis() >= nextUpdateTime) {
                    performGameCycle(gs, ai1, ai2, window);
                    nextUpdateTime += (visualize ? updatePeriod : 0);
                } else {
                    Thread.sleep(1);
                }
            }
            winner = gs.winner();
            gameCycles.add(gs.getTime());

            ai1.reset();
            ai2.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return winner;
    }

    private void performGameCycle(GameState gs, AI ai1, AI ai2, JFrame window) throws Exception {
        PlayerAction pa1 = ai1.getAction(0, gs);
        PlayerAction pa2 = ai2.getAction(1, gs);
        gs.issueSafe(pa1);
        gs.issueSafe(pa2);
        gs.cycle();
        if (window != null)
            window.repaint();
    }

    private GameState setupGame() {
        try {
            PhysicalGameState pgs = PhysicalGameState.load(mapPath, utt);
            return new GameState(pgs, utt);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private JFrame setupVisualizer(GameState gs) {
        JFrame window = PhysicalGameStatePanel.newVisualizer(gs, windowSize, windowSize, false);
        window.setVisible(true);
        window.setLocationRelativeTo(null);
        return window;
    }

    private void recordResult(Map<String, Map<String, List<MatchResult>>> results, String ai1, String ai2,
            int winner) {
        MatchResult result = winner == 0 ? MatchResult.WIN : winner == 1 ? MatchResult.LOSE : MatchResult.DRAW;
        results.computeIfAbsent(ai1, k -> new HashMap<>()).computeIfAbsent(ai2, k -> new ArrayList<>()).add(result);
        MatchResult oppositeResult = (result == MatchResult.WIN) ? MatchResult.LOSE
                : (result == MatchResult.LOSE ? MatchResult.WIN : result);
        results.computeIfAbsent(ai2, k -> new HashMap<>()).computeIfAbsent(ai1, k -> new ArrayList<>())
                .add(oppositeResult);
    }
}