package com.crosswordapp;

import com.crosswordapp.dao.LeaderboardDAO;
import com.crosswordapp.rep.LeaderboardRep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LeaderboardCache {
    static Logger logger = LoggerFactory.getLogger(LeaderboardCache.class);

    private static LeaderboardCache instance = null;

    private LeaderboardRep leaderboard;

    @Autowired
    LeaderboardDAO leaderboardDAO;

    private boolean shouldUpdate;

    public static void initializeLeaderboardCache() {
        if (instance != null) {
            logger.error("Leaderboard cache is already initialized, cannot re-initialize");
        } else {
            instance = new LeaderboardCache();
        }
    }

    public LeaderboardCache() {
        shouldUpdate = true;
    }

    // run a leaderboard update IF NECESSARY every 5 seconds
    @Scheduled(fixedRate = 5000, initialDelay = 3000)
    private void updateLeaderboard() {
        if (instance.shouldUpdate) {
            instance.shouldUpdate = false;
            logger.info("Updating leaderboard cache in the background");
            instance.leaderboard = leaderboardDAO.getLeaderboard();
        } else {
            logger.debug("Nothing has changed, so no need to update leaderboard this time");
        }
    }

    // run a leaderboard update each early morning to refresh streaks that have lapsed
    @Scheduled(cron = "0 0 4 * * *")
    private void dailyLeaderboardRefresh() {
        instance.shouldUpdate = true;
    }

    public static void markLeaderboardChanged() {
        instance.shouldUpdate = true;
    }

    public static LeaderboardRep getLeaderboard() {
        return instance.leaderboard;
    }
}
