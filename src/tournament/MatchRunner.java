package tournament;

import java.util.*;
import javax.swing.*;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import gui.PhysicalGameStatePanel;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;

public class MatchRunner {
    private final TournamentConfig config;
    private final String map;
    private final MatchResultAnalyzer analyzer;

    public MatchRunner(TournamentConfig config, String map, MatchResultAnalyzer analyzer) {
        this.config = config;
        this.map = map;
        this.analyzer = analyzer;
    }

    public void runMatches(int p1Index, int p2Index, List<AI> players, TournamentResults results) {
        String p1Name = players.get(p1Index).getClass().getSimpleName();
        String p2Name = players.get(p2Index).getClass().getSimpleName();
        MatchStats stats = new MatchStats(p1Name, p2Name);

        long start = System.currentTimeMillis();

        for (int sim = 0; sim < config.getSimulations(); sim++) {
            if (System.currentTimeMillis() - start > config.getMaxDuration()) {
                break;
            }
            simulateMatch(p1Name, p2Name, p1Index, p2Index, stats, results, false, players);
            if (config.isCheckAdvantage()) {
                simulateMatch(p2Name, p1Name, p2Index, p1Index, stats, results, true, players);
            }
        }

        stats.setDuration(System.currentTimeMillis() - start);
        results.addMatchStats(stats);

        analyzer.printMatchSummary(stats, map);
    }

    private void simulateMatch(String p1Name, String p2Name, int p1Index, int p2Index,
            MatchStats stats, TournamentResults results, boolean switched, List<AI> players) {
        GameState gs = setupGame();
        JFrame window = config.isVisible() ? setupVisualizer(gs) : null;
        AI p1 = players.get(p1Index);
        AI p2 = players.get(p2Index);

        int winner = runMatch(gs, p1, p2, window);
        stats.addResult(winner, switched);
        results.addResult(map, p1Name, p2Name, winner);

        if (window != null && config.isDisposeWindow())
            window.dispose();

        stats.addCycles(gs.getTime());
    }

    private int runMatch(GameState gs, AI ai1, AI ai2, JFrame window) {
        int winner = -1;
        try {
            long nextUpdateTime = System.currentTimeMillis() + (config.isVisible() ? config.getUpdatePeriod() : 0);
            while (!gs.gameover() && gs.getTime() < config.getMaxCycles()) {
                if (System.currentTimeMillis() >= nextUpdateTime) {
                    performGameCycle(gs, ai1, ai2, window);
                    nextUpdateTime += (config.isVisible() ? config.getUpdatePeriod() : 0);
                } else {
                    Thread.sleep(1);
                }
            }
            winner = gs.winner();

            ai1.reset();
            ai2.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return winner;
    }

    private void performGameCycle(GameState gs, AI ai1, AI ai2, JFrame window) throws Exception {
        if (ai1 instanceof AIWithComputationBudget) {
            ((AIWithComputationBudget) ai1).setTimeBudget(config.getTimeBudget());
        }
        if (ai2 instanceof AIWithComputationBudget) {
            ((AIWithComputationBudget) ai2).setTimeBudget(config.getTimeBudget());
        }

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
            PhysicalGameState pgs = PhysicalGameState.load(map, config.getUtt());
            return new GameState(pgs, config.getUtt());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private JFrame setupVisualizer(GameState gs) {
        JFrame window = PhysicalGameStatePanel.newVisualizer(gs, config.getWindowSize(), config.getWindowSize(), false);
        window.setVisible(true);
        window.setLocationRelativeTo(null);
        return window;
    }
}