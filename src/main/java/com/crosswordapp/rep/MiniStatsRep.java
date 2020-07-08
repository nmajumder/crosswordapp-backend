package com.crosswordapp.rep;

import com.crosswordapp.object.MiniCategoryStats;
import java.util.List;

public class MiniStatsRep {
    public String userToken;
    public List<MiniCategoryStats> categoryStats;
    public Integer currentStreak;
    public Integer longestStreak;

    public MiniStatsRep() {}

    public MiniStatsRep(String userToken, List<MiniCategoryStats> categoryStats,
                        Integer currentStreak, Integer longestStreak) {
        this.userToken = userToken;
        this.categoryStats = categoryStats;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
    }
}
