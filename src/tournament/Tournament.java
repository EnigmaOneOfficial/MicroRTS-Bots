package tournament;

import java.util.ArrayList;
import java.util.List;

import ai.core.AI;
import rts.units.UnitTypeTable;

import ai.PassiveAI;
import ai.RandomAI;
import ai.abstraction.WorkerRush;
import ai.coac.CoacAI;

import BaluBot.BaluBot;
import DameBot.DameBot;
import DinkleBot.DinkleBot;
import HSBot.HSBot;
import LasyaBot.LasyaBot;
import RangedRushImproved.RangedRushImproved;
import RitsBot.RitsBot;
import brady.BradyBot;
import kaleb.KalebBot;
import mayariBot.mayari;
import mybot.MyBot;
import nick.nickBot;
import sspringer_mcts_bot.sspringer_mcts_bot;
import swag.SwagBot;

public class Tournament {

    public static List<AI> getBots(TournamentConfig config) throws Exception {
        List<AI> players = new ArrayList<>();
        UnitTypeTable utt = config.getUtt();

        // Testing Bots:
        players.add(new WorkerRush(utt));
        players.add(new PassiveAI(utt));
        players.add(new RandomAI(utt));
        players.add(new CoacAI(utt));
        players.add(new mayari(utt));

        // Student Bots:
        // players.add(new BaluBot(utt));
        // players.add(new BradyBot(utt));
        // players.add(new DameBot(utt));
        // players.add(new DinkleBot(utt));
        // players.add(new HSBot(utt));
        // players.add(new KalebBot(utt));
        // players.add(new LasyaBot(utt));
        // players.add(new MyBot(utt));
        // players.add(new nickBot(utt));
        // players.add(new RangedRushImproved(utt));
        // players.add(new RitsBot(utt));
        // players.add(new sspringer_mcts_bot(utt));
        // players.add(new SwagBot(utt));

        return players;
    }

    public static void main(String[] args) {
        TournamentConfig config = new TournamentConfig();

        // config.setMaps(new String[] { "maps/32x32/basesWorkers32x32.xml" });
        // config.setMaxCycles(10000);
        // config.setUpdatePeriod(50);
        // config.setWindowSize(1600);
        // config.setDisposeWindow(false);
        // config.setCheckAdvantage(true);
        // config.setMaxDuration(120000);
        // config.setVisible(true);
        // config.setSimulations(5);
        // config.setTimeBudget(5);
        // config.setType(TournamentType.BRACKET);

        TournamentRunner.runTournament(config);
    }
}