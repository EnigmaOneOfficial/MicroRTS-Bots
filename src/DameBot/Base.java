package DameBot;

import java.util.List;
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
        List<Unit> closeResources = bot.findUnitsWithin(bot.units.resources, base,
                Math.hypot(bot.board.getWidth(), bot.board.getHeight()) / 2);
        Unit closeEnemy = bot.findClosestWithin(bot.units._units, base, 10);
        List<Unit> closeWorkers = bot.findUnitsWithin(bot.units.workers, base, 16);
        boolean shouldTrain = (bot.units.barracks.isEmpty()
                ? bot.player.getResources() >= bot.units.BARRACKS.cost
                : bot.player.getResources() >= bot.units.WORKER.cost)
                && (bot.units.workers.size() < Math.min(closeResources.size(), 8)
                        || closeEnemy != null)
                && (closeWorkers.size() < 4);

        if (shouldTrain) {
            if (closeEnemy != null) {
                bot.train(base, bot.units.WORKER);
            } else {
                bot.train(base, bot.units.WORKER, true);
            }
        }
    }

}
