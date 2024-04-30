package tournament;

import rts.units.UnitTypeTable;

public class TournamentConfig {
    private String[] maps = {
            "maps/16x16/basesWorkers16x16.xml",
            "maps/GardenOfWar64x64.xml",
            "maps/BroodWar/(4)BloodBath.scmA.xml",
    };
    private int maxCycles = 5000;
    private int updatePeriod = 10;
    private int windowSize = 800;
    private boolean disposeWindow = true;
    private boolean checkAdvantage = true;
    private int maxDuration = 3600000;
    private boolean visible = false;
    private int simulations = 1;
    private int timeBudget = 10;
    private TournamentType type = TournamentType.ROUND_ROBIN;
    private UnitTypeTable utt = new UnitTypeTable();

    public String[] getMaps() {
        return maps;
    }

    public void setMaps(String[] maps) {
        this.maps = maps;
    }

    public int getMaxCycles() {
        return maxCycles;
    }

    public void setMaxCycles(int maxCycles) {
        this.maxCycles = maxCycles;
    }

    public int getUpdatePeriod() {
        return updatePeriod;
    }

    public void setUpdatePeriod(int updatePeriod) {
        this.updatePeriod = updatePeriod;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public boolean isDisposeWindow() {
        return disposeWindow;
    }

    public void setDisposeWindow(boolean disposeWindow) {
        this.disposeWindow = disposeWindow;
    }

    public boolean isCheckAdvantage() {
        return checkAdvantage;
    }

    public void setCheckAdvantage(boolean checkAdvantage) {
        this.checkAdvantage = checkAdvantage;
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(int maxDuration) {
        this.maxDuration = maxDuration;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getSimulations() {
        return simulations;
    }

    public void setSimulations(int simulations) {
        this.simulations = simulations;
    }

    public int getTimeBudget() {
        return timeBudget;
    }

    public void setTimeBudget(int timeBudget) {
        this.timeBudget = timeBudget;
    }

    public TournamentType getType() {
        return type;
    }

    public void setType(TournamentType type) {
        this.type = type;
    }

    public UnitTypeTable getUtt() {
        return utt;
    }
}