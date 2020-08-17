package com.crosswordapp;

import com.crosswordapp.dao.MinisDAO;
import com.crosswordapp.rep.MiniSolutionRep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MiniBoardCache {
    static Logger logger = LoggerFactory.getLogger(MiniBoardCache.class);

    private static Map<String, MiniSolutionRep> miniMap;
    private static MinisDAO minisDAO;

    @Autowired
    public void setMinisDAO(MinisDAO minisDAO) {
        MiniBoardCache.minisDAO = minisDAO;
    }

    public static void initializeMiniBoardCache() {
        miniMap = new ConcurrentHashMap<>();
    }

    // tries to pull from the static cache map or if not there then the db
    // either way returns NULL if does not exist
    public static MiniSolutionRep getBoardForUser(String userId) {
        if (!miniMap.containsKey(userId)) {
            // if not in static cache map, try to pull from db (then update map)
            // (this could be necessary if the server restarted during the mini solving or something)
            MiniSolutionRep miniSolution = minisDAO.getMiniSolution(userId);
            if (miniSolution != null)
                miniMap.put(userId, miniSolution);
            return miniSolution;
        } else {
            // if in static map, just return the current cached state
            return miniMap.get(userId);
        }
    }

    public static void addOrUpdateBoardForUser(String userId, MiniSolutionRep miniSolution) {
        miniMap.put(userId, miniSolution);
        // in case of server failure or reboot, let's persist the board to db
        writeBoardToDatabase(userId, miniSolution);
    }

    public static void resetBoardForUser(String userId) {
        miniMap.remove(userId);
        // for now do not delete mini board row in db for user in background
        //      only hiccup might be if it gets in a weird state and it thinks this puzzle is being solved
        //          or if we ever want to have multiple servers, one might think it started a game on another one
        //      but it will probably be ok, since each time the mini app is mounted a blank grid pops up
        //minisDAO.deleteMini(userId);
    }

    @Async
    private static void writeBoardToDatabase(String userId, MiniSolutionRep miniSolution) {
        if (minisDAO.getMiniSolution(userId) == null) {
            minisDAO.createMini(userId, miniSolution);
        } else {
            minisDAO.updateMini(userId, miniSolution);
        }
    }

}
