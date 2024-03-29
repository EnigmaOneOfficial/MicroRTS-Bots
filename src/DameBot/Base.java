package DameBot;

import java.util.List;

import rts.UnitAction;
import rts.units.Unit;

public class Base {
    private Bot bot;

    public Base(Bot bot) {
        this.bot = bot;
        bot.units.bases.forEach(base -> {
            if (base.isIdle(bot.game))
                assignTask(base);
        });
    }

    private void assignTask(Unit base) {
        boolean isBarracksBuilding = bot.units.builders.size() > 0
                && bot.units.builders.get(0).getUnitActions(bot.game).get(0).getType() == UnitAction.TYPE_PRODUCE;
        List<Unit> enemiesWithinHalfBoard = bot.findUnitsWithin(bot.units._units, base,
                (int) Math.floor(Math.sqrt(bot.board.getWidth() * bot.board.getHeight()) / 4));
        boolean shouldTrain = (bot.units.barracks.size() == 0
                && !isBarracksBuilding && bot.units.workers.size() > 2
                && enemiesWithinHalfBoard.size() == 0)
                        ? bot.player.getResources() >= bot.units.BARRACKS.cost + bot.units.WORKER.cost
                        : bot.player.getResources() >= bot.units.WORKER.cost;

        if (shouldTrain && bot.units.defenders.size() == 0) {
            bot.train(base, bot.units.WORKER);
            return;
        }
    }
}
