package com.crosswordapp;

import com.crosswordapp.rep.MiniSolutionRep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MiniBoardCache {
    static Logger logger = LoggerFactory.getLogger(MiniBoardCache.class);

    private static MiniBoardCache instance = null;

    private Map<String, MiniSolutionRep> miniMap;

    public static void initializeMiniBoardCache() {
        if (instance != null) {
            logger.error("Mini board cache is already initialized, cannot re-initialize");
        } else {
            instance = new MiniBoardCache();
        }
    }

    public MiniBoardCache() {
        miniMap = new ConcurrentHashMap<>();
    }

    public static MiniSolutionRep getBoardForUser(String userId) {
        return instance.miniMap.get(userId);
    }

    public static void addOrUpdateBoardForUser(String userId, MiniSolutionRep miniSolution) {
        instance.miniMap.put(userId, miniSolution);
    }

    public static void resetBoardForUser(String userId) {
        instance.miniMap.remove(userId);
    }
}
