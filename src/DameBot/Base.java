package DameBot;

import java.util.List;
import java.util.stream.Collectors;

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
        List<Unit> closeResources = bot.findUnitsWithin(bot.units.resources, base, 8);
        boolean shouldTrain = bot.units.barracks.isEmpty()
                ? bot.player.getResources() >= bot.units.BARRACKS.cost
                : bot.player.getResources() >= bot.units.WORKER.cost;

        if (shouldTrain && bot.units.workers.size() <= closeResources.size()) {
            bot.train(base, bot.units.WORKER);
        }
    }

}
