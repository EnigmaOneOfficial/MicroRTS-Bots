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
                (int) Math.hypot(bot.board.getWidth(), bot.board.getHeight()) / 3);
        boolean shouldTrain = (bot.units.barracks.isEmpty()
                ? bot.player.getResources() >= bot.units.BARRACKS.cost
                : bot.player.getResources() >= bot.units.WORKER.cost)
                && (bot.units.workers.size() < closeResources.size()
                        || bot.findClosestWithin(bot.units._units, base, 6) != null);

        if (shouldTrain) {
            bot.train(base, bot.units.WORKER);
        }
    }

}
