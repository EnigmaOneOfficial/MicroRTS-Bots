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
        if (bot.player.getResources() >= bot.units.LIGHT.cost
                && bot.units.light.size() < (bot.units._light.size() > 0 ? bot.units._light.size() / 2 + 2 : 0)) {
            bot.train(barrack, bot.units.LIGHT);
            return;
        }
        if (bot.player.getResources() >= bot.units.RANGED.cost) {
            bot.train(barrack, bot.units.RANGED);
            return;
        }
    }
}