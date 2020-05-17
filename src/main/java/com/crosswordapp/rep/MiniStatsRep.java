package com.crosswordapp.rep;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class MiniStatsRep {
    public Integer[] completedGames;
    public Integer[] startedGames;
    public Integer[] bestTimes;
    public Date[] bestDates;
    public BigDecimal[] averageTimes;
    public BigDecimal[] checkPercents;
    public BigDecimal[] revealPercents;
    public Map<String, List<Integer>> activityOverTime;

    public MiniStatsRep() {}

    public MiniStatsRep(Integer[] completedGames, Integer[] startedGames, Integer[] bestTimes,
                        Date[] bestDates, BigDecimal[] averageTimes, BigDecimal[] checkPercents,
                        BigDecimal[] revealPercents, Map<String, List<Integer>> activityOverTime) {
        this.completedGames = completedGames;
        this.startedGames = startedGames;
        this.bestTimes = bestTimes;
        this.bestDates = bestDates;
        this.averageTimes = averageTimes;
        this.checkPercents = checkPercents;
        this.revealPercents = revealPercents;
        this.activityOverTime = activityOverTime;
    }
}
