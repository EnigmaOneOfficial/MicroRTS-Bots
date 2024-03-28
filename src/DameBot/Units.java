package DameBot;

import java.util.ArrayList;
import java.util.List;

import rts.PhysicalGameState;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

public class Units {
    public UnitType WORKER, LIGHT, HEAVY, RANGED, BASE, BARRACKS;
    public List<Unit> units, workers, light, heavy, ranged, bases, barracks;
    public List<Unit> _units, _workers, _light, _heavy, _ranged, _bases, _barracks;
    public List<Unit> builders = new ArrayList<>();
    public List<Unit> defenders = new ArrayList<>();
    public List<Unit> harvesters = new ArrayList<>();
    public List<Unit> resources;

    public Units(UnitTypeTable unitTypeTable) {
        setUnitTypes(unitTypeTable);
    }

    @SuppressWarnings("unchecked")
    public void Refresh(PhysicalGameState board, int player) {
        resetUnits();
        for (Unit unit : board.getUnits()) {
            if (unit.getPlayer() == player) {
                addUnitToLists(unit, units, bases, barracks, workers, light, heavy, ranged);
            } else if (unit.getPlayer() >= 0) {
                addUnitToLists(unit, _units, _bases, _barracks, _workers, _light, _heavy, _ranged);
            } else {
                resources.add(unit);
            }
        }
    }

    private void resetUnits() {
        units = new ArrayList<>();
        _units = new ArrayList<>();
        bases = new ArrayList<>();
        barracks = new ArrayList<>();
        workers = new ArrayList<>();
        light = new ArrayList<>();
        heavy = new ArrayList<>();
        ranged = new ArrayList<>();
        _bases = new ArrayList<>();
        _barracks = new ArrayList<>();
        _workers = new ArrayList<>();
        _light = new ArrayList<>();
        _heavy = new ArrayList<>();
        _ranged = new ArrayList<>();
        resources = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private void addUnitToLists(Unit unit, List<Unit>... lists) {
        lists[0].add(unit);
        switch (unit.getType().name) {
            case "Base" -> lists[1].add(unit);
            case "Barracks" -> lists[2].add(unit);
            case "Worker" -> lists[3].add(unit);
            case "Light" -> lists[4].add(unit);
            case "Heavy" -> lists[5].add(unit);
            case "Ranged" -> lists[6].add(unit);
        }
    }

    private void setUnitTypes(UnitTypeTable unitTypeTable) {
        for (UnitType unitType : unitTypeTable.getUnitTypes()) {
            switch (unitType.name) {
                case "Worker":
                    WORKER = unitType;
                    break;
                case "Light":
                    LIGHT = unitType;
                    break;
                case "Heavy":
                    HEAVY = unitType;
                    break;
                case "Ranged":
                    RANGED = unitType;
                    break;
                case "Base":
                    BASE = unitType;
                    break;
                case "Barracks":
                    BARRACKS = unitType;
                    break;
            }
        }
    }
}