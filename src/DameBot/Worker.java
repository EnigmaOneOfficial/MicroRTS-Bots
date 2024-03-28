package DameBot;

import java.util.List;

import rts.UnitAction;
import rts.units.Unit;

public class Worker {
    private Bot bot;

    public Worker(Bot bot) {
        this.bot = bot;
        bot.units.builders.removeIf(builder -> !bot.units.workers.contains(builder));
        bot.units.harvesters.removeIf(harvester -> !bot.units.workers.contains(harvester));
        bot.units.defenders.removeIf(defender -> !bot.units.workers.contains(defender));

        if (bot.units.barracks.size() > 0)
            bot.units.builders.clear();
        if (bot.units.resources.size() == 0)
            bot.units.harvesters.clear();

        bot.units.workers.forEach(worker -> {
            if (worker.isIdle(bot.game))
                assignTask(worker);
        });
    }

    private void assignTask(Unit worker) {
        Unit base = bot.findClosest(bot.units.bases, worker);
        Unit enemyBase = bot.findClosest(bot.units._bases, worker);
        Unit enemy = bot.findClosest(bot.units._units, worker);
        Unit resource = bot.findClosest(bot.units.resources, worker);

        boolean isHarvester = bot.units.harvesters.contains(worker);
        boolean isBuilder = bot.units.builders.contains(worker);
        boolean isDefender = bot.units.defenders.contains(worker);

        if (enemy == null)
            return;

        boolean shouldPrioritizeAttack = (bot.distance(worker,
                enemy) <= (!isHarvester ? (worker.getAttackRange() + 1) : worker.getAttackRange())
                || (enemyBase == null && !isHarvester)) || base == null;

        if (shouldPrioritizeAttack) {
            bot.units.harvesters.removeIf(harvester -> harvester == worker);
            bot.units.builders.removeIf(builder -> builder == worker);
            bot.attack(worker, enemy);
            return;
        }

        if (worker.getResources() > 0) {
            bot.harvest(worker, resource, base);
            return;
        }

        boolean isBarracksBuilding = bot.units.builders.size() > 0
                && bot.units.builders.get(0).getUnitActions(bot.game).get(0).getType() == UnitAction.TYPE_PRODUCE;
        List<Unit> nearbyResources = bot.findUnitsWithin(bot.units.resources, base,
                (int) Math.floor(Math.sqrt(bot.board.getWidth() * bot.board.getHeight()) / 2));
        int harvestersNeeded = (int) Math.ceil(bot.findAdjacentCells(nearbyResources).size() / 2);
        if (harvestersNeeded == 1 && !isBarracksBuilding) {
            harvestersNeeded = 2;
        }

        Unit enemyWithinHalfOfMap = bot.findClosestWithin(bot.units._units, worker,
                (int) Math.floor(Math.sqrt(bot.board.getWidth() * bot.board.getHeight()) / 2));
        boolean canBuildBarracks = (bot.player.getResources() >= bot.units.BARRACKS.cost + bot.units.WORKER.cost
                && enemyBase != null
                && bot.units.builders.size() == 0 && !isBarracksBuilding
                && bot.units.harvesters.size() == harvestersNeeded && (!isHarvester || bot.units.workers.size() >= 2))
                || isBuilder;
        boolean shouldBuildBarracks = bot.units.barracks.size() == 0 && enemyWithinHalfOfMap == null;

        if (canBuildBarracks && shouldBuildBarracks) {

            int buildX = base.getX();
            int buildY = base.getY();
            buildX += (enemyBase.getX() > base.getX()) ? 1 : -1;
            buildY += (enemyBase.getY() > base.getY()) ? -2 : 2;
            buildX = Math.max(0, Math.min(buildX, bot.board.getWidth() - 1));
            buildY = Math.max(0, Math.min(buildY, bot.board.getHeight() - 1));

            double minDist = Double.MAX_VALUE;
            Unit closestWorker = null;
            for (Unit u : bot.units.workers) {
                double d = bot.distance(u.getX(), u.getY(), buildX, buildY);
                if (d < minDist) {
                    minDist = d;
                    closestWorker = u;
                }
            }

            if (closestWorker == worker) {
                if (!isBuilder)
                    bot.units.builders.add(worker);
                bot.units.harvesters.removeIf(harvester -> harvester == worker);
                bot.units.defenders.removeIf(defender -> defender == worker);

                bot.build(worker, bot.units.BARRACKS, buildX, buildY);
                return;
            }
        }

        boolean canHarvest = resource != null || worker.getResources() > 0;
        boolean shouldHarvest = bot.units.harvesters.size() < harvestersNeeded;

        if ((canHarvest && shouldHarvest) || isHarvester) {
            if (!isHarvester)
                bot.units.harvesters.add(worker);
            bot.units.defenders.removeIf(defender -> defender == worker);

            bot.harvest(worker, resource, base);
            return;
        }

        bot.units.harvesters.removeIf(harvester -> harvester == worker);
        bot.units.builders.removeIf(builder -> builder == worker);
        if (!isDefender)
            bot.units.defenders.add(worker);

        bot.attack(worker, enemy);
    }
}
