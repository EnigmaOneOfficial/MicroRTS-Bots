package CustomUnitClasses;

import java.util.Random;

import ai.abstraction.AbstractAction;
import ai.abstraction.pathfinding.PathFinding;
import rts.GameState;
import rts.PhysicalGameState;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.units.Unit;
import util.XMLWriter;

public class AttackBest extends AbstractAction {
    Unit target;
    PathFinding pf;

    public AttackBest(Unit u, Unit a_target, PathFinding a_pf) {
        super(u);
        target = a_target;
        pf = a_pf;
    }

    public boolean completed(GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        return !pgs.getUnits().contains(target);
    }

    public boolean equals(Object o) {
        if (!(o instanceof AttackBest))
            return false;
        AttackBest a = (AttackBest) o;
        return target.getID() == a.target.getID() && pf.getClass() == a.pf.getClass();
    }

    public void toxml(XMLWriter w) {
        w.tagWithAttributes("Attack",
                "unitID=\"" + getUnit().getID() + "\" target=\"" + target.getID() + "\" pathfinding=\""
                        + pf.getClass().getSimpleName() + "\"");
        w.tag("/Attack");
    }

    public UnitAction execute(GameState gs, ResourceUsage ru) {
        if (enemyIsInRangeAttack(getUnit(), this.target)) {
            return new UnitAction(UnitAction.TYPE_ATTACK_LOCATION, this.target.getX(), this.target.getY());
        } else {
            int[] newPosition = nextPos(this.target, gs);
            if (dist(getUnit(), newPosition) <= (double) getUnit().getAttackRange()) {
                return new UnitAction(UnitAction.TYPE_NONE, 1);
            } else {
                UnitAction move = this.pf.findPathToPositionInRange(getUnit(),
                        this.target.getX() + this.target.getY() * gs.getPhysicalGameState().getWidth(),
                        getUnit().getAttackRange(), gs, ru);
                if (move != null && gs.isUnitActionAllowed(getUnit(), move)) {
                    return move;
                } else {
                    int m = (new Random()).nextInt(4);
                    UnitAction m1 = new UnitAction(UnitAction.TYPE_MOVE, m);
                    UnitAction m2 = new UnitAction(UnitAction.TYPE_MOVE, (m + 1) % 4);
                    UnitAction m3 = new UnitAction(UnitAction.TYPE_MOVE, (m + 2) % 4);
                    UnitAction m4 = new UnitAction(UnitAction.TYPE_MOVE, (m + 3) % 4);
                    if (gs.isUnitActionAllowed(getUnit(), m1)) {
                        return m1;
                    } else if (gs.isUnitActionAllowed(getUnit(), m2)) {
                        return m2;
                    } else if (gs.isUnitActionAllowed(getUnit(), m3)) {
                        return m3;
                    } else {
                        return gs.isUnitActionAllowed(getUnit(), m4) ? m4 : new UnitAction(UnitAction.TYPE_NONE, 1);
                    }
                }
            }
        }
    }

    public static int[] nextPos(Unit target, GameState gs) {
        UnitAction targetAction = gs.getUnitAction(target);
        if (targetAction != null && targetAction.getType() == 1 && targetAction.getDirection() != -1) {
            int newPosX = target.getX() + UnitAction.DIRECTION_OFFSET_X[targetAction.getDirection()];
            int newPosY = target.getY() + UnitAction.DIRECTION_OFFSET_Y[targetAction.getDirection()];
            return new int[] { newPosX, newPosY };
        } else {
            return new int[] { target.getX(), target.getY() };
        }
    }

    static double dist(Unit ourUnit, Unit closestUnit) {
        int dx = closestUnit.getX() - ourUnit.getX();
        int dy = closestUnit.getY() - ourUnit.getY();
        return Math.sqrt((double) (dx * dx + dy * dy));
    }

    public static double dist(Unit ourUnit, int[] pos2) {
        int dx = pos2[0] - ourUnit.getX();
        int dy = pos2[1] - ourUnit.getY();
        return Math.sqrt((double) (dx * dx + dy * dy));
    }

    public static boolean enemyIsInRangeAttack(Unit ourUnit, Unit closestUnit) {
        return dist(ourUnit, closestUnit) <= (double) ourUnit.getAttackRange();
    }
}
