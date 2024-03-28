package DameBot;

import java.util.ArrayList;
import java.util.List;

import rts.units.Unit;

public class Ranged {

    private Bot bot;

    public Ranged(Bot bot) {
        this.bot = bot;
        bot.units.ranged.forEach(ranged -> {
            if (ranged.isIdle(bot.game))
                assignTask(ranged);
        });
    }

    private List<Point> calculateRetreatPositions(Unit ranged, List<Unit> enemies) {
        List<Point> retreats = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0)
                    continue;
                int newX = ranged.getX() + dx;
                int newY = ranged.getY() + dy;
                if (bot.isValidRetreat(newX, newY) && bot.isMovingAwayFromEnemies(newX, newY, ranged, enemies)) {
                    retreats.add(new Point(newX, newY));
                }
            }
        }
        return retreats;
    }

    private void assignTask(Unit ranged) {
        List<Unit> enemiesWithinAttackRange = bot.findUnitsWithin(bot.units._units, ranged, ranged.getAttackRange());
        List<Unit> enemiesWithinReducedAttackRange = bot.findUnitsWithin(bot.units._units, ranged,
                ranged.getAttackRange() - 1);
        List<Unit> enemyRangedUnitsWithinAttackRange = bot.findUnitsWithin(bot.units._ranged, ranged,
                ranged.getAttackRange());

        if (!enemyRangedUnitsWithinAttackRange.isEmpty()) {
            bot.attack(ranged, bot.findEnemyWithLowestHealth(enemyRangedUnitsWithinAttackRange));
        } else if (!enemiesWithinReducedAttackRange.isEmpty()) {
            retreatOrAttack(ranged, enemiesWithinReducedAttackRange, enemiesWithinAttackRange);
        } else if (!enemiesWithinAttackRange.isEmpty()) {
            bot.attack(ranged, bot.findEnemyWithLowestHealth(enemiesWithinAttackRange));
        } else {
            bot.attackWithMarch(ranged);
        }
    }

    private void retreatOrAttack(Unit ranged, List<Unit> enemiesWithinReducedAttackRange,
            List<Unit> enemiesWithinAttackRange) {
        List<Point> possibleRetreats = calculateRetreatPositions(ranged, enemiesWithinAttackRange);
        Point bestRetreat = bot.chooseBestRetreat(possibleRetreats, enemiesWithinAttackRange);
        if (bestRetreat != null) {
            bot.move(ranged, bestRetreat.x, bestRetreat.y);
        } else {
            Unit target = bot.findEnemyWithLowestHealth(enemiesWithinAttackRange);
            if (target != null) {
                bot.attack(ranged, target);
            } else {
                bot.attackWithMarch(ranged);
                // bot.attack(ranged, bot.findClosest(bot.units._units, ranged));
            }
        }
    }

}