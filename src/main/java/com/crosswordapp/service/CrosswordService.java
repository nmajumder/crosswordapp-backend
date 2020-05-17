package com.crosswordapp.service;

import com.crosswordapp.dao.BoardDAO;
import com.crosswordapp.exception.BoardException;
import com.crosswordapp.object.CheckType;
import com.crosswordapp.object.Crossword;
import com.crosswordapp.StaticCrosswordService;
import com.crosswordapp.rep.BoardRep;
import com.crosswordapp.rep.CrosswordRep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CrosswordService {
    static Logger logger = LoggerFactory.getLogger(CrosswordService.class);

    @Autowired
    private BoardDAO boardDAO;

    public List<CrosswordRep> findAll(String userid) {
        List<CrosswordRep> reps = new ArrayList<>();
        for (Crossword c: StaticCrosswordService.getCrosswordMap().values()) {
            BoardRep boardRep = boardDAO.getBoard(userid, c.getId());
            if (boardRep == null) {
                boardRep = new BoardRep(c.getBoard(), false);
                boardDAO.createBoard(userid, c.getId(), boardRep);
            }
            CrosswordRep crosswordRep = new CrosswordRep(c);
            crosswordRep.board = boardRep;
            reps.add(crosswordRep);
        }
        return reps;
    }

    public void updateById(String id, String userid, BoardRep board) throws BoardException {
        Crossword c = StaticCrosswordService.getCrossword(id);
        if (c == null) {
            throw new BoardException("Crossword with id {} does not exist", id);
        }
        boardDAO.updateBoard(userid, id, board);
    }

    public BoardRep crosswordIsComplete(String id, String userid) {
        BoardRep boardRep = boardDAO.getBoard(userid, id);
        if (boardRep == null) {
            throw new BoardException("User {} does not have a crossword with id {}", userid, id);
        }
        Crossword c = StaticCrosswordService.getCrossword(id);
        c.getBoard().gridIsSolved(boardRep.grid);
        return boardRep;
    }

    public BoardRep checkCrossword(String id, String userid, CheckType type) {
        BoardRep boardRep = boardDAO.getBoard(userid, id);
        if (boardRep == null) {
            throw new BoardException("User {} does not have a crossword with id {}", userid, id);
        }
        Crossword c = StaticCrosswordService.getCrossword(id);
        c.getBoard().check(type, boardRep.grid, boardRep.selection);
        return boardRep;
    }

    public BoardRep revealCrossword(String id, String userid, CheckType type) {
        BoardRep boardRep = boardDAO.getBoard(userid, id);
        if (boardRep == null) {
            throw new BoardException("User {} does not have a crossword with id {}", userid, id);
        }
        Crossword c = StaticCrosswordService.getCrossword(id);
        c.getBoard().reveal(type, boardRep.grid, boardRep.selection);
        return boardRep;
    }

    public BoardRep resetCrossword(String id, String userid) {
        BoardRep boardRep = boardDAO.getBoard(userid, id);
        if (boardRep == null) {
            throw new BoardException("User {} does not have a crossword with id {}", userid, id);
        }
        Crossword c = StaticCrosswordService.getCrossword(id);
        c.getBoard().reset(boardRep.grid, boardRep.selection);
        return boardRep;
    }
}
