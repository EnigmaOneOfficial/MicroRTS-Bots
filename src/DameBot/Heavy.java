package DameBot;

import rts.units.Unit;

public class Heavy {
    private Bot bot;

    public Heavy(Bot bot) {
        this.bot = bot;
        bot.units.heavy.forEach(heavy -> {
            if (heavy.isIdle(bot.game))
                assignTask(heavy);
        });
    }

    private void assignTask(Unit heavy) {
        Unit closestEnemy = bot.findClosest(bot.units._units, heavy);
        bot.attack(heavy, closestEnemy);
    }
}
