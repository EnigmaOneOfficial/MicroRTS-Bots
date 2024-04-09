package CustomUnitClasses;

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

        double dx = target.getX() - getUnit().getX();
        double dy = target.getY() - getUnit().getY();
        double d = Math.hypot(dx, dy);
        UnitAction action = new UnitAction(UnitAction.TYPE_ATTACK_LOCATION, target.getX(), target.getY());
        if (d <= getUnit().getAttackRange() && gs.isUnitActionAllowed(getUnit(),
                action)) {
            return action;
        } else {
            UnitAction move = pf.findPathToPositionInRange(getUnit(),
                    target.getX() + target.getY() * gs.getPhysicalGameState().getWidth(), getUnit().getAttackRange(),
                    gs,
                    ru);
            if (move != null && gs.isUnitActionAllowed(getUnit(), move))
                return move;
            return null;
        }
    }
}
