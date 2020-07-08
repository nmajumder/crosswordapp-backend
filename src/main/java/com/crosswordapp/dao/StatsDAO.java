package com.crosswordapp.dao;

import com.crosswordapp.object.MiniCategoryStats;
import com.crosswordapp.object.MiniDifficulty;
import com.crosswordapp.object.MiniTotalStats;
import com.crosswordapp.rep.MiniCompletedRep;
import com.crosswordapp.rep.MiniStatsRep;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.SQLException;
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

    // both category & total stat cols
    private final static String USER_ID_COL = "user_id";
    private final static String COMPLETED_COL = "completed";
    private final static String STARTED_COL = "started";
    private final static String REVEALED_COL = "revealed";

    // category stat cols
    private final static String SIZE_COL = "size";
    private final static String DIFFICULTY_COL = "difficulty";
    private final static String BEST_TIME_COL = "best_time";
    private final static String BEST_DATE_COL = "best_date";
    private final static String AVERAGE_COL = "average_time";
    private final static String CHECKED_COL = "checked";
    private final static String ACTIVITY_COL = "activity_map";

    // total stat cols
    private final static String CUR_STREAK_COL = "current_streak";
    private final static String LAST_COMPLETED_COL = "last_completed";
    private final static String LONG_STREAK_COL = "longest_streak";

    private final static String GET_ALL_CATEGORY_STATS =
            "SELECT " + getFieldList(false, SIZE_COL, DIFFICULTY_COL, COMPLETED_COL,
                    STARTED_COL, BEST_TIME_COL, BEST_DATE_COL, AVERAGE_COL, CHECKED_COL, REVEALED_COL, ACTIVITY_COL)
                    + " FROM category_stats WHERE " + getFieldList(true, USER_ID_COL);
    private final static String GET_CATEGORY_STATS =
            "SELECT " + getFieldList(false, COMPLETED_COL, STARTED_COL, BEST_TIME_COL, BEST_DATE_COL, AVERAGE_COL,
                    CHECKED_COL, REVEALED_COL, ACTIVITY_COL) + " FROM category_stats WHERE "
                    + getFieldList(true, USER_ID_COL) + " AND " + getFieldList(true, SIZE_COL) + " AND "
                    + getFieldList(true, DIFFICULTY_COL);
    private final static String GET_CATEGORY_STATS_FOR_UPDATE =
            GET_CATEGORY_STATS + " FOR UPDATE";
    private final static String GET_TOTAL_STATS =
            "SELECT " + getFieldList(false, CUR_STREAK_COL, LAST_COMPLETED_COL, LONG_STREAK_COL)
                    + " FROM total_stats WHERE " + getFieldList(true, USER_ID_COL);
    private final static String GET_TOTAL_STATS_FOR_UPDATE =
            "SELECT " + getFieldList(false, COMPLETED_COL, STARTED_COL, REVEALED_COL, CUR_STREAK_COL, LAST_COMPLETED_COL, LONG_STREAK_COL)
                    + " FROM total_stats WHERE " + getFieldList(true, USER_ID_COL) + " FOR UPDATE";
    private final static String CREATE_CATEGORY_STATS =
            "INSERT INTO category_stats (" + getFieldList(false, USER_ID_COL, SIZE_COL, DIFFICULTY_COL,
                    COMPLETED_COL, STARTED_COL, BEST_TIME_COL, BEST_DATE_COL, AVERAGE_COL, CHECKED_COL, REVEALED_COL, ACTIVITY_COL)
                    + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private final static String CREATE_TOTAL_STATS =
            "INSERT INTO total_stats (" + getFieldList(false, USER_ID_COL, COMPLETED_COL, STARTED_COL,
                    REVEALED_COL, CUR_STREAK_COL, LAST_COMPLETED_COL, LONG_STREAK_COL)
                    + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
    private final static String UPDATE_CATEGORY_STARTED =
            "UPDATE category_stats SET " + STARTED_COL + " = " + STARTED_COL + " + 1"
                    + " WHERE " + getFieldList(true, USER_ID_COL) + " AND "
                    + getFieldList(true, SIZE_COL) + " AND "
                    + getFieldList(true, DIFFICULTY_COL);
    private final static String UPDATE_TOTAL_STARTED =
            "UPDATE total_stats SET " + STARTED_COL + " = " + STARTED_COL + " + 1"
                    + " WHERE " + getFieldList(true, USER_ID_COL);
    private final static String UPDATE_CATEGORY_COMPLETED =
            "UPDATE category_stats SET "
                    + getFieldList(true, COMPLETED_COL, BEST_TIME_COL,
                    BEST_DATE_COL, AVERAGE_COL, CHECKED_COL, REVEALED_COL, ACTIVITY_COL)
                    + " WHERE " + getFieldList(true, USER_ID_COL) + " AND "
                    + getFieldList(true, SIZE_COL) + " AND "
                    + getFieldList(true, DIFFICULTY_COL);
    private final static String UPDATE_TOTAL_COMPLETED =
            "UPDATE total_stats SET "
                    + getFieldList(true, COMPLETED_COL, REVEALED_COL, CUR_STREAK_COL, LAST_COMPLETED_COL, LONG_STREAK_COL)
                    + " WHERE " + getFieldList(true, USER_ID_COL);

    /*
        GET STATS METHODS
     */

    private MiniCategoryStats getCategoryStatsForUpdate(String userId, int size, MiniDifficulty difficulty, Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(GET_CATEGORY_STATS_FOR_UPDATE)) {
            ps.setString(1, userId);
            ps.setInt(2, size);
            ps.setString(3, difficulty.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int completed = rs.getInt(COMPLETED_COL);
                    int started = rs.getInt(STARTED_COL);
                    int bestTime = rs.getInt(BEST_TIME_COL);
                    Date bestDate = rs.getDate(BEST_DATE_COL);
                    float avgTime = rs.getFloat(AVERAGE_COL);
                    int checked = rs.getInt(CHECKED_COL);
                    int revealed = rs.getInt(REVEALED_COL);
                    String activityMapJson  = rs.getString(ACTIVITY_COL);
                    Map<String, Integer> activityMap = getActivityMapFromJson(activityMapJson);
                    logger.debug("Successfully retrieved category (" + size + ", " + difficulty.name() + ") stats for user " + userId);
                    return new MiniCategoryStats(size, difficulty, completed, started,
                            revealed, checked, bestTime, bestDate, avgTime, activityMap);
                }
            }
            logger.error("Failed to retrieve category (" + size + ", " + difficulty.name() + ") stats for user " + userId);
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve category (" + size + ", " + difficulty.name() + ") stats for user " + userId, e);
        }
    }

    private List<MiniCategoryStats> getAllCategoryStats(String userId, Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(GET_ALL_CATEGORY_STATS)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<MiniCategoryStats> categoryStatsList = new ArrayList<>();
                while (rs.next()) {
                    int size = rs.getInt(SIZE_COL);
                    MiniDifficulty difficulty = MiniDifficulty.valueOf(rs.getString(DIFFICULTY_COL));
                    int completed = rs.getInt(COMPLETED_COL);
                    int started = rs.getInt(STARTED_COL);
                    int bestTime = rs.getInt(BEST_TIME_COL);
                    Date bestDate = rs.getDate(BEST_DATE_COL);
                    float avgTime = rs.getFloat(AVERAGE_COL);
                    int checked = rs.getInt(CHECKED_COL);
                    int revealed = rs.getInt(REVEALED_COL);
                    String activityMapJson  = rs.getString(ACTIVITY_COL);
                    Map<String, Integer> activityMap = getActivityMapFromJson(activityMapJson);
                    categoryStatsList.add(
                            new MiniCategoryStats(size, difficulty, completed, started,
                                    revealed, checked, bestTime, bestDate, avgTime, activityMap));
                }
                logger.debug("Successfully retrieved all category stats for user " + userId);
                return categoryStatsList;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve all category stats for user " + userId, e);
        }
    }

    private MiniTotalStats getTotalStatsForUpdate(String userId, Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(GET_TOTAL_STATS_FOR_UPDATE)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int completed = rs.getInt(COMPLETED_COL);
                    int started = rs.getInt(STARTED_COL);
                    int revealed = rs.getInt(REVEALED_COL);
                    int curStreak = rs.getInt(CUR_STREAK_COL);
                    Date lastCompleted = rs.getDate(LAST_COMPLETED_COL);
                    int longStreak = rs.getInt(LONG_STREAK_COL);
                    logger.debug("Successfully retrieved total stats for user " + userId);
                    return new MiniTotalStats(completed, started, revealed, curStreak, lastCompleted, longStreak);
                }
            }
            logger.error("Unable to get total stats for user " + userId);
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve total stats for user " + userId, e);
        }
    }

    public MiniStatsRep getMiniStats(String userId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            List<MiniCategoryStats> categoryStatsList = getAllCategoryStats(userId, conn);
            try (PreparedStatement ps = conn.prepareStatement(GET_TOTAL_STATS)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int currentStreak = rs.getInt(CUR_STREAK_COL);
                        Date lastCompleted = rs.getDate(LAST_COMPLETED_COL);
                        int longestStreak = rs.getInt(LONG_STREAK_COL);
                        // check if last completed game is today or yesterday -- if not, streak is 0
                        Date cutoffDate = new Date(System.currentTimeMillis() - (1000 * 3600 * 24 * 2));
                        if (lastCompleted == null || !lastCompleted.after(cutoffDate)) {
                            currentStreak = 0;
                        }
                        MiniStatsRep miniStats = new MiniStatsRep(userId, categoryStatsList, currentStreak, longestStreak);
                        logger.info("Successfully retrieved all mini stats for user " + userId);
                        return miniStats;
                    }
                }
            }
            logger.error("Unable to find mini stats for user " + userId);
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Unable to establish db connection", e);
        }
    }

    /*
        INITIALIZE STATS METHODS
     */

    private void createCategoryStats(String userId, int size, MiniDifficulty difficulty, Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(CREATE_CATEGORY_STATS)) {
            ps.setString(1, userId);
            ps.setInt(2, size);
            ps.setString(3, difficulty.name());
            ps.setInt(4, 0);
            ps.setInt(5, 0);
            ps.setInt(6, -1);
            ps.setDate(7, null);
            ps.setFloat(8, 0);
            ps.setInt(9, 0);
            ps.setInt(10, 0);
            ps.setObject(11, getJsonActivityMap(new HashMap<>()));
            ps.execute();
            logger.debug("Successfully created category (" + size + ", " + difficulty.name() + ") stats for user " + userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create category (" + size + ", " + difficulty.name() + ") stats for user " + userId, e);
        }
    }

    private void createTotalStats(String userId, Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(CREATE_TOTAL_STATS)) {
            ps.setString(1, userId);
            ps.setInt(2, 0);
            ps.setInt(3, 0);
            ps.setInt(4, 0);
            ps.setInt(5, 0);
            ps.setDate(6, null);
            ps.setInt(7, 0);
            ps.execute();
            logger.debug("Successfully created total stats for user " + userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create total stats for user " + userId, e);
        }
    }

    public void initializeAllStatsForUser(String userId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            conn.setAutoCommit(false);
            createTotalStats(userId, conn);
            for (int size = 5; size <= 9; size++) {
                for (MiniDifficulty difficulty : MiniDifficulty.values()) {
                    createCategoryStats(userId, size, difficulty, conn);
                }
            }
            conn.commit();
            conn.setAutoCommit(true);
            logger.info("Successfully initialized all stat rows for user " + userId);
        } catch(SQLException e) {
            throw new RuntimeException("Failed to create db connection");
        }
    }

    /*
        UPDATE STATS METHODS FOR STARTED GAMES
     */

    public void updateMiniStarted(String userId, Integer size, MiniDifficulty difficulty) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(UPDATE_CATEGORY_STARTED)) {
                ps.setString(1, userId);
                ps.setInt(2, size);
                ps.setString(3, difficulty.name());
                int updated = ps.executeUpdate();
                if (updated != 1) {
                    logger.error("Found no rows to update in category stats for user " + userId);
                    throw new RuntimeException("Failed to update mini stats on game start for user " + userId);
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(UPDATE_TOTAL_STARTED)) {
                ps.setString(1, userId);
                int updated = ps.executeUpdate();
                if (updated != 1) {
                    logger.error("Found no rows to update in total stats for user " + userId);
                    throw new RuntimeException("Failed to update mini stats on game start for user " + userId);
                }
            }
            conn.commit();
            conn.setAutoCommit(true);
            logger.debug("Successfully updated mini stats on game start for user " + userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update mini stats on game start for user " + userId, e);
        }
    }

    private void updateCategoryCompleted(String userId, MiniCategoryStats categoryStats, Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_CATEGORY_COMPLETED)) {
            ps.setInt(1, categoryStats.completed);
            ps.setInt(2, categoryStats.bestTime);
            ps.setDate(3, categoryStats.bestDate);
            ps.setFloat(4, categoryStats.averageTime);
            ps.setInt(5, categoryStats.checked);
            ps.setInt(6, categoryStats.revealed);
            ps.setObject(7, getJsonActivityMap(categoryStats.activityMap));
            ps.setString(8, userId);
            ps.setInt(9, categoryStats.gridSize);
            ps.setString(10, categoryStats.difficulty.name());
            int updated = ps.executeUpdate();
            if (updated != 1) {
                logger.error("Found no rows to update in category stats for user " + userId);
                throw new RuntimeException("Failed to update category stats on game completion for user " + userId);
            }
            logger.debug("Successfully updated category stats on game completion for user " + userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update category stats for user " + userId, e);
        }
    }

    private void updateTotalCompleted(String userId, MiniTotalStats totalStats, Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_TOTAL_COMPLETED)) {
            ps.setInt(1, totalStats.completed);
            ps.setInt(2, totalStats.revealed);
            ps.setInt(3, totalStats.curStreak);
            ps.setDate(4, totalStats.lastCompleted);
            ps.setInt(5, totalStats.longStreak);
            ps.setString(6, userId);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                logger.error("Found no rows to update in total stats for user " + userId);
                throw new RuntimeException("Failed to update total stats on game completion for user " + userId);
            }
            logger.debug("Successfully updated total stats on game completion for user " + userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update total stats for user " + userId, e);
        }
    }

    public void updateMiniCompleted(String userId, MiniCompletedRep miniRep) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            conn.setAutoCommit(false);

            // handle RETRIEVE and UPDATE for category stats
            MiniCategoryStats categoryStats = getCategoryStatsForUpdate(userId, miniRep.size, miniRep.difficulty, conn);
            int completed = categoryStats.completed;
            int bestTime = categoryStats.bestTime;
            Date bestDate = categoryStats.bestDate;
            float avgTime = categoryStats.averageTime;
            int checked = categoryStats.checked;
            int revealed = categoryStats.revealed;
            Map<String, Integer> activityMap = categoryStats.activityMap;

            LocalDate today = LocalDate.now(ZoneId.systemDefault());

            completed++;
            if (miniRep.seconds < bestTime || bestTime == -1) {
                if (!miniRep.revealed && !miniRep.checked && miniRep.seconds > 0) {
                    bestTime = miniRep.seconds;
                    bestDate = Date.valueOf(today);
                }
            }
            avgTime = ((avgTime * (completed-1)) + miniRep.seconds) / completed;
            if (miniRep.checked) checked++;
            if (miniRep.revealed) revealed++;

            if (activityMap == null) activityMap = new HashMap<>();
            Integer gamesToday = activityMap.get(today.toString());
            gamesToday = gamesToday == null ? 1 : gamesToday + 1;
            activityMap.put(today.toString(), gamesToday);

            categoryStats.completed = completed;
            categoryStats.revealed = revealed;
            categoryStats.checked = checked;
            categoryStats.bestTime = bestTime;
            categoryStats.bestDate = bestDate;
            categoryStats.averageTime = avgTime;
            categoryStats.activityMap = activityMap;
            updateCategoryCompleted(userId, categoryStats, conn);

            // handle RETRIEVE and UPDATE for total stats
            MiniTotalStats totalStats = getTotalStatsForUpdate(userId, conn);
            totalStats.completed++;
            if (miniRep.revealed) {
                totalStats.revealed++;
            }
            int curStreak = totalStats.curStreak;
            Date lastCompleted = totalStats.lastCompleted;
            Date twoDaysAgo = new Date(System.currentTimeMillis() - (1000*3600*24*2));
            if (lastCompleted == null || !lastCompleted.after(twoDaysAgo)) {
                // if no prev games OR last completed game was at least 2 days ago, then reset streak
                curStreak = 1;
            } else {
                Date yesterday = new Date(System.currentTimeMillis() - (1000*3600*24));
                if (!lastCompleted.after(yesterday)) {
                    // then last completed a game yesterday, so increment
                    curStreak++;
                } else {
                    // then already completed a game today, so do nothing to streak
                }
            }
            if (curStreak > totalStats.longStreak) {
                totalStats.longStreak = curStreak;
            }
            totalStats.curStreak = curStreak;
            totalStats.lastCompleted = new Date(System.currentTimeMillis());
            updateTotalCompleted(userId, totalStats, conn);

            // finally commit these changes
            conn.commit();
            conn.setAutoCommit(true);
            logger.info("Successfully updated mini stats on game completion for user " + userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update mini stats on game completion for user " + userId, e);
        }
    }

    private static PGobject getJsonActivityMap(Map<String, Integer> activityMap) {
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

    private static Map<String, Integer> getActivityMapFromJson(String json) {
        try {
            Map<String, Integer> activityMap = mapper.readValue(json,
                    new TypeReference<Map<String, Integer>>(){});
            return activityMap;
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize json into activity map: " + json, e);
        }
    }

    private static String getFieldList(boolean setter, String... args) {
        if (args.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
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
