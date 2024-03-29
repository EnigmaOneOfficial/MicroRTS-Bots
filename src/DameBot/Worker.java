package DameBot;

import java.util.List;

import rts.UnitAction;
import rts.units.Unit;

public class Worker {
    private Bot bot;

    public Worker(Bot bot) {
        this.bot = bot;
        updateWorkerLists();
        bot.units.workers.forEach(this::assignTask);
    }

    private void updateWorkerLists() {
        bot.units.builders.removeIf(builder -> !bot.units.workers.contains(builder));
        bot.units.harvesters.removeIf(harvester -> !bot.units.workers.contains(harvester));
        bot.units.defenders.removeIf(defender -> !bot.units.workers.contains(defender));

        if (!bot.units.barracks.isEmpty()) {
            bot.units.builders.clear();
        }
        if (bot.units.resources.isEmpty()) {
            bot.units.harvesters.clear();
        }
    }

    private void assignTask(Unit worker) {
        if (worker.isIdle(bot.game)) {
            Unit base = bot.findClosest(bot.units.bases, worker);
            Unit enemyBase = bot.findClosest(bot.units._bases, worker);
            Unit enemy = bot.findClosest(bot.units._units, worker);
            Unit resource = bot.findClosest(bot.units.resources, worker);

            boolean isHarvester = bot.units.harvesters.contains(worker);
            boolean isBuilder = bot.units.builders.contains(worker);

            if (enemy != null) {
                boolean shouldPrioritizeAttack = bot.distance(worker,
                        enemy) <= (!isHarvester ? (worker.getAttackRange() + 1) : worker.getAttackRange())
                        || (enemyBase == null && !isHarvester) || base == null;

                if (shouldPrioritizeAttack) {
                    bot.units.harvesters.remove(worker);
                    bot.units.builders.remove(worker);
                    bot.attack(worker, enemy);
                    return;
                }
            }

            if (worker.getResources() > 0) {
                bot.harvest(worker, resource, base);
                return;
            }

            boolean isBarracksBuilding = !bot.units.builders.isEmpty()
                    && bot.units.builders.get(0).getUnitActions(bot.game).get(0).getType() == UnitAction.TYPE_PRODUCE;
            List<Unit> nearbyResources = bot.findUnitsWithin(bot.units.resources, base,
                    (int) Math.floor(Math.sqrt(bot.board.getWidth() * bot.board.getHeight()) / 4));
            int harvestersNeeded = (int) Math.ceil(bot.findAdjacentCells(nearbyResources).size() / 2);
            if (harvestersNeeded == 1 && !isBarracksBuilding) {
                harvestersNeeded = 2;
            }

            Unit enemyWithinHalfOfMap = bot.findClosestWithin(bot.units._units, worker,
                    (int) Math.floor(Math.sqrt(bot.board.getWidth() * bot.board.getHeight()) / 2));
            boolean canBuildBarracks = (bot.player.getResources() >= bot.units.BARRACKS.cost + bot.units.WORKER.cost
                    && enemyBase != null && bot.units.builders.isEmpty() && !isBarracksBuilding
                    && bot.units.harvesters.size() == harvestersNeeded
                    && (!isHarvester || bot.units.workers.size() >= 2))
                    || isBuilder;
            boolean shouldBuildBarracks = bot.units.barracks.isEmpty() && enemyWithinHalfOfMap == null;

            if (canBuildBarracks && shouldBuildBarracks) {
                int[] buildCoords = calculateBarracksCoordinates(base, enemyBase);
                Unit closestWorker = findClosestWorkerToCoordinates(buildCoords[0], buildCoords[1]);

                if (closestWorker == worker) {
                    if (!isBuilder) {
                        bot.units.builders.add(worker);
                    }
                    bot.units.harvesters.remove(worker);
                    bot.units.defenders.remove(worker);
                    bot.build(worker, bot.units.BARRACKS, buildCoords[0], buildCoords[1]);
                    return;
                }
            }

            boolean canHarvest = resource != null || worker.getResources() > 0;
            boolean shouldHarvest = bot.units.harvesters.size() < harvestersNeeded;

            if ((canHarvest && shouldHarvest) || isHarvester) {
                if (!isHarvester) {
                    bot.units.harvesters.add(worker);
                }
                bot.units.defenders.remove(worker);
                bot.harvest(worker, resource, base);
                return;
            }

            bot.units.harvesters.remove(worker);
            bot.units.builders.remove(worker);
            if (!bot.units.defenders.contains(worker)) {
                bot.units.defenders.add(worker);
            }
            bot.attack(worker, enemy);
        }
    }

    private int[] calculateBarracksCoordinates(Unit base, Unit enemyBase) {
        int buildX = base.getX();
        int buildY = base.getY();
        buildX += (enemyBase.getX() > base.getX()) ? 1 : -1;
        buildY += (enemyBase.getY() > base.getY()) ? -2 : 2;
        buildX = Math.max(0, Math.min(buildX, bot.board.getWidth() - 1));
        buildY = Math.max(0, Math.min(buildY, bot.board.getHeight() - 1));
        return new int[] { buildX, buildY };
    }

    private Unit findClosestWorkerToCoordinates(int x, int y) {
        double minDist = Double.MAX_VALUE;
        Unit closestWorker = null;
        for (Unit u : bot.units.workers) {
            double d = bot.distance(u.getX(), u.getY(), x, y);
            if (d < minDist) {
                minDist = d;
                closestWorker = u;
            }
        }
        return closestWorker;
    }
}