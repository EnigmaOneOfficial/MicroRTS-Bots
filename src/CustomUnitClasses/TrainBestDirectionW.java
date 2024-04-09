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

public class TrainBestDirectionW extends AbstractAction {
    UnitType type;
    boolean completed = false;
    Unit unit;

    public TrainBestDirectionW(Unit u, UnitType a_type) {
        super(u);
        type = a_type;
        unit = u;
    }

    public boolean completed(GameState pgs) {
        return completed;
    }

    public boolean equals(Object o) {
        if (!(o instanceof TrainBestDirectionW))
            return false;
        TrainBestDirectionW a = (TrainBestDirectionW) o;
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
        int[] bestDirection = findBestAvailableDirection(gs, directions, ownBaseUnit);
        completed = true;
        if (bestDirection != null) {
            UnitAction ua = new UnitAction(UnitAction.TYPE_PRODUCE, bestDirection[2], type);
            if (gs.isUnitActionAllowed(unit, ua)) {
                return ua;
            }
        }
        return null;
    }

    private boolean isValidTrainDirection(GameState gs, int x, int y) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        return x >= 0 && x < pgs.getWidth() && y >= 0 && y < pgs.getHeight() && gs.free(x, y);
    }

    private int[] findBestAvailableDirection(GameState gs, int[][] directions, Unit ownBase) {
        int bestScore = Integer.MIN_VALUE;
        int[] bestDirection = null;
        Unit nearestResource = findNearestResource(gs);
        for (int[] dir : directions) {
            int newX = unit.getX() + dir[0];
            int newY = unit.getY() + dir[1];
            if (isValidTrainDirection(gs, newX, newY)) {
                int score = score(newX, newY, type, unit.getPlayer(), gs.getPhysicalGameState(), nearestResource,
                        ownBase);
                if (score > bestScore || bestDirection == null) {
                    bestScore = score;
                    bestDirection = dir;
                }
            }
        }
        return bestDirection;
    }

    private Unit findNearestResource(GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Unit nearestResource = null;
        int minDistance = Integer.MAX_VALUE;
        for (Unit u : pgs.getUnits()) {
            if (u.getType().isResource) {
                int distance = Math.abs(u.getX() - unit.getX()) + Math.abs(u.getY() - unit.getY());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestResource = u;
                }
            }
        }
        return nearestResource;
    }

    public int score(int x, int y, UnitType type, int player, PhysicalGameState pgs, Unit nearestResource,
            Unit ownBase) {
        int score = 0;
        if (nearestResource != null) {
            int distanceToResource = Math.abs(nearestResource.getX() - x) + Math.abs(nearestResource.getY() - y);
            score -= distanceToResource;
        }
        if (ownBase != null) {
            int distanceToOwnBase = Math.abs(ownBase.getX() - x) + Math.abs(ownBase.getY() - y);
            score += distanceToOwnBase;
        }
        return score;
    }

}