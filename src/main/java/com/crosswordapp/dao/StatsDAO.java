package com.crosswordapp.dao;

import com.crosswordapp.object.BoardSquare;
import com.crosswordapp.object.MiniDifficulty;
import com.crosswordapp.rep.BoardRep;
import com.crosswordapp.rep.MiniCompletedRep;
import com.crosswordapp.rep.MiniStatsRep;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.jni.Local;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Component
public class StatsDAO {
    Logger logger = LoggerFactory.getLogger(StatsDAO.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${postgres.url}")
    private String DB_URL;
    @Value("${postgres.user}")
    private String DB_USER;
    @Value("${postgres.password}")
    private String DB_PASS;

    private final static String USER_ID_COL = "user_id";
    private final static String COMPLETED_COL = "completed_games";
    private final static String STARTED_COL = "started_games";
    private final static String BEST_TIMES_COL = "best_times";
    private final static String BEST_DATES_COL = "best_dates";
    private final static String AVERAGES_COL = "average_times";
    private final static String CHECKED_COL = "check_percents";
    private final static String REVEALED_COL = "reveal_percents";
    private final static String ACTIVITY_COL = "activity_over_time";
    private final static String LONG_STREAK_COL = "longest_streak";

    private final static String GET_STATS =
            "SELECT " + getFieldList(false, false, "ALL") + " FROM ministats"
                + " WHERE " + getFieldList(true, false, USER_ID_COL);
    private final static String GET_ALL_STATS =
            "SELECT " + getFieldList(false, false, "ALL") + " FROM ministats";
    private final static String CREATE_STATS =
            "INSERT INTO ministats (" + getFieldList(false, false, "ALL")
                    + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private final static String STARTED_GAME_UPDATE =
            "UPDATE ministats SET " + getFieldList(true, true, STARTED_COL)
                    + " WHERE " + getFieldList(true, false, USER_ID_COL);
    private final static String COMPLETED_GAME_UPDATE =
            "UPDATE ministats SET "
                    + getFieldList(true, true, COMPLETED_COL, BEST_TIMES_COL,
                    BEST_DATES_COL, AVERAGES_COL, CHECKED_COL, REVEALED_COL)
                    + ", " + getFieldList(true, false, ACTIVITY_COL, LONG_STREAK_COL)
                    + " WHERE " + getFieldList(true, false, USER_ID_COL);

    public void createStats(String userId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(CREATE_STATS)) {
            Integer[] initCol = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            Date[] initDateCol = {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null};
            ps.setString(1, userId);
            ps.setArray(2, conn.createArrayOf("integer", initCol));
            ps.setArray(3, conn.createArrayOf("integer", initCol));
            ps.setArray(4, conn.createArrayOf("integer", initCol));
            ps.setArray(5, conn.createArrayOf("date", initDateCol));
            ps.setArray(6, conn.createArrayOf("decimal", initCol));
            ps.setArray(7, conn.createArrayOf("decimal", initCol));
            ps.setArray(8, conn.createArrayOf("decimal", initCol));
            ps.setObject(9, getJsonActivityMap(new HashMap<>()));
            ps.setInt(10, 0);
            ps.execute();
            logger.info("Successfully created mini stats for user " + userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create mini stats for user " + userId, e);
        }
    }

    public void updateMiniStarted(String userid, Integer size, MiniDifficulty difficulty) {
        int arrInd = (size - 5) * 3;
        if (difficulty.equals(MiniDifficulty.Moderate)) arrInd = arrInd + 1;
        else if (difficulty.equals(MiniDifficulty.Hard)) arrInd = arrInd + 2;

        MiniStatsRep curStats = getStats(userid);
        int numStarted = curStats.startedGames[arrInd];
        numStarted++;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(STARTED_GAME_UPDATE)) {
            ps.setInt(1, arrInd);
            ps.setInt(2, numStarted);
            ps.setString(3, userid);
            ps.execute();
            logger.info("Successfully updated game start stat for user " + userid);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update stats on game start for user " + userid, e);
        }
    }

    public void updateMiniCompleted(String userid, MiniCompletedRep miniRep) {
        int arrInd = (miniRep.size - 5) * 3;
        if (miniRep.difficulty.equals(MiniDifficulty.Moderate)) arrInd = arrInd + 1;
        else if (miniRep.difficulty.equals(MiniDifficulty.Hard)) arrInd = arrInd + 2;

        MiniStatsRep curStats = getStats(userid);

        int numGames = curStats.completedGames[arrInd];
        int bestTime = curStats.bestTimes[arrInd];
        Date bestDate = curStats.bestDates[arrInd];
        float avgTime = curStats.averageTimes[arrInd].floatValue();
        float checkedPct = curStats.checkPercents[arrInd].floatValue();
        float revealPct = curStats.revealPercents[arrInd].floatValue();

        Map<String, List<Integer>> activityMap = curStats.activityOverTime;
        LocalDate today = LocalDate.now(ZoneId.systemDefault());

        if (miniRep.seconds < bestTime || bestTime == 0) {
            if (!miniRep.revealed && !miniRep.checked && miniRep.seconds > 0) {
                bestTime = miniRep.seconds;
                bestDate = Date.valueOf(today);
            }
        }
        avgTime = ((avgTime * numGames) + miniRep.seconds) / (numGames + 1);
        checkedPct = miniRep.checked ?
                ((checkedPct * numGames) + 1) / (numGames + 1) :
                (checkedPct * numGames) / (numGames + 1);
        revealPct = miniRep.revealed ?
                ((revealPct * numGames) + 1) / (numGames + 1) :
                (revealPct * numGames) / (numGames + 1);
        numGames++;

        if (activityMap == null) activityMap = new HashMap<>();

        int curStreak = getCurrentStreak(activityMap);
        int longStreak = curStats.longestStreak == null ? getLongestStreak(activityMap) : curStats.longestStreak;

        if (!activityMap.containsKey(today.toString()) || activityMap.get(today.toString()) == null) {
            curStreak++;
            if (curStreak > longStreak) {
                longStreak = curStreak;
            }
            activityMap.put(today.toString(), Arrays.asList(new Integer[15]));
        }
        if (activityMap.get(today.toString()).get(arrInd) == null) {
            activityMap.get(today.toString()).set(arrInd, 1);
        } else {
            int numGamesToday = activityMap.get(today.toString()).get(arrInd);
            logger.debug("The user has played " + numGamesToday + " today in this category up to this point");
            activityMap.get(today.toString()).set(arrInd, numGamesToday + 1);
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(COMPLETED_GAME_UPDATE)) {
            ps.setInt(1, arrInd);
            ps.setInt(2, numGames);
            ps.setInt(3, arrInd);
            ps.setInt(4, bestTime);
            ps.setInt(5, arrInd);
            ps.setObject(6, bestDate);
            ps.setInt(7, arrInd);
            ps.setFloat(8, avgTime);
            ps.setInt(9, arrInd);
            ps.setFloat(10, checkedPct);
            ps.setInt(11, arrInd);
            ps.setFloat(12, revealPct);
            ps.setObject(13, getJsonActivityMap(activityMap));
            ps.setInt(14, longStreak);
            ps.setString(15, userid);
            ps.execute();
            logger.info("Successfully updated game complete stats for user " + userid);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update stats on game complete for user " + userid, e);
        }
    }

    public MiniStatsRep getStats(String userId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(GET_STATS)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String userToken = rs.getString(USER_ID_COL);
                    Integer[] completedGames = (Integer[]) rs.getArray(COMPLETED_COL).getArray();
                    Integer[] startedGames = (Integer[]) rs.getArray(STARTED_COL).getArray();
                    Integer[] bestTimes = (Integer[]) rs.getArray(BEST_TIMES_COL).getArray();
                    Date[] bestDates = (Date[]) rs.getArray(BEST_DATES_COL).getArray();
                    BigDecimal[] averageTimes = (BigDecimal[]) rs.getArray(AVERAGES_COL).getArray();
                    BigDecimal[] checkPercents = (BigDecimal[]) rs.getArray(CHECKED_COL).getArray();
                    BigDecimal[] revealPercents = (BigDecimal[]) rs.getArray(REVEALED_COL).getArray();
                    String activityMapStr = rs.getString(ACTIVITY_COL);
                    Map<String, List<Integer>> activityMap = getActivityMapFromJson(activityMapStr);
                    Integer curStreak = getCurrentStreak(activityMap);
                    Integer longStreak = rs.getInt(LONG_STREAK_COL);
                    if (longStreak == 0) longStreak = getLongestStreak(activityMap);
                    MiniStatsRep miniRep =
                            new MiniStatsRep(userToken, completedGames, startedGames, bestTimes, bestDates,
                                    averageTimes, checkPercents, revealPercents, activityMap, curStreak, longStreak);
                    logger.info("Successfully retrieved mini stats for user " + userId);
                    return miniRep;
                }
                logger.error("Unable to find mini stats for user " + userId);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve mini stats for user " + userId, e);
        }
    }

    public List<MiniStatsRep> getAllStats() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(GET_ALL_STATS);
             ResultSet rs = ps.executeQuery()) {
            List<MiniStatsRep> statsReps = new ArrayList<>();
            while (rs.next()) {
                String userid = rs.getString(USER_ID_COL);
                Integer[] completedGames = (Integer[]) rs.getArray(COMPLETED_COL).getArray();
                Integer[] startedGames = (Integer[]) rs.getArray(STARTED_COL).getArray();
                Integer[] bestTimes = (Integer[]) rs.getArray(BEST_TIMES_COL).getArray();
                Date[] bestDates = (Date[]) rs.getArray(BEST_DATES_COL).getArray();
                BigDecimal[] averageTimes = (BigDecimal[]) rs.getArray(AVERAGES_COL).getArray();
                BigDecimal[] checkPercents = (BigDecimal[]) rs.getArray(CHECKED_COL).getArray();
                BigDecimal[] revealPercents = (BigDecimal[]) rs.getArray(REVEALED_COL).getArray();
                String activityMapStr = rs.getString(ACTIVITY_COL);
                Map<String, List<Integer>> activityMap = getActivityMapFromJson(activityMapStr);
                Integer curStreak = getCurrentStreak(activityMap);
                Integer longStreak = rs.getInt(LONG_STREAK_COL);
                if (longStreak == 0) longStreak = getLongestStreak(activityMap);
                MiniStatsRep statsRep =
                        new MiniStatsRep(userid, completedGames, startedGames, bestTimes, bestDates, averageTimes,
                                checkPercents, revealPercents, activityMap, curStreak, longStreak);
                logger.debug("Successfully retrieved mini stats for user " + userid);
                statsReps.add(statsRep);
            }
            logger.info("Successfully retrieved mini stats for " + statsReps.size() + " users");
            return statsReps;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve mini stats for all users", e);
        }
    }

    private static PGobject getJsonActivityMap(Map<String, List<Integer>> activityMap) {
        try {
            String mapStr = mapper.writeValueAsString(activityMap);
            PGobject mapObj = new PGobject();
            mapObj.setType("json");
            mapObj.setValue(mapStr);
            return mapObj;
        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize activity map to json", e);
        }
    }

    private static Map<String, List<Integer>> getActivityMapFromJson(String json) {
        try {
            Map<String, List<Integer>> activityMap = mapper.readValue(json,
                    new TypeReference<Map<String, List<Integer>>>(){});
            return activityMap;
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize json into activity map: " + json, e);
        }
    }

    private int getCurrentStreak(Map<String, List<Integer>> activityMap) {
        LocalDate day = LocalDate.now(ZoneId.systemDefault());
        int streak = 0;
        if (activityMap.containsKey(day.toString())) {
            streak++;
        }
        day = day.minusDays(1);
        while (activityMap.containsKey(day.toString())) {
            streak++;
            day = day.minusDays(1);
        }
        return streak;
    }

    private int getLongestStreak(Map<String, List<Integer>> activityMap) {
        LocalDate minDate = null;
        for (String dateStr : activityMap.keySet()) {
            LocalDate thisDate = LocalDate.parse(dateStr);
            if (minDate == null || thisDate.isBefore(minDate)) {
                minDate = thisDate;
            }
        }
        if (minDate == null) return 0;

        int tempStreak = 0;
        int longStreak = 0;
        while (minDate.isBefore(LocalDate.now(ZoneId.systemDefault()))) {
            if (activityMap.containsKey(minDate.toString())) {
                tempStreak++;
            } else {
                if (tempStreak > longStreak) {
                    longStreak = tempStreak;
                }
                tempStreak = 0;
            }
            minDate = minDate.plusDays(1);
        }
        if (tempStreak > longStreak) {
            longStreak = tempStreak;
        }
        return longStreak;
    }

    private static String getFieldList(boolean setter, boolean arrayBrackets, String... args) {
        if (args.length == 0) {
            return "";
        }
        if (args.length == 1 && args[0].equals("ALL")) {
            args = new String[]{USER_ID_COL, COMPLETED_COL, STARTED_COL, BEST_TIMES_COL, BEST_DATES_COL,
                    AVERAGES_COL, CHECKED_COL, REVEALED_COL, ACTIVITY_COL, LONG_STREAK_COL};
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (arrayBrackets) {
                sb.append("[?]");
            }
            if (setter) {
                sb.append(" = ?");
            }
            if (i < args.length-1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
