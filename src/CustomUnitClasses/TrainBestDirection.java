package CustomUnitClasses;

import java.util.ArrayList;
import java.util.List;

import ai.abstraction.AbstractAction;
import rts.GameState;
import rts.PhysicalGameState;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitType;
import util.XMLWriter;

public class TrainBestDirection extends AbstractAction {
    UnitType type;
    boolean completed = false;
    Unit unit;

    public TrainBestDirection(Unit u, UnitType a_type) {
        super(u);
        type = a_type;
        unit = u;
    }

    public boolean completed(GameState pgs) {
        return completed;
    }

    public boolean equals(Object o) {
        if (!(o instanceof TrainBestDirection))
            return false;
        TrainBestDirection a = (TrainBestDirection) o;
        return type == a.type;
    }

    public void toxml(XMLWriter w) {
        w.tagWithAttributes("Train", "unitID=\"" + unit.getID() + "\" type=\"" + type.name + "\"");
        w.tag("/Train");
    }

    public UnitAction execute(GameState gs, ResourceUsage ru) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        List<Unit> enemyBase = new ArrayList<>();
        List<Unit> ownBase = new ArrayList<>();
        for (Unit u : pgs.getUnits()) {
            if (u.getType().name.equals("Base")) {
                if (u.getPlayer() == unit.getPlayer()) {
                    ownBase.add(u);
                } else {
                    enemyBase.add(u);
                }
            }
        }
        int enemyBaseX = -1;
        int enemyBaseY = -1;
        if (!enemyBase.isEmpty()) {
            enemyBaseX = enemyBase.get(0).getX();
            enemyBaseY = enemyBase.get(0).getY();
        }
        Unit ownBaseUnit = null;
        if (!ownBase.isEmpty()) {
            ownBaseUnit = ownBase.get(0);
        }
        int[][] directions = new int[][] {
                { 0, -1, UnitAction.DIRECTION_UP },
                { 1, 0, UnitAction.DIRECTION_RIGHT },
                { 0, 1, UnitAction.DIRECTION_DOWN },
                { -1, 0, UnitAction.DIRECTION_LEFT }
        };
        int[] bestDirection = findBestAvailableDirection(gs, directions, enemyBaseX, enemyBaseY, ownBaseUnit);
        completed = true;
        if (bestDirection != null) {
            UnitAction ua = new UnitAction(UnitAction.TYPE_PRODUCE, bestDirection[2], type);
            if (gs.isUnitActionAllowed(unit, ua)) {
                return ua;
            }
        }
        return null;
    }

    private int[] findBestAvailableDirection(GameState gs, int[][] directions, int enemyBaseX, int enemyBaseY,
            Unit ownBase) {
        int bestScore = Integer.MIN_VALUE;
        int[] bestDirection = null;
        for (int[] dir : directions) {
            int newX = unit.getX() + dir[0];
            int newY = unit.getY() + dir[1];
            if (isValidTrainDirection(gs, newX, newY)) {
                int score = score(newX, newY, type, unit.getPlayer(), gs.getPhysicalGameState(), enemyBaseX, enemyBaseY,
                        ownBase);
                if (score > bestScore || bestDirection == null) {
                    bestScore = score;
                    bestDirection = dir;
                }
            }
        }
        return bestDirection;
    }

    private boolean isValidTrainDirection(GameState gs, int x, int y) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        return x >= 0 && x < pgs.getWidth() && y >= 0 && y < pgs.getHeight() && gs.free(x, y);
    }

    public int score(int x, int y, UnitType type, int player, PhysicalGameState pgs, int enemyBaseX, int enemyBaseY,
            Unit ownBase) {
        int score = 0;
        if (enemyBaseX != -1 && enemyBaseY != -1) {
            int distanceToEnemyBase = Math.abs(enemyBaseX - x) + Math.abs(enemyBaseY - y);
            score -= distanceToEnemyBase;
        }
        if (ownBase != null) {
            int distanceToOwnBase = Math.abs(ownBase.getX() - x) + Math.abs(ownBase.getY() - y);
            score += 2 * distanceToOwnBase;
        }
        return score;
    }
}