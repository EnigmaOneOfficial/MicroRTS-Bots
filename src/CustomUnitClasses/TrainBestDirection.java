package CustomUnitClasses;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Stream;

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

        // Set enemyBase and ownBase
        for (Unit u : pgs.getUnits()) {
            if (u.getType().name == "Base") {
                if (u.getPlayer() == unit.getPlayer()) {
                    ownBase.add(u);
                } else {
                    enemyBase.add(u);
                }
            }
        }

        if (enemyBase.isEmpty() || ownBase.isEmpty()) {
            return null; // Return null if no enemy base is found
        }
        int enemyBaseX = enemyBase.get(0).getX();
        int enemyBaseY = enemyBase.get(0).getY();

        int bestDirection = -1;
        int bestScore = Integer.MIN_VALUE; // Initialize with min value to ensure any score is better

        // Directions mapped to their deltas (dx, dy)
        int[][] directions = new int[][] { { 0, -1, UnitAction.DIRECTION_UP }, { 1, 0, UnitAction.DIRECTION_RIGHT },
                { 0, 1, UnitAction.DIRECTION_DOWN }, { -1, 0, UnitAction.DIRECTION_LEFT } };

        for (int[] dir : directions) {
            int newX = unit.getX() + dir[0];
            int newY = unit.getY() + dir[1];
            if (newX >= 0 && newX < pgs.getWidth() && newY >= 0 && newY < pgs.getHeight() && gs.free(newX, newY)) {
                int score = score(newX, newY, type, unit.getPlayer(), pgs, enemyBaseX, enemyBaseY, ownBase.get(0));
                if (score > bestScore || bestDirection == -1) {
                    bestScore = score;
                    bestDirection = dir[2];
                }
            }
        }

        completed = true; // Assuming this is a flag indicating the action has been decided

        if (bestDirection != -1) {
            UnitAction ua = new UnitAction(UnitAction.TYPE_PRODUCE, bestDirection, type);
            if (gs.isUnitActionAllowed(unit, ua))
                return ua;
        }

        return null; // Return null if no valid direction is found
    }

    public int score(int x, int y, UnitType type, int player, PhysicalGameState pgs, int enemyBaseX, int enemyBaseY,
            Unit ownBase) {
        int score = 0;

        // Distance to enemy base
        int distanceToEnemyBase = Math.abs(enemyBaseX - x) + Math.abs(enemyBaseY - y);
        score -= distanceToEnemyBase; // Lower distance increases score

        // Distance to own base (the further, the better)
        int distanceToOwnBase = Math.abs(ownBase.getX() - x) + Math.abs(ownBase.getY() - y);
        score += 2 * distanceToOwnBase; // Increase score significantly based on distance from own base

        // Adjust scoring based on other units/resources if needed...

        return score;
    }

}
