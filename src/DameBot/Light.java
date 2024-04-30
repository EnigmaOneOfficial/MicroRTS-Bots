package DameBot;

import java.util.List;

import rts.units.Unit;
import rts.units.UnitType;

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
        Unit closestBase = bot.findClosest(bot.units._bases, light);
        Unit closestBarracks = bot.findClosest(bot.units._barracks, light);
        Unit closestEnemy = bot.findClosest(bot.units._units, light);
        List<Unit> heavies = bot.findUnitsWithin(bot.units._heavy, light, 6);
        List<Unit> ranged = bot.findUnitsWithin(bot.units._ranged, light, 6);

        Unit priorityTarget = closestBase != null ? closestBase
                : closestBarracks != null ? closestBarracks : closestEnemy;

        if (Math.ceil(bot.distance(light, closestEnemy)) == light.getAttackRange()) {
            bot.attack(light, closestEnemy);
            return;
        }

        // Avoid ranged
        if (!ranged.isEmpty()) {
            int[] furthestCell = bot.findFurthestAdjacentCell(light, ranged);
            if (Math.ceil(bot.distance(light, furthestCell)) <= light.getAttackRange()) {
                bot.move(light, furthestCell[0], furthestCell[1]);
                return;
            }
        }

        // Avoid heavies
        if (!heavies.isEmpty()) {
            int[] furthestCell = bot.findFurthestAdjacentCell(light, heavies, priorityTarget);
            if (Math.ceil(bot.distance(light, furthestCell)) <= light.getAttackRange()) {
                bot.move(light, furthestCell[0], furthestCell[1]);
                return;
            }
        }

        if (closestBase != null) {
            bot.attack(light, closestBase);
        } else if (closestBarracks != null) {
            bot.attack(light, closestBarracks);
        } else if (closestEnemy != null) {
            bot.attack(light, closestEnemy);
        }
    }
}