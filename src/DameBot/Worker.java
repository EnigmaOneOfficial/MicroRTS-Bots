package DameBot;

import java.util.List;
import rts.units.Unit;

public class Worker {
    private Bot bot;

    public Worker(Bot bot) {
        this.bot = bot;
        updateWorkerLists();
        bot.units.workers.forEach(worker -> {
            if (worker.isIdle(bot.game))
                assignTask(worker);
        });
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
        // Action 1: Attack a nearby enemy - non interrupting
        List<Unit> enemies = bot.findUnitsWithin(bot.units._units, worker, worker.getAttackRange() + 1);
        Unit closestBase = bot.findClosest(bot.units.bases, worker);
        if (!enemies.isEmpty() || closestBase == null) {
            bot.log("Worker " + worker.getID() + " is attacking");
            bot.attack(worker, bot.findClosest(enemies, worker));
            return;
        }
        // Action 2: Return resources - non interrupting
        if (worker.getResources() > 0 && bot.units.bases.size() > 0) {
            bot.log("Worker " + worker.getID() + " is returning resources");
            bot.harvest(worker, closestBase);
            return;
        }

        // Action 3: Build a barracks
        if (bot.player.getResources() < bot.units.BARRACKS.cost + bot.units.WORKER.cost) {
            bot.units.builders.remove(worker);
        }
        if ((bot.units.barracks.isEmpty() && bot.units.bases.size() > 0
                && bot.units._bases.size() > 0
                && bot.player.getResources() >= bot.units.BARRACKS.cost + bot.units.WORKER.cost) &&
                (bot.units.builders.isEmpty() || bot.units.builders.contains(worker))) {
            bot.log("Worker " + worker.getID() + " is building a barracks");
            bot.units.harvesters.remove(worker);
            bot.units.defenders.remove(worker);
            if (!bot.units.builders.contains(worker))
                bot.units.builders.add(worker);
            Unit closestEnemy = bot.findClosest(bot.units._units, worker);
            int[] buildCoordinates = calculateBarracksCoordinates(closestBase, closestEnemy);
            bot.build(worker, bot.units.BARRACKS, buildCoordinates[0], buildCoordinates[1]);
            return;
        }

        // Action 4: Harvest resources
        List<Unit> closeResources = bot.findUnitsWithin(bot.units.resources, worker,
                (int) Math.hypot(bot.board.getWidth(), bot.board.getHeight()) / 3);
        if (closeResources.size() > 0 && bot.units.bases.size() > 0) {
            if (bot.units.harvesters.size() <= closeResources.size() || bot.units.harvesters.contains(worker)) {
                bot.units.builders.remove(worker);
                bot.units.defenders.remove(worker);
                if (!bot.units.harvesters.contains(worker))
                    bot.units.harvesters.add(worker);
                bot.log("Worker " + worker.getID() + " is harvesting");

                Unit chosenResource = bot.findClosest(closeResources, worker);

                // Harvest a resource where a pathfinding path exists

                bot.harvest(worker, chosenResource, closestBase);
                return;
            }
        }

        // Action 5: Defend/attack
        bot.units.harvesters.remove(worker);
        bot.units.builders.remove(worker);
        if (!bot.units.defenders.contains(worker))
            bot.units.defenders.add(worker);
        bot.attack(worker, bot.findClosest(bot.units._units, worker));
    }

    private int[] calculateBarracksCoordinates(Unit base, Unit enemyBase) {
        int buildX = base.getX();
        int buildY = base.getY();
        buildX += (enemyBase.getX() > base.getX()) ? 2 : -2;
        buildY += (enemyBase.getY() > base.getY()) ? -1 : 1;
        buildX = Math.max(0, Math.min(buildX, bot.board.getWidth() - 1));
        buildY = Math.max(0, Math.min(buildY, bot.board.getHeight() - 1));
        return new int[] { buildX, buildY };
    }
}