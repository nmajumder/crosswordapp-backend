package com.crosswordapp.object;

import java.sql.Date;
import java.util.Map;

public class MiniCategoryStats {
    public int gridSize;
    public MiniDifficulty difficulty;
    public int completed;
    public int started;
    public int revealed;
    public int checked;
    public int bestTime;
    public Date bestDate;
    public float averageTime;
    public Map<String, Integer> activityMap;

    public MiniCategoryStats() {}

    public MiniCategoryStats(int gridSize, MiniDifficulty difficulty, int completed, int started,
                             int revealed, int checked, int bestTime, Date bestDate, float averageTime,
                             Map<String, Integer> activityMap) {
        this.gridSize = gridSize;
        this.difficulty = difficulty;
        this.completed = completed;
        this.started = started;
        this.revealed = revealed;
        this.checked = checked;
        this.bestTime = bestTime;
        this.bestDate = bestDate;
        this.averageTime = averageTime;
        this.activityMap = activityMap;
    }
}
