package com.crosswordapp.service;

import com.crosswordapp.dao.BoardDAO;
import com.crosswordapp.exception.BoardException;
import com.crosswordapp.object.CheckType;
import com.crosswordapp.object.Crossword;
import com.crosswordapp.StaticCrosswordService;
import com.crosswordapp.object.Rating;
import com.crosswordapp.rep.BoardRep;
import com.crosswordapp.rep.CrosswordRep;
import com.crosswordapp.rep.RatingsRep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                boardRep = new BoardRep(c.getBoard());
                boardDAO.createBoard(userid, c.getId(), boardRep);
            }
            CrosswordRep crosswordRep = new CrosswordRep(c);
            crosswordRep.board = boardRep;
            reps.add(crosswordRep);
        }
        return reps;
    }

    public List<RatingsRep> getRatings() {
        List<Rating> allRatings = boardDAO.getAllRatings();
        Map<String, RatingsRep> ratingsMap = new HashMap<>();

        for (Rating rating : allRatings) {
            String id = rating.getCrosswordId();
            if (!ratingsMap.containsKey(id)) {
                ratingsMap.put(id, new RatingsRep());
                ratingsMap.get(id).crosswordId = id;
            }
            if (rating.getDifficultyRating() != null && rating.getDifficultyRating() > 0) {
                ratingsMap.get(id).difficultyScore += rating.getDifficultyRating();
                ratingsMap.get(id).numDifficultyRatings++;
            }
            if (rating.getEnjoymentRating() != null && rating.getEnjoymentRating() > 0) {
                ratingsMap.get(id).enjoymentScore += rating.getEnjoymentRating();
                ratingsMap.get(id).numEnjoymentRatings++;
            }
        }
        List<RatingsRep> ratings = new ArrayList<>();
        for (String id : ratingsMap.keySet()) {
            RatingsRep rep = ratingsMap.get(id);
            if (rep.numDifficultyRatings > 0) {
                rep.difficultyScore /= rep.numDifficultyRatings;
            } else {
                rep.difficultyScore = 0;
            }
            if (rep.numEnjoymentRatings > 0) {
                rep.enjoymentScore /= rep.numEnjoymentRatings;
            } else {
                rep.enjoymentScore = 0;
            }
            ratings.add(rep);
        }
        return ratings;
    }

    public BoardRep rateCrossword(String id, String userid, BoardRep board) {
        BoardRep boardRep = boardDAO.getBoard(userid, id);
        if (boardRep == null) {
            throw new BoardException("User {} does not have a crossword with id {}", userid, id);
        }
        boardRep.difficultyRating = board.difficultyRating;
        boardRep.enjoymentRating = board.enjoymentRating;
        boardDAO.updateBoard(userid, id, boardRep);
        return boardRep;
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
        boardRep.completed = c.getBoard().gridIsSolved(boardRep.grid);
        boardDAO.updateBoard(userid, id, boardRep);
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
        boardRep.completed = c.getBoard().gridIsSolved(boardRep.grid);
        boardDAO.updateBoard(userid, id, boardRep);
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
