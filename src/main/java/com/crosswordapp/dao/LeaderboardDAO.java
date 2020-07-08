package com.crosswordapp.dao;

import com.crosswordapp.object.MiniDifficulty;
import com.crosswordapp.rep.LeaderboardDataRep;
import com.crosswordapp.rep.LeaderboardRep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class LeaderboardDAO {
    Logger logger = LoggerFactory.getLogger(LeaderboardDAO.class);

    @Value("${postgres.url}")
    private String DB_URL;
    @Value("${postgres.user}")
    private String DB_USER;
    @Value("${postgres.password}")
    private String DB_PASS;

    private final static String USERNAME_COL = "usr";
    private final static String DATA_COL = "val";

    private final static String GET_COMPLETED_GAMES_LEADERBOARD =
            "SELECT u.username usr, t.completed val FROM total_stats t JOIN users u ON t.user_id = u.token "
                    + "WHERE t.completed > 0 ORDER BY val DESC LIMIT 10";
    private final static String GET_COMPLETION_PERCENT_LEADERBOARD =
            "SELECT u.username usr, cast(t.completed as decimal) / t.started val "
                    + "FROM total_stats t JOIN users u ON t.user_id = u.token "
                    + "WHERE t.completed > 0 AND t.started > 0 "
                    + "ORDER BY val DESC LIMIT 10";
    private final static String GET_REVEAL_PERCENT_LEADERBOARD =
            "SELECT u.username usr, cast(t.revealed as decimal) / t.completed val "
                    + "FROM total_stats t JOIN users u ON t.user_id = u.token "
                    + "WHERE t.completed > 0 ORDER BY val ASC LIMIT 10";
    private final static String GET_CURRENT_STREAK_LEADERBOARD =
            "SELECT u.username usr, t.current_streak val FROM total_stats t JOIN users u ON t.user_id = u.token "
                    + "WHERE t.current_streak > 0 AND t.last_completed > current_date - interval '2 days' "
                    + "ORDER BY val DESC LIMIT 10";
    private final static String GET_LONGEST_STREAK_LEADERBOARD =
            "SELECT u.username usr, t.longest_streak val FROM total_stats t JOIN users u ON t.user_id = u.token "
                    + "WHERE t.longest_streak > 0 ORDER BY val DESC LIMIT 10";
    private final static String GET_BEST_TIMES_PER_CATEGORY_LEADERBOARD =
            "SELECT u.username usr, c.best_time val FROM category_stats c JOIN users u ON c.user_id = u.token "
                    + "WHERE c.size = ? AND c.difficulty = ? AND c.best_time > 0 ORDER BY val ASC LIMIT 10";

    public LeaderboardRep getLeaderboard() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            LeaderboardRep leaderboard = new LeaderboardRep();
            leaderboard.mostGamesCompleted = getTotalDataLeaderboard(conn, GET_COMPLETED_GAMES_LEADERBOARD);
            leaderboard.bestCompletionPercent = getTotalDataLeaderboard(conn, GET_COMPLETION_PERCENT_LEADERBOARD);
            leaderboard.lowestRevealPercent = getTotalDataLeaderboard(conn, GET_REVEAL_PERCENT_LEADERBOARD);
            leaderboard.currentStreak = getTotalDataLeaderboard(conn, GET_CURRENT_STREAK_LEADERBOARD);
            leaderboard.longestStreak = getTotalDataLeaderboard(conn, GET_LONGEST_STREAK_LEADERBOARD);
            List<List<LeaderboardDataRep>> bestTimesPerCategory = new ArrayList<>();
            for (int size = 5; size <= 9; size++) {
                for (MiniDifficulty difficulty : MiniDifficulty.values()) {
                    bestTimesPerCategory.add(getCategoryDataLeaderboard(conn, size, difficulty));
                }
            }
            leaderboard.bestTimesPerCategory = bestTimesPerCategory;
            logger.info("Successfully retrieved entire leaderboard");
            return leaderboard;
        } catch (SQLException e) {
            throw new RuntimeException("Unable to establish db connection", e);
        }
    }

    private List<LeaderboardDataRep> getTotalDataLeaderboard(Connection conn, String query) {
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            List<LeaderboardDataRep> leaderboard = new ArrayList<>();
            while (rs.next()) {
                String user = rs.getString(USERNAME_COL);
                float data = rs.getFloat(DATA_COL);
                leaderboard.add(new LeaderboardDataRep(user, data));
            }
            return leaderboard;
        } catch (SQLException e) {
            throw new RuntimeException("Unable to get completed games leaderboard", e);
        }
    }

    private List<LeaderboardDataRep> getCategoryDataLeaderboard(Connection conn, int size, MiniDifficulty difficulty) {
        try (PreparedStatement ps = conn.prepareStatement(GET_BEST_TIMES_PER_CATEGORY_LEADERBOARD)) {
            ps.setInt(1, size);
            ps.setString(2, difficulty.name());
            try (ResultSet rs = ps.executeQuery()) {
                List<LeaderboardDataRep> leaderboard = new ArrayList<>();
                while (rs.next()) {
                    String user = rs.getString(USERNAME_COL);
                    float data = rs.getInt(DATA_COL);
                    leaderboard.add(new LeaderboardDataRep(user, data));
                }
                return leaderboard;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to get category best times leaderboard", e);
        }
    }
}
