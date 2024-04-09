package DameBot;

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
        if (closestEnemy == null) {
            return;
        }

        int distanceToEnemy = bot.distance(ranged, closestEnemy);
        int attackRange = ranged.getAttackRange();
        int idealDistance = (int) (attackRange - 1);

        if (distanceToEnemy <= attackRange) {
            if (distanceToEnemy < idealDistance) {
                moveToIdealDistance(ranged, closestEnemy, idealDistance);
            } else {
                bot.attack(ranged, closestEnemy);
            }
        } else {
            moveToIdealDistance(ranged, closestEnemy, idealDistance);
        }
    }

    private void moveToIdealDistance(Unit ranged, Unit enemy, int idealDistance) {
        int[] bestPosition = findBestPosition(ranged, enemy, idealDistance);
        if (bestPosition != null) {
            if (ranged.getX() == enemy.getX() || ranged.getY() == enemy.getY()) {
                // If the ranged unit and enemy are on the same row or column, move diagonally
                int dx = (enemy.getX() - ranged.getX()) > 0 ? 1 : -1;
                int dy = (enemy.getY() - ranged.getY()) > 0 ? 1 : -1;
                bot.move(ranged, ranged.getX() + dx, ranged.getY() + dy);
            } else {
                bot.move(ranged, bestPosition[0], bestPosition[1]);
            }
        } else {
            bot.attack(ranged, enemy);
        }
    }

    private int[] findBestPosition(Unit ranged, Unit enemy, int distance) {
        int[] directions = { -1, 0, 1 };
        int bestScore = Integer.MIN_VALUE;
        int[] bestPosition = null;

        for (int dx : directions) {
            for (int dy : directions) {
                if (dx == 0 && dy == 0)
                    continue;

                int newX = ranged.getX() + dx * distance;
                int newY = ranged.getY() + dy * distance;

                if (bot.isWithinBoard(newX, newY) && !bot.isOccupied(newX, newY)) {
                    int score = evaluatePosition(ranged, enemy, newX, newY);

                    // Check adjacent positions of the enemy unit
                    for (int adjDx : directions) {
                        for (int adjDy : directions) {
                            if (adjDx == 0 && adjDy == 0)
                                continue;

                            int adjX = enemy.getX() + adjDx;
                            int adjY = enemy.getY() + adjDy;

                            if (bot.isWithinBoard(adjX, adjY) && !bot.isOccupied(adjX, adjY)) {
                                int adjScore = evaluatePosition(ranged, enemy, adjX, adjY);
                                score = Math.min(score, adjScore);
                            }
                        }
                    }

                    if (score > bestScore) {
                        bestScore = score;
                        bestPosition = new int[] { newX, newY };
                    }
                }
            }
        }

        return bestPosition;
    }

    private int evaluatePosition(Unit ranged, Unit enemy, int x, int y) {
        int distanceToEnemy = bot.distance(x, y, enemy.getX(), enemy.getY());
        int attackRange = ranged.getAttackRange();
        int idealDistance = (int) (attackRange - 1);

        int score = 0;
        if (distanceToEnemy <= attackRange) {
            score += 100;
        }
        score -= Math.abs(distanceToEnemy - idealDistance);

        // Check if the position allows surrounding the enemy
        int[] surroundingDirections = { -1, 0, 1 };
        for (int dx : surroundingDirections) {
            for (int dy : surroundingDirections) {
                if (dx == 0 && dy == 0)
                    continue;
                int surroundingX = enemy.getX() + dx;
                int surroundingY = enemy.getY() + dy;
                if (bot.isWithinBoard(surroundingX, surroundingY) && !bot.isOccupied(surroundingX, surroundingY)) {
                    score += 10;
                }
            }
        }

        return score;
    }
}