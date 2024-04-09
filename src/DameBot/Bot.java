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

    public boolean isOccupied(int x, int y) {
        return units._units.stream().anyMatch(u -> u.getX() == x && u.getY() == y);
    }

    public boolean isWithinBoard(int x, int y) {
        return x >= 0 && x < board.getWidth() && y >= 0 && y < board.getHeight();
    }

    public int distance(Unit u1, Unit u2) {
        return distance(u1.getX(), u1.getY(), u2.getX(), u2.getY());
    }

    public int distance(int x1, int y1, int x2, int y2) {
        return (int) Math.hypot(x2 - x1, y2 - y1);
    }

    public int distance(int pos1, int pos2) {
        return Math.abs(pos2 - pos1);
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