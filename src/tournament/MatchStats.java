package tournament;

import java.util.List;

public class MatchStats {
    private List<Integer> winners;
    private List<Integer> winnersSwitched;
    private List<Integer> cycles;
    private String player1;
    private String player2;
    private long duration;

    public MatchStats(List<Integer> winners, List<Integer> winnersSwitched, List<Integer> cycles, long duration,
            String player1, String player2) {
        this.winners = winners;
        this.cycles = cycles;
        this.duration = duration;
        this.player1 = player1;
        this.player2 = player2;
        this.winnersSwitched = winnersSwitched;
    }

    public List<Integer> getWinners() {
        return winners;
    }

    public List<Integer> getWinnersSwitched() {
        return winnersSwitched;
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