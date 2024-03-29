package DameBot;

import java.util.List;

import rts.units.Unit;

public class Light {
    private Bot bot;

    public Light(Bot bot) {
        this.bot = bot;
        bot.units.light.forEach(light -> {
            if (light.isIdle(bot.game))
                assignTask(light);
        });
    }

    private void assignTask(Unit light) {
        List<Unit> enemiesWithinAttackRange = bot.findUnitsWithin(bot.units._units, light, light.getAttackRange() + 1);

        if (!enemiesWithinAttackRange.isEmpty()) {
            Unit closeRanged = bot.findClosest(enemiesWithinAttackRange, light);
            if (closeRanged != null) {
                bot.attack(light, closeRanged);
                return;
            }
        }
        bot.attack(light, bot.findClosest(bot.units._units, light));
    }
}