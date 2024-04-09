package DameBot;

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
        bot.attack(light, bot.findClosest(bot.units._units, light));
    }
}