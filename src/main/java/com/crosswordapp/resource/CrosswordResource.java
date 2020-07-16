package com.crosswordapp.resource;

import com.crosswordapp.exception.BoardException;
import com.crosswordapp.object.CheckType;
import com.crosswordapp.rep.BoardRep;
import com.crosswordapp.rep.CrosswordRep;
import com.crosswordapp.rep.RatingsRep;
import com.crosswordapp.service.CrosswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
public class CrosswordResource {
    final static String PATH = "/api/crossword";
    Logger logger = LoggerFactory.getLogger(CrosswordResource.class);

    @Autowired
    private CrosswordService crosswordService;

    @GetMapping(PATH + "/{userid}/all")
    public List<CrosswordRep> getAllCrosswords(@PathVariable String userid) {
        return crosswordService.findAll(userid);
    }

    @GetMapping(PATH + "/ratings")
    public List<RatingsRep> getAllRatings() {
        return crosswordService.getRatings();
    }

    @PostMapping(PATH + "/{id}/{userid}/rate")
    public BoardRep rateCrossword(@RequestBody BoardRep board,
                                  @PathVariable String id, @PathVariable String userid) {
        return crosswordService.rateCrossword(id, userid, board);
    }

    @PutMapping(PATH + "/{id}/{userid}/update")
    public void updateCrossword(@RequestBody BoardRep board,
                                 @PathVariable String id, @PathVariable String userid) {
        logger.info("Saving board for crossword {} and user {}", id, userid);
        try {
            crosswordService.updateById(id, userid, board);
        } catch (BoardException e) {
            String error = String.format("Unable to update board with id %s for user %s: %s", id, userid, e.getMessage());
            logger.error(error);
        }
    }

    @PutMapping(PATH + "/{id}/{userid}/isComplete")
    public BoardRep crosswordIsComplete(@RequestBody BoardRep board,
                                       @PathVariable String id, @PathVariable String userid) {
        return crosswordService.crosswordIsComplete(id, userid, board);
    }

    @PutMapping(PATH + "/{id}/{userid}/check/square")
    public BoardRep checkSquare(@RequestBody BoardRep board,
                                @PathVariable String id, @PathVariable String userid) {
        logger.info("Checking square for crossword {} and user {}", id, userid);
        return crosswordService.checkCrossword(id, userid, board, CheckType.Square);
    }

    @PutMapping(PATH + "/{id}/{userid}/check/word")
    public BoardRep checkWord(@RequestBody BoardRep board,
                                @PathVariable String id, @PathVariable String userid) {
        logger.info("Checking word for crossword {} and user {}", id, userid);
        return crosswordService.checkCrossword(id, userid, board, CheckType.Word);
    }

    @PutMapping(PATH + "/{id}/{userid}/check/puzzle")
    public BoardRep checkPuzzle(@RequestBody BoardRep board,
                                @PathVariable String id, @PathVariable String userid) {
        logger.info("Checking puzzle for crossword {} and user {}", id, userid);
        return crosswordService.checkCrossword(id, userid, board, CheckType.Puzzle);
    }

    @PutMapping(PATH + "/{id}/{userid}/reveal/square")
    public BoardRep revealSquare(@RequestBody BoardRep board,
                                @PathVariable String id, @PathVariable String userid) {
        logger.info("Revealing square for crossword {} and user {}", id, userid);
        return crosswordService.revealCrossword(id, userid, board, CheckType.Square);
    }

    @PutMapping(PATH + "/{id}/{userid}/reveal/word")
    public BoardRep revealWord(@RequestBody BoardRep board,
                              @PathVariable String id, @PathVariable String userid) {
        logger.info("Revealing word for crossword {} and user {}", id, userid);
        return crosswordService.revealCrossword(id, userid, board, CheckType.Word);
    }

    @PutMapping(PATH + "/{id}/{userid}/reveal/puzzle")
    public BoardRep revealPuzzle(@RequestBody BoardRep board,
                                @PathVariable String id, @PathVariable String userid) {
        logger.info("Revealing puzzle for crossword {} and user {}", id, userid);
        return crosswordService.revealCrossword(id, userid, board, CheckType.Puzzle);
    }

    @PutMapping(PATH + "/{id}/{userid}/reset")
    public BoardRep resetPuzzle(@RequestBody BoardRep board,
                                 @PathVariable String id, @PathVariable String userid) {
        logger.info("Clearing puzzle for crossword {} and user {}", id, userid);
        return crosswordService.resetCrossword(id, userid, board);
    }
}
