package com.crosswordapp.rep;

import java.util.List;

public class LeaderboardRep {
    public MiniStatsRep userStats;
    public List<LeaderboardDataRep> mostGamesCompleted;
    public List<LeaderboardDataRep> bestCompletionPercent;
    public List<LeaderboardDataRep> lowestRevealPercent;
    public List<LeaderboardDataRep> currentStreak;
    public List<LeaderboardDataRep> longestStreak;
    public List<List<LeaderboardDataRep>> bestTimesPerCategory;

    public LeaderboardRep () {}


    public LeaderboardRep(MiniStatsRep userStats, List<LeaderboardDataRep> mostGamesCompleted,
                          List<LeaderboardDataRep> bestCompletionPercent, List<LeaderboardDataRep> lowestRevealPercent,
                          List<LeaderboardDataRep> currentStreak, List<LeaderboardDataRep> longestStreak,
                          List<List<LeaderboardDataRep>> bestTimesPerCategory) {
        this.userStats = userStats;
        this.mostGamesCompleted = mostGamesCompleted;
        this.bestCompletionPercent = bestCompletionPercent;
        this.lowestRevealPercent = lowestRevealPercent;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.bestTimesPerCategory = bestTimesPerCategory;
    }
}
