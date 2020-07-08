package com.crosswordapp.resource;

import com.crosswordapp.StaticMiniGridService;
import com.crosswordapp.generation.GenerationApp;
import com.crosswordapp.object.*;
import com.crosswordapp.rep.*;
import com.crosswordapp.service.MiniService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
public class MiniResource {
    final static String PATH = "/api/mini";
    Logger logger = LoggerFactory.getLogger(MiniResource.class);

    @Autowired
    private MiniService miniService;

    @GetMapping(PATH + "/{userid}/{size}/{difficulty}/generate")
    public MiniRep generateMini(@PathVariable String userid, @PathVariable Integer size,
                                @PathVariable MiniDifficulty difficulty) {
        Mini mini = miniService.generateMini(size, difficulty);
        if (mini != null) {
            miniService.recordMiniStarted(userid,
                    new MiniSolutionRep(mini.getBoard().getSolution(), size, difficulty, false, false));
        }

        return new MiniRep(mini);
    }

    @GetMapping(PATH + "/{size}/generate/test")
    public List<Integer> testGenerations(@PathVariable Integer size) {
        List<Integer> failuresList = new ArrayList<>();
        List<MiniGridTemplate> grids = StaticMiniGridService.getMiniGridMap().get(size);
        for (int i = 0; i < grids.size(); i++) {
            logger.info("On grid " + (i+1) + " of " + grids.size() + " for size: " + size);
            failuresList.add(0);
            boolean anyFailures = false;
            // test this grid 5 times initially
            for (int j = 0; j < 5; j++) {
                if (GenerationApp.generateBoard(grids.get(i).getGrid()) == null) {
                    anyFailures = true;
                    failuresList.set(i, failuresList.get(i) + 1);
                }
            }
            // if any of the 5 fail, run it again 15 more times
            // this eliminates extra work done for grids that will never/rarely fail
            if (anyFailures) {
                for (int j = 0; j < 15; j++) {
                    if (GenerationApp.generateBoard(grids.get(i).getGrid()) == null) {
                        failuresList.set(i, failuresList.get(i) + 1);
                    }
                }
            }
        }
        logger.info(failuresList.toString());
        return failuresList;
    }

    @PutMapping(PATH + "/{userid}/isComplete")
    public BoardRep miniIsComplete(@PathVariable String userid, @RequestBody BoardRep board) {
        return miniService.miniIsComplete(userid, board);
    }

    @PutMapping(PATH + "/{userid}/check/square")
    public BoardRep checkSquare(@PathVariable String userid, @RequestBody BoardRep board) {
        return miniService.checkMini(userid, CheckType.Square, board);
    }

    @PutMapping(PATH + "/{userid}/check/word")
    public BoardRep checkWord(@PathVariable String userid, @RequestBody BoardRep board) {
        return miniService.checkMini(userid, CheckType.Word, board);
    }

    @PutMapping(PATH + "/{userid}/check/puzzle")
    public BoardRep checkPuzzle(@PathVariable String userid, @RequestBody BoardRep board) {
        return miniService.checkMini(userid, CheckType.Puzzle, board);
    }

    @PutMapping(PATH + "/{userid}/reveal/square")
    public BoardRep revealSquare(@PathVariable String userid, @RequestBody BoardRep board) {
        return miniService.revealMini(userid, CheckType.Square, board);
    }

    @PutMapping(PATH + "/{userid}/reveal/word")
    public BoardRep revealWord(@PathVariable String userid, @RequestBody BoardRep board) {
        return miniService.revealMini(userid, CheckType.Word, board);
    }

    @PutMapping(PATH + "/{userid}/reveal/puzzle")
    public BoardRep revealPuzzle(@PathVariable String userid, @RequestBody BoardRep board) {
        return miniService.revealMini(userid, CheckType.Puzzle, board);
    }

    /* STATS AND LEADERBOARD RELATED ENDPOINTS */

    @GetMapping(PATH + "/{userid}/stats")
    public MiniStatsRep getMiniStats(@PathVariable String userid) {
        return miniService.getMiniStats(userid);
    }

    @GetMapping(PATH + "/{userid}/leaderboard")
    public LeaderboardRep getLeaderboard(@PathVariable String userid) {
        return miniService.getLeaderboard(userid);
    }
}
