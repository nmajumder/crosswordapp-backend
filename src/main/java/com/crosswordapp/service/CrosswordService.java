package com.crosswordapp.service;

import com.crosswordapp.exception.BoardUpdateException;
import com.crosswordapp.object.CheckType;
import com.crosswordapp.object.Crossword;
import com.crosswordapp.StaticCrosswordService;
import com.crosswordapp.rep.BoardRep;
import com.crosswordapp.rep.CrosswordRep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CrosswordService {
    static Logger logger = LoggerFactory.getLogger(CrosswordService.class);

    public CrosswordService() { }

    public List<CrosswordRep> findAll() {
        List<CrosswordRep> reps = new ArrayList<>();
        for (Crossword c: StaticCrosswordService.getCrosswordMap().values()) {
            reps.add(new CrosswordRep(c));
        }
        return reps;
    }

    public CrosswordRep findById(UUID id) {
        Crossword c = StaticCrosswordService.getCrossword(id);
        if (c == null) {
            logger.error("Unable to find crossword with id " + id.toString());
            return null;
        }
        return new CrosswordRep(c);
    }

    public void updateById(UUID id, BoardRep board) throws BoardUpdateException {
        Crossword c = StaticCrosswordService.getCrossword(id);
        if (c == null) {
            throw new BoardUpdateException("Crossword with id {} does not exist", id.toString());
        }
        c.getBoard().setSelection(board.selection);
        c.getBoard().setNumSeconds(board.numSeconds);
        boolean success = c.getBoard().setGrid(board.grid);
        if (!success) {
            throw new BoardUpdateException("Updated board is invalid for crossword with id {}", id.toString());
        }
    }

    public BoardRep crosswordIsComplete(UUID id) {
        Crossword c = StaticCrosswordService.getCrossword(id);
        c.getBoard().gridIsSolved();
        return new BoardRep(c.getBoard(), false);
    }

    public BoardRep checkCrossword(UUID id, CheckType type) {
        Crossword c = StaticCrosswordService.getCrossword(id);
        c.getBoard().check(type);
        return new BoardRep(c.getBoard(), false);
    }

    public BoardRep revealCrossword(UUID id, CheckType type) {
        Crossword c = StaticCrosswordService.getCrossword(id);
        c.getBoard().reveal(type);
        return new BoardRep(c.getBoard(), false);
    }

    public BoardRep clearCrossword(UUID id, CheckType type) {
        Crossword c = StaticCrosswordService.getCrossword(id);
        c.getBoard().clear(type);
        return new BoardRep(c.getBoard(), false);
    }
}
