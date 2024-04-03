package DameBot;

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

    private void assignTask(Unit ranged) {
        List<Unit> enemiesWithinAttackRange = bot.findUnitsWithin(bot.units._units, ranged, ranged.getAttackRange());
        List<Unit> enemiesWithinReducedAttackRange = bot.findUnitsWithin(bot.units._units, ranged,
                ranged.getAttackRange() - 1);
        List<Unit> enemyRangedUnitsWithinAttackRange = bot.findUnitsWithin(bot.units._ranged, ranged,
                ranged.getAttackRange());
        List<Unit> enemyLightUnitsWithinAttackRange = bot.findUnitsWithin(bot.units._light, ranged,
                ranged.getAttackRange());
        List<Unit> enemyWorkerUnitsWithinAttackRange = bot.findUnitsWithin(bot.units._workers, ranged,
                ranged.getAttackRange());

        if (!enemyRangedUnitsWithinAttackRange.isEmpty()) {
            // Should both kill each other
            bot.attack(ranged, bot.findEnemyWithLowestHealth(enemyRangedUnitsWithinAttackRange));
        } else if (!enemyLightUnitsWithinAttackRange.isEmpty()) {
            // Cant outrun
            bot.attack(ranged, bot.findEnemyWithLowestHealth(enemyLightUnitsWithinAttackRange));
        } else if (!enemyWorkerUnitsWithinAttackRange.isEmpty()) {
            // One shottable
            bot.attack(ranged, bot.findEnemyWithLowestHealth(enemyWorkerUnitsWithinAttackRange));
        } else if (!enemiesWithinReducedAttackRange.isEmpty()) {
            // Retreat, only attacking if necessary
            bot.retreatOrAttack(ranged, enemiesWithinAttackRange);
        } else if (!enemiesWithinAttackRange.isEmpty()) {
            bot.attack(ranged, bot.findEnemyWithLowestHealth(enemiesWithinAttackRange));
        } else {
            bot.attackWithMarch(ranged);
        }
    }

}