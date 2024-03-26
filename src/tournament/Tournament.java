package tournament;

import java.util.List;

import DameBot.DameBot;
import ai.core.AI;
import mayariBot.mayari;
import rts.units.UnitTypeTable;

public class Tournament {
    public static void createPlayers(List<AI> players, UnitTypeTable utt) throws Exception {
        // players.add(new PassiveAI(utt));
        // players.add(new RandomAI(utt));
        // players.add(new RandomBiasedAI(utt));
        // players.add(new HeavyRush(utt));
        // players.add(new LightDefense(utt));
        // players.add(new WorkerDefense(utt));
        // players.add(new WorkerRush(utt));
        players.add(new DameBot(utt));
        // players.add(new MicroBot(utt));
        players.add(new mayari(utt));
    }

    public static void main(String[] args) {
        TournamentRunner.runTournament();
    }
}