package DameBot;

import rts.units.Unit;

public class Barracks {
    private Bot bot;

    public Barracks(Bot bot) {
        this.bot = bot;
        bot.units.barracks.forEach(barrack -> {
            if (barrack.isIdle(bot.game))
                assignTask(barrack);
        });
    }

    private void assignTask(Unit barrack) {
        Unit closestEnemyBase = bot.findClosest(bot.units._bases, barrack);
        if (bot.player.getResources() >= bot.units.LIGHT.cost
                && closestEnemyBase != null
                && bot.units.light.size() < (bot.units._light.size() >= 1 ? bot.units._light.size()
                        : bot.units._ranged.size() >= 1 ? Math.ceil(bot.units._ranged.size() / 2)
                                : bot.distance(barrack, closestEnemyBase) < 16 ? 1 : 1)) {
            bot.train(barrack, bot.units.LIGHT);
            return;
        }
        if (bot.player.getResources() >= bot.units.HEAVY.cost
                && bot.units.heavy
                        .size() < (bot.units._heavy.size() > 1
                                ? Math.floor(bot.units._heavy.size() / 2)
                                : bot.units._light.size() > 1 ? Math.floor(bot.units._light.size() / 2) : 0)
                && bot.units._ranged.size() == 0) {
            bot.train(barrack, bot.units.HEAVY);
            return;
        }
        if (bot.player.getResources() >= bot.units.RANGED.cost) {
            bot.train(barrack, bot.units.RANGED);
            return;
        }
    }
}