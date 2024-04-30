package DameBot;

import java.util.List;

import rts.units.Unit;

public class Ranged {
    private Bot bot;

    public Ranged(Bot bot) {
        this.bot = bot;
        bot.units.ranged.forEach(ranged -> {
            if (ranged.isIdle(bot.game)) {
                assignTask(ranged);
            }
        });
    }

    private void assignTask(Unit ranged) {
        Unit closestEnemy = bot.findClosest(bot.units._units, ranged);
        int[] nextEnemyPos = bot.nextEnemyPos(closestEnemy);
        double distanceToNextPos = Math.ceil(bot.distance(ranged, nextEnemyPos));
        double distanceToClosestEnemy = Math.ceil(bot.distance(ranged, closestEnemy));

        List<Unit> light = bot.findUnitsWithin(bot.units._light, ranged, ranged.getAttackRange() * 2);
        if (!light.isEmpty()) {
            int[] furthestCell = bot.findFurthestAdjacentCell(ranged, bot.nextEnemyPos(bot.findClosest(light, ranged)));
            double distanceToFurthestCell = Math.ceil(bot.distance(ranged, furthestCell));
            if (distanceToFurthestCell <= ranged.getAttackRange()) {
                bot.attack(ranged, bot.findClosest(light, ranged));
                return;
            }
            bot.move(ranged, furthestCell[0], furthestCell[1]);
            return;
        }

        List<Unit> rangersWithinRange = bot.findUnitsWithin(bot.units._ranged, ranged, ranged.getAttackRange());
        if (!rangersWithinRange.isEmpty()) {
            bot.attack(ranged, bot.findClosest(rangersWithinRange, ranged));
            return;
        }

        if (distanceToNextPos == ranged.getAttackRange()) {
            bot.attack(ranged, closestEnemy);
        } else if (distanceToClosestEnemy < ranged.getAttackRange()) {
            int[] furthestCell = bot.findFurthestAdjacentCell(ranged, nextEnemyPos, closestEnemy);
            double distanceToFurthestCell = Math.ceil(bot.distance(ranged, furthestCell));
            if (distanceToFurthestCell <= distanceToClosestEnemy || distanceToFurthestCell <= distanceToNextPos) {
                bot.attack(ranged, closestEnemy);
                return;
            }
            bot.move(ranged, furthestCell[0], furthestCell[1]);
        } else {
            bot.attack(ranged, closestEnemy);
        }
    }
}