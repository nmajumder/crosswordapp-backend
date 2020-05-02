package com.crosswordapp.resource;

import com.crosswordapp.exception.BoardUpdateException;
import com.crosswordapp.object.CheckType;
import com.crosswordapp.rep.BoardRep;
import com.crosswordapp.rep.CrosswordRep;
import com.crosswordapp.service.CrosswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:4200" })
@RestController
public class CrosswordResource {
    final static String PATH = "/api/crossword";
    Logger logger = LoggerFactory.getLogger(CrosswordResource.class);

    @Autowired
    private CrosswordService crosswordService;

    @GetMapping(PATH + "/all")
    public List<CrosswordRep> getAllCrosswords() {
        return crosswordService.findAll();
    }

    @GetMapping(PATH + "/{id}/{userid}")
    public CrosswordRep getCrossword(@PathVariable String id, @PathVariable String userid) {
        return crosswordService.findById(UUID.fromString(id));
    }

    @PutMapping(PATH + "/{id}/{userid}/update")
    public void updateCrossword(@RequestBody BoardRep board,
                                 @PathVariable String id, @PathVariable String userid) {
        logger.info("Saving board for crossword {} and user {}", id, userid);
        try {
            crosswordService.updateById(UUID.fromString(id), board);
        } catch (BoardUpdateException e) {
            String error = String.format("Unable to update board with id %s: %s", id, e.getMessage());
            logger.error(error);
        }
    }

    @PutMapping(PATH + "/{id}/{userid}/isComplete")
    public BoardRep crosswordIsComplete(@RequestBody BoardRep board,
                                       @PathVariable String id, @PathVariable String userid) {
        crosswordService.updateById(UUID.fromString(id), board);
        return crosswordService.crosswordIsComplete(UUID.fromString(id));
    }

    @PutMapping(PATH + "/{id}/{userid}/check/square")
    public BoardRep checkSquare(@RequestBody BoardRep board,
                                @PathVariable String id, @PathVariable String userid) {
        logger.info("Checking square for crossword {} and user {}", id, userid);
        crosswordService.updateById(UUID.fromString(id), board);
        return crosswordService.checkCrossword(UUID.fromString(id), CheckType.Square);
    }

    @PutMapping(PATH + "/{id}/{userid}/check/word")
    public BoardRep checkWord(@RequestBody BoardRep board,
                                @PathVariable String id, @PathVariable String userid) {
        logger.info("Checking word for crossword {} and user {}", id, userid);
        crosswordService.updateById(UUID.fromString(id), board);
        return crosswordService.checkCrossword(UUID.fromString(id), CheckType.Word);
    }

    @PutMapping(PATH + "/{id}/{userid}/check/puzzle")
    public BoardRep checkPuzzle(@RequestBody BoardRep board,
                                @PathVariable String id, @PathVariable String userid) {
        logger.info("Checking puzzle for crossword {} and user {}", id, userid);
        crosswordService.updateById(UUID.fromString(id), board);
        return crosswordService.checkCrossword(UUID.fromString(id), CheckType.Puzzle);
    }

    @PutMapping(PATH + "/{id}/{userid}/reveal/square")
    public BoardRep revealSquare(@RequestBody BoardRep board,
                                @PathVariable String id, @PathVariable String userid) {
        logger.info("Revealing square for crossword {} and user {}", id, userid);
        crosswordService.updateById(UUID.fromString(id), board);
        return crosswordService.revealCrossword(UUID.fromString(id), CheckType.Square);
    }

    @PutMapping(PATH + "/{id}/{userid}/reveal/word")
    public BoardRep revealWord(@RequestBody BoardRep board,
                              @PathVariable String id, @PathVariable String userid) {
        logger.info("Revealing word for crossword {} and user {}", id, userid);
        crosswordService.updateById(UUID.fromString(id), board);
        return crosswordService.revealCrossword(UUID.fromString(id), CheckType.Word);
    }

    @PutMapping(PATH + "/{id}/{userid}/reveal/puzzle")
    public BoardRep revealPuzzle(@RequestBody BoardRep board,
                                @PathVariable String id, @PathVariable String userid) {
        logger.info("Revealing puzzle for crossword {} and user {}", id, userid);
        crosswordService.updateById(UUID.fromString(id), board);
        return crosswordService.revealCrossword(UUID.fromString(id), CheckType.Puzzle);
    }

    @PutMapping(PATH + "/{id}/{userid}/clear/word")
    public BoardRep clearWord(@RequestBody BoardRep board,
                               @PathVariable String id, @PathVariable String userid) {
        logger.info("Clearing word for crossword {} and user {}", id, userid);
        crosswordService.updateById(UUID.fromString(id), board);
        return crosswordService.clearCrossword(UUID.fromString(id), CheckType.Word);
    }

    @PutMapping(PATH + "/{id}/{userid}/clear/puzzle")
    public BoardRep clearPuzzle(@RequestBody BoardRep board,
                                 @PathVariable String id, @PathVariable String userid) {
        logger.info("Clearing puzzle for crossword {} and user {}", id, userid);
        crosswordService.updateById(UUID.fromString(id), board);
        return crosswordService.clearCrossword(UUID.fromString(id), CheckType.Puzzle);
    }
}
