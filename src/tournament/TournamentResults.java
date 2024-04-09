package tournament;

import java.util.*;

public class TournamentResults {
    private Map<String, Map<String, Map<String, List<MatchResult>>>> results;
    private Map<String, Map<String, Map<String, List<MatchResult>>>> resultsSwitched;
    private List<MatchStats> matchStats;

    public TournamentResults() {
        results = new HashMap<>();
        resultsSwitched = new HashMap<>();
        matchStats = new ArrayList<>();
    }

    public void addResult(String map, String p1, String p2, int winner) {
        results.computeIfAbsent(map, k -> new HashMap<>())
                .computeIfAbsent(p1, k -> new HashMap<>())
                .computeIfAbsent(p2, k -> new ArrayList<>())
                .add(winner == 0 ? MatchResult.WIN : winner == 1 ? MatchResult.LOSE : MatchResult.DRAW);

        MatchResult opposite = winner == 0 ? MatchResult.LOSE : winner == 1 ? MatchResult.WIN : MatchResult.DRAW;
        results.computeIfAbsent(map, k -> new HashMap<>())
                .computeIfAbsent(p2, k -> new HashMap<>())
                .computeIfAbsent(p1, k -> new ArrayList<>())
                .add(opposite);
    }

    public void addResultSwitched(String map, String p1, String p2, int winner) {
        resultsSwitched.computeIfAbsent(map, k -> new HashMap<>())
                .computeIfAbsent(p1, k -> new HashMap<>())
                .computeIfAbsent(p2, k -> new ArrayList<>())
                .add(winner == 0 ? MatchResult.WIN : winner == 1 ? MatchResult.LOSE : MatchResult.DRAW);

        MatchResult opposite = winner == 0 ? MatchResult.LOSE : winner == 1 ? MatchResult.WIN : MatchResult.DRAW;
        resultsSwitched.computeIfAbsent(map, k -> new HashMap<>())
                .computeIfAbsent(p2, k -> new HashMap<>())
                .computeIfAbsent(p1, k -> new ArrayList<>())
                .add(opposite);
    }

    public void addMatchStats(MatchStats stats) {
        matchStats.add(stats);
    }

    public Set<String> getMaps() {
        Set<String> maps = new HashSet<>(results.keySet());
        maps.addAll(resultsSwitched.keySet());
        return maps;
    }

    public Map<String, Map<String, List<MatchResult>>> getResults(String map) {
        return results.getOrDefault(map, new HashMap<>());
    }

    public Map<String, Map<String, List<MatchResult>>> getResultsSwitched(String map) {
        return resultsSwitched.getOrDefault(map, new HashMap<>());
    }

    public List<MatchStats> getMatchStats() {
        return matchStats;
    }

    public int determineWinner(String map, String p1, String p2) {
        List<MatchResult> resultsP1vsP2 = results.getOrDefault(map, new HashMap<>())
                .getOrDefault(p1, new HashMap<>())
                .getOrDefault(p2, new ArrayList<>());

        int p1Wins = (int) resultsP1vsP2.stream().filter(r -> r == MatchResult.WIN).count();
        int p2Wins = (int) resultsP1vsP2.stream().filter(r -> r == MatchResult.LOSE).count();

        if (p1Wins > p2Wins) {
            return 0;
        } else if (p2Wins > p1Wins) {
            return 1;
        } else {
            return -1;
        }
    }
}