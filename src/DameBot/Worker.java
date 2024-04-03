package DameBot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import rts.UnitAction;
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
        List<Unit> enemies = bot.findUnitsWithin(bot.units._units, worker, worker.getAttackRange());
        if (!enemies.isEmpty()) {
            System.out.println("Worker " + worker.getID() + " is attacking");
            bot.attack(worker, bot.findEnemyWithLowestHealth(enemies));
            return;
        }
        // Action 2: Return resources - non interrupting
        if (worker.getResources() > 0 && bot.units.bases.size() > 0) {
            System.out.println("Worker " + worker.getID() + " is returning resources");
            bot.harvest(worker, bot.units.bases.get(0));
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
            System.out.println("Worker " + worker.getID() + " is building a barracks");
            bot.units.harvesters.remove(worker);
            bot.units.defenders.remove(worker);
            if (!bot.units.builders.contains(worker))
                bot.units.builders.add(worker);
            Unit base = bot.units.bases.get(0);
            Unit enemyBase = bot.units._bases.get(0);
            int[] buildCoordinates = calculateBarracksCoordinates(base, enemyBase);
            bot.build(worker, bot.units.BARRACKS, buildCoordinates[0], buildCoordinates[1]);
            return;
        }

        // Action 4: Harvest resources
        if (bot.units.resources.size() > 0 && bot.units.bases.size() > 0) {
            List<Unit> closeResources = bot.findUnitsWithin(bot.units.resources, bot.units.bases.get(0), 8);
            if (bot.units.harvesters.size() < closeResources.size() + 1 || bot.units.harvesters.contains(worker)) {
                bot.units.builders.remove(worker);
                bot.units.defenders.remove(worker);
                if (!bot.units.harvesters.contains(worker))
                    bot.units.harvesters.add(worker);
                System.out.println("Worker " + worker.getID() + " is harvesting");
                bot.harvest(worker, bot.findClosest(bot.units.resources, worker), bot.units.bases.get(0));
                return;
            }
        }

        // Action 5: Defend/attack
        bot.units.harvesters.remove(worker);
        bot.units.builders.remove(worker);
        if (!bot.units.defenders.contains(worker))
            bot.units.defenders.add(worker);
        bot.attackWithMarch(worker);
        System.out.println("Worker " + worker.getID() + " is defending");

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
}