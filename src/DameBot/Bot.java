package DameBot;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import CustomUnitClasses.AbstractionLayerAI;
import CustomUnitClasses.DameStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.AI;
import ai.core.ParameterSpecification;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;

public class Bot extends AbstractionLayerAI {
    public UnitTypeTable unitTypeTable;
    public Player player;
    public GameState game;
    public PhysicalGameState board;
    public Units units;
    public DameStarPathFinding pf;
    public boolean debug = false;

    public void log(String message) {
        if (debug) {
            System.out.println(message);
        }
    }

    public List<Unit> findUnitsWithin(List<Unit> units, Unit reference, double distance) {
        return units.stream()
                .filter(u -> !u.equals(reference) && distance(u, reference) <= distance)
                .collect(Collectors.toList());
    }

    public Unit findClosest(List<Unit> units, Unit reference) {
        return units.stream()
                .filter(u -> !u.equals(reference))
                .min(Comparator.comparingDouble(u -> distance(u, reference)))
                .orElse(null);
    }

    public Unit findClosestWithin(List<Unit> units, Unit reference, double distance) {
        return units.stream()
                .filter(u -> !u.equals(reference) && distance(u, reference) <= distance)
                .min(Comparator.comparingDouble(u -> distance(u, reference)))
                .orElse(null);
    }

    public int[] findFurthestAdjacentCell(Unit unit, List<Unit> enemies) {
        int[][] DIRECTIONS = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        int[] furthestCell = { 0, 0 };
        double maxDistance = 0;

        for (int[] direction : DIRECTIONS) {
            int nx = unit.getX() + direction[0];
            int ny = unit.getY() + direction[1];

            if (isWithinBoard(nx, ny) && !isOccupied(nx, ny) && board.getTerrain(nx, ny) == 0) {
                double distance = enemies.stream()
                        .filter(u -> !u.equals(unit))
                        .mapToDouble(u -> {
                            return distance(nx, ny, u.getX(), u.getY());
                        })
                        .max()
                        .orElse(0);

                if (distance >= maxDistance) {
                    maxDistance = distance;
                    furthestCell[0] = nx;
                    furthestCell[1] = ny;
                }
            }
        }

        return furthestCell;
    }

    public int[] findFurthestAdjacentCell(Unit unit, List<Unit> enemies, Unit target) {
        int[][] DIRECTIONS = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        int[] furthestCell = { 0, 0 };
        double maxScore = Double.NEGATIVE_INFINITY;

        for (int[] direction : DIRECTIONS) {
            int nx = unit.getX() + direction[0];
            int ny = unit.getY() + direction[1];
            if (isWithinBoard(nx, ny) && !isOccupied(nx, ny) && board.getTerrain(nx, ny) == 0 && game.free(nx, ny)) {
                double enemyDistance = enemies.stream()
                        .filter(u -> !u.equals(unit))
                        .mapToDouble(u -> distance(nx, ny, u.getX(), u.getY()))
                        .max()
                        .orElse(0);

                double targetDistance = distance(nx, ny, target.getX(), target.getY());
                double score = enemyDistance - targetDistance;

                if (score >= maxScore) {
                    maxScore = score;
                    furthestCell[0] = nx;
                    furthestCell[1] = ny;
                }
            }
        }

        return furthestCell;
    }

    public int[] findFurthestAdjacentCell(Unit unit, int[] pos, Unit target) {
        int[][] DIRECTIONS = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        int[] furthestCell = { 0, 0 };
        double maxScore = Double.NEGATIVE_INFINITY;

        for (int[] direction : DIRECTIONS) {
            int nx = unit.getX() + direction[0];
            int ny = unit.getY() + direction[1];
            if (isWithinBoard(nx, ny) && !isOccupied(nx, ny) && board.getTerrain(nx, ny) == 0 && game.free(nx, ny)) {
                double enemyDistance = distance(nx, ny, pos);
                double targetDistance = distance(nx, ny, target.getX(), target.getY());
                double score = enemyDistance - targetDistance;

                if (score >= maxScore) {
                    maxScore = score;
                    furthestCell[0] = nx;
                    furthestCell[1] = ny;
                }
            }
        }

        return furthestCell;
    }

    public int[] findFurthestAdjacentCell(Unit unit, Unit enemy) {
        return findFurthestAdjacentCell(unit, List.of(enemy));
    }

    public int[] findFurthestAdjacentCell(Unit unit, int[] pos) {
        int[][] DIRECTIONS = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        int[] furthestCell = { 0, 0 };
        double maxDistance = 0;

        for (int[] direction : DIRECTIONS) {
            int nx = unit.getX() + direction[0];
            int ny = unit.getY() + direction[1];

            if (isWithinBoard(nx, ny) && !isOccupied(nx, ny) && board.getTerrain(nx, ny) == 0 && game.free(nx, ny)) {
                double distance = distance(nx, ny, pos[0], pos[1]);

                if (distance >= maxDistance) {
                    maxDistance = distance;
                    furthestCell[0] = nx;
                    furthestCell[1] = ny;
                }
            }
        }

        return furthestCell;
    }

    public int[] findFurthestAdjacentCell(Unit unit, Unit enemy, Unit target) {
        return findFurthestAdjacentCell(unit, List.of(enemy), target);
    }

    public Set<String> findAdjacentCells(List<Unit> units) {
        int[][] DIRECTIONS = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        Set<String> cells = new HashSet<>();

        for (Unit unit : units) {
            for (int[] direction : DIRECTIONS) {
                int nx = unit.getX() + direction[0];
                int ny = unit.getY() + direction[1];

                if (isWithinBoard(nx, ny) && !isOccupied(nx, ny) && board.getTerrain(nx, ny) == 0
                        && game.free(nx, ny)) {
                    String cellId = nx + "," + ny;
                    cells.add(cellId);
                }
            }
        }

        return cells;
    }

    public boolean isOccupied(int x, int y) {
        return units._units.stream().anyMatch(u -> u.getX() == x && u.getY() == y);
    }

    public boolean isWithinBoard(int x, int y) {
        return x >= 0 && x < board.getWidth() && y >= 0 && y < board.getHeight();
    }

    public double distance(Unit u1, Unit u2) {
        return distance(u1.getX(), u1.getY(), u2.getX(), u2.getY());
    }

    public double distance(int x1, int y1, int x2, int y2) {
        return Math.hypot(x2 - x1, y2 - y1);
    }

    public double distance(Unit u1, int[] pos) {
        return distance(u1.getX(), u1.getY(), pos[0], pos[1]);
    }

    public double distance(Unit u1, int pos1, int pos2) {
        return distance(u1.getX(), u1.getY(), pos1, pos2);
    }

    public double distance(int x1, int y1, int[] pos) {
        return distance(x1, y1, pos[0], pos[1]);
    }

    public int[] nextEnemyPos(Unit target) {
        UnitAction targetAction = game.getUnitAction(target);
        if (targetAction != null && targetAction.getType() == 1 && targetAction.getDirection() != -1) {
            int newPosX = target.getX() + UnitAction.DIRECTION_OFFSET_X[targetAction.getDirection()];
            int newPosY = target.getY() + UnitAction.DIRECTION_OFFSET_Y[targetAction.getDirection()];
            return new int[] { newPosX, newPosY };
        } else {
            return new int[] { target.getX(), target.getY() };
        }
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
        this(unitTypeTable, new DameStarPathFinding());
    }

    public Bot(UnitTypeTable unitTypeTable, boolean debug) {
        this(unitTypeTable, new DameStarPathFinding(), debug);
        this.debug = debug;
    }

    public Bot(UnitTypeTable unitTypeTable, PathFinding pf, boolean debug) {
        super(pf);
        this.unitTypeTable = unitTypeTable;
        this.units = new Units(unitTypeTable);
        this.debug = debug;
        this.pf = (DameStarPathFinding) pf;
    }

    public Bot(UnitTypeTable unitTypeTable, PathFinding pf) {
        super(pf);
        this.unitTypeTable = unitTypeTable;
        this.units = new Units(unitTypeTable);
        this.pf = (DameStarPathFinding) pf;
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