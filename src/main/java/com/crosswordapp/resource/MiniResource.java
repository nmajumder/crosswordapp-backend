package com.crosswordapp.resource;

import com.crosswordapp.StaticMiniGridService;
import com.crosswordapp.generation.GenerationApp;
import com.crosswordapp.object.MiniDifficulty;
import com.crosswordapp.object.MiniGridTemplate;
import com.crosswordapp.rep.MiniCompletedRep;
import com.crosswordapp.rep.MiniRep;
import com.crosswordapp.rep.MiniStatsRep;
import com.crosswordapp.service.MiniService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:4200" })
@RestController
public class MiniResource {
    final static String PATH = "/api/mini";
    Logger logger = LoggerFactory.getLogger(MiniResource.class);

    @Autowired
    private MiniService miniService;

    @GetMapping(PATH + "/{userid}/{size}/{difficulty}/generate")
    public MiniRep generateMini(@PathVariable String userid, @PathVariable Integer size,
                                @PathVariable MiniDifficulty difficulty) {
        miniService.recordMiniStarted(userid, size, difficulty);
        return miniService.generateMini(size, difficulty);
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

    @PostMapping(PATH + "/{userid}/completed")
    public MiniStatsRep miniCompleted(@PathVariable String userid, @RequestBody MiniCompletedRep mini) {
        return miniService.recordMiniStats(userid, mini);
    }

    @GetMapping(PATH + "/{userid}/stats")
    public MiniStatsRep getMiniStats(@PathVariable String userid) {
        return miniService.getMiniStats(userid);
    }
}
