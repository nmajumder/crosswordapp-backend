package com.crosswordapp.object;

import java.sql.Date;

public class MiniTotalStats {
    public int completed;
    public int started;
    public int revealed;
    public int curStreak;
    public Date lastCompleted;
    public int longStreak;

    public MiniTotalStats () {}

    public MiniTotalStats(int completed, int started, int revealed,
                          int curStreak, Date lastCompleted, int longStreak) {
        this.completed = completed;
        this.started = started;
        this.revealed = revealed;
        this.curStreak = curStreak;
        this.lastCompleted = lastCompleted;
        this.longStreak = longStreak;
    }
}
