package tournament;

import java.util.List;
import ai.core.AI;
import rts.units.UnitTypeTable;

public class Tournament {
    private static final String MAP_PATH = "maps/16x16/basesWorkers16x16.xml"; // "maps/BroodWar/(4)BloodBath.scmA.xml";
    private static final int MAX_CYCLES = 5000;
    private static final int UPDATE_PERIOD = 15;
    private static final int WINDOW_SIZE = 800;
    private static final boolean DISPOSE_WINDOW = true;
    private static final boolean CHECK_FOR_ADVANTAGE = true;
    private static final int MAX_DURATION_PER_MATCHUP = 999999;
    private static final boolean VISUALIZE = false;
    private static final int SIMULATIONS = 20;

    public static void createPlayers(List<AI> players, UnitTypeTable utt) throws Exception {
        // players.add(new PassiveAI(utt));
        // players.add(new RandomAI(utt));
        // players.add(new RandomBiasedAI(utt));
        // players.add(new HeavyRush(utt));
        // players.add(new LightDefense(utt));
        // players.add(new WorkerDefense(utt));
        // players.add(new WorkerRush(utt));
        // players.add(new sspringer_mcts_bot(utt));
        // players.add(new mayari(utt));
        // players.add(new BradyBot(utt));
        // players.add(new KalebBot(utt));
        // players.add(new BasicRush(utt));
        // players.add(new DinkleBot(utt));
        // players.add(new RangedRushImproved(utt));
        // players.add(new DameBot(utt));
        // players.add(new SwagBot(utt));
        // players.add(new LasyaBot(utt));
        // players.add(new BaluBot(utt));
        // players.add(new RitsBot(utt));
        // players.add(new nickBot(utt));
        // players.add(new MyBot(utt));
        // players.add(new RAISocketAI(utt));
        // players.add(new HSBot(utt));
    }

    public static void main(String[] args) {
        TournamentRunner.runTournament(MAP_PATH, MAX_CYCLES, UPDATE_PERIOD, WINDOW_SIZE, DISPOSE_WINDOW,
                CHECK_FOR_ADVANTAGE, MAX_DURATION_PER_MATCHUP, VISUALIZE, SIMULATIONS);
    }
}