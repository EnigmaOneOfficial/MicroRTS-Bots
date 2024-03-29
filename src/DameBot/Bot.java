package DameBot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import CustomUnitClasses.AbstractionLayerAI;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.AI;
import ai.core.ParameterSpecification;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;

public class Bot extends AbstractionLayerAI {
    public UnitTypeTable unitTypeTable;
    public Player player;
    public GameState game;
    public PhysicalGameState board;
    public Units units;

    public Unit findEnemyWithLowestHealth(List<Unit> enemies) {
        return enemies.stream()
                .min(Comparator.comparingInt(Unit::getHitPoints))
                .orElse(null);
    }

    public boolean isValidRetreat(int x, int y) {
        return x >= 0 && x < board.getWidth() && y >= 0 && y < board.getHeight() && game.free(x, y);
    }

    public boolean isMovingAwayFromEnemies(int newX, int newY, Unit unit, List<Unit> enemies) {
        return enemies.stream().allMatch(enemy -> distance(newX, newY, enemy.getX(),
                enemy.getY()) > distance(unit.getX(), unit.getY(), enemy.getX(), enemy.getY()));
    }

    public Point chooseBestRetreat(List<Point> possibleRetreats, List<Unit> enemies) {
        return possibleRetreats.stream()
                .min(Comparator.comparingDouble(retreat -> enemies.stream()
                        .mapToDouble(enemy -> potentialDamage(retreat.x, retreat.y, enemy))
                        .sum()))
                .orElse(null);
    }

    public double potentialDamage(int x, int y, Unit enemy) {
        double distance = distance(x, y, enemy.getX(), enemy.getY());
        return (distance <= enemy.getAttackRange()) ? enemy.getMinDamage() : 0;
    }

    public void attackWithMarch(Unit unit) {
        List<Unit> enemiesInCloseRange = findUnitsWithin(units._units, unit, unit.getAttackRange() * 2);
        if (!enemiesInCloseRange.isEmpty()) {
            attack(unit, findEnemyWithLowestHealth(enemiesInCloseRange));
            return;
        }

        Unit enemyBase = findEnemyWithLowestHealth(units._bases);
        Unit enemyBarracks = findEnemyWithLowestHealth(units._barracks);

        if (enemyBase != null && enemyBarracks != null) {
            if (enemyBase.getHitPoints() < enemyBarracks.getHitPoints()) {
                attack(unit, enemyBase);
            } else {
                attack(unit, enemyBarracks);
            }
        } else {
            attack(unit, findEnemyWithLowestHealth(units._units));
        }
    }

    public List<Unit> findUnitsWithin(List<Unit> units, Unit reference, double distance) {
        return units.stream().filter(u -> distance(u, reference) <= distance).collect(Collectors.toList());
    }

    public Unit findClosest(List<Unit> units, Unit reference) {
        return units.stream().min(Comparator.comparingDouble(u -> distance(u, reference))).orElse(null);
    }

    public Unit findClosestWithin(List<Unit> units, Unit reference, double distance) {
        return findUnitsWithin(units, reference, distance).stream()
                .min(Comparator.comparingDouble(u -> distance(u, reference)))
                .orElse(null);
    }

    public List<Point> calculateRetreatPositions(Unit unit, List<Unit> enemies) {
        List<Point> retreats = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0)
                    continue;
                int newX = unit.getX() + dx;
                int newY = unit.getY() + dy;
                if (isValidRetreat(newX, newY)) {
                    retreats.add(new Point(newX, newY));
                }
            }
        }
        return retreats;
    }

    public void retreatOrAttack(Unit unit, List<Unit> enemiesWithinReducedAttackRange,
            List<Unit> enemiesWithinAttackRange) {
        List<Point> possibleRetreats = calculateRetreatPositions(unit, enemiesWithinAttackRange);
        Point bestRetreat = chooseBestRetreat(possibleRetreats, enemiesWithinAttackRange);
        if (bestRetreat != null) {
            move(unit, bestRetreat.x, bestRetreat.y);
        } else {
            Unit target = findClosest(enemiesWithinAttackRange, unit);
            if (target != null) {
                attack(unit, target);
            } else {
                attackWithMarch(unit);
            }
        }
    }

    public Set<String> findAdjacentCells(List<Unit> units) {
        int[][] DIRECTIONS = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        Set<String> cells = new HashSet<>();

        for (Unit unit : units) {
            for (int[] direction : DIRECTIONS) {
                int nx = unit.getX() + direction[0];
                int ny = unit.getY() + direction[1];

                if (isWithinBoard(nx, ny)) {
                    String cellId = nx + "," + ny;
                    cells.add(cellId);
                }
            }
        }

        return cells;
    }

    public boolean isWithinBoard(int x, int y) {
        return x >= 0 && x < board.getWidth() && y >= 0 && y < board.getHeight();
    }

    public double distance(Unit u1, Unit u2) {
        return distance(u1.getX(), u1.getY(), u2.getX(), u2.getY());
    }

    public double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    @Override
    public PlayerAction getAction(int player, GameState game) {
        setActionState(player, game);
        new Base(this);
        new Barracks(this);
        new Worker(this);
        new Light(this);
        new Heavy(this);
        new Ranged(this);
        return translateActions(player, game);
    }

    public void setActionState(int player, GameState game) {
        this.player = game.getPlayer(player);
        this.game = game;
        this.board = game.getPhysicalGameState();
        this.units.Refresh(board, player);
    }

    public Bot(UnitTypeTable unitTypeTable) {
        this(unitTypeTable, new AStarPathFinding());
    }

    public Bot(UnitTypeTable unitTypeTable, PathFinding pf) {
        super(pf);
        this.unitTypeTable = unitTypeTable;
        this.units = new Units(unitTypeTable);
    }

    @Override
    public void reset(UnitTypeTable unitTypeTable) {
        super.reset(unitTypeTable);
    }

    @Override
    public AI clone() {
        return new Bot(unitTypeTable, pf);
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        return null;
    }

}