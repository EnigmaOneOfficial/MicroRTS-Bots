package tournament;

import java.util.ArrayList;
import java.util.List;

public class MatchStats {
    private List<Integer> results;
    private List<Integer> resultsSwitched;
    private List<Integer> cycles;
    private String player1;
    private String player2;
    private long duration;

    public MatchStats(String player1, String player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.results = new ArrayList<>();
        this.resultsSwitched = new ArrayList<>();
        this.cycles = new ArrayList<>();
    }

    public void addResult(int winner, boolean switched) {
        if (switched) {
            resultsSwitched.add(winner);
        } else {
            results.add(winner);
        }
    }

    public void addCycles(int cyclesCount) {
        cycles.add(cyclesCount);
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public List<Integer> getResults() {
        return results;
    }

    public List<Integer> getResultsSwitched() {
        return resultsSwitched;
    }

    public List<Integer> getCycles() {
        return cycles;
    }

    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public long getDuration() {
        return duration;
    }
}