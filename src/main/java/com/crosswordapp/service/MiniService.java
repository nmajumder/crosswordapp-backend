package com.crosswordapp.service;

import com.crosswordapp.*;
import com.crosswordapp.dao.LeaderboardDAO;
import com.crosswordapp.dao.StatsDAO;
import com.crosswordapp.dao.UserDAO;
import com.crosswordapp.generation.GenerationApp;
import com.crosswordapp.generation.Grid;
import com.crosswordapp.object.*;
import com.crosswordapp.rep.*;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MiniService {
    static Logger logger = LoggerFactory.getLogger(MiniService.class);

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private StatsDAO statsDAO;

    @Autowired
    private LeaderboardDAO leaderboardDAO;

    public MiniService() {}

    public Mini generateMini(Integer size, MiniDifficulty difficulty) {
        MiniGridTemplate gridTemplate = StaticMiniGridService.getMiniGridTemplate(size, difficulty);
        if (gridTemplate == null) {
            return null;
        }
        Grid grid = GenerationApp.generateBoard(gridTemplate.getGrid());
        if (grid == null) {
            logger.error("Unable to generate a board within the time limit, must try again");
            return null;
        }

        List<String> gridRows = new ArrayList<>();
        for (int r = 0; r < grid.height; r++) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < grid.width; c++) {
                sb.append(grid.slots.get(r).get(c).val);
            }
            gridRows.add(sb.toString());
        }
        Board board = new Board(gridRows, Symmetry.DIAGONAL);
        Mini mini = new Mini(difficulty, board, getClueManager(board, difficulty));
        return mini;
    }

    private ClueManager getClueManager(Board board, MiniDifficulty difficulty) {
        List<Clue> acrossClues = new ArrayList<>();
        List<Clue> downClues = new ArrayList<>();
        for (int r = 0; r < board.getHeight(); r++) {
            for (int c = 0; c < board.getWidth(); c++) {
                BoardSquare bs = board.getBoardSquareAtIndex(r,c);
                if (bs.getValue().equals("_")) continue;
                if (bs.getAcrossWordIndex() == 0) {
                    Integer acrossNum = bs.getAcrossClueNum();
                    String acrossWord = board.getAnswerForClueNum(acrossNum, ClueType.Across);
                    Clue clue = new Clue(getMiniClueForWord(acrossWord, difficulty).getText(), ClueType.Across,
                            acrossNum, new Pair<>(r, c), acrossWord.length());
                    acrossClues.add(clue);
                }
                if (bs.getDownWordIndex() == 0) {
                    Integer downNum = bs.getDownClueNum();
                    String downWord = board.getAnswerForClueNum(downNum, ClueType.Down);
                    Clue clue = new Clue(getMiniClueForWord(downWord, difficulty).getText(), ClueType.Down,
                            downNum, new Pair<>(r, c), downWord.length());
                    downClues.add(clue);
                }
            }
        }
        return new ClueManager(acrossClues, downClues);
    }

    private MiniClue getMiniClueForWord(String word, MiniDifficulty difficulty) {
        List<MiniClue> choices = StaticMiniClueService.getWordClueMap().get(word);
        List<MiniClue> easyChoices = new ArrayList<>();
        List<MiniClue> moderateChoices = new ArrayList<>();
        List<MiniClue> hardChoices = new ArrayList<>();
        for (MiniClue choice: choices) {
            if (choice.getDay().getDifficulty().equals(MiniDifficulty.Standard)) {
                easyChoices.add(choice);
            } else if (choice.getDay().getDifficulty().equals(MiniDifficulty.Difficult)){
                moderateChoices.add(choice);
            } else {
                hardChoices.add(choice);
            }
        }
        // in order to slightly shake up the results, there will only be a 90% chance of getting the difficulty requested
        // EASY     => EASY (90%), MODERATE (8%), HARD (2%)
        // MODERATE => EASY (5%), MODERATE (90%), HARD (5%)
        // HARD     => EASY (2%), MODERATE (8%), HARD (90%)
        double r = Math.random();
        if (r < .02) {
            if (difficulty.equals(MiniDifficulty.Standard)) difficulty = MiniDifficulty.Expert;
            else if (difficulty.equals(MiniDifficulty.Expert)) difficulty = MiniDifficulty.Standard;
        } else if (r < .05) {
            if (difficulty.equals(MiniDifficulty.Difficult)) difficulty = MiniDifficulty.Standard;
        } else if (r < .1) {
            if (difficulty.equals(MiniDifficulty.Standard) || difficulty.equals(MiniDifficulty.Expert))
                difficulty = MiniDifficulty.Difficult;
            else difficulty = MiniDifficulty.Expert;
        }

        // we will try to pull a clue from the difficulty list specified, but may have to go to another if empty
        // EASY empty? -----> try MODERATE then try HARD
        // MODERATE empty? -> 50% chance of EASY or HARD
        // HARD empty? -----> try MODERATE then EASY
        if (difficulty.equals(MiniDifficulty.Standard)) {
            if (easyChoices.size() > 0) {
                return getMiniClueFromList(easyChoices);
            } else if (moderateChoices.size() > 0) {
                return getMiniClueFromList(moderateChoices);
            } else {
                return getMiniClueFromList(hardChoices);
            }
        } else if (difficulty.equals(MiniDifficulty.Difficult)) {
            if (moderateChoices.size() > 0) {
                return getMiniClueFromList(moderateChoices);
            } else {
                r = Math.random();
                if ((r < .5 && hardChoices.size() > 0) || (r >= .5 && easyChoices.size() == 0)) {
                    return getMiniClueFromList(hardChoices);
                } else {
                    return getMiniClueFromList(easyChoices);
                }
            }
        } else {
            if (hardChoices.size() > 0) {
                return getMiniClueFromList(hardChoices);
            } else if (moderateChoices.size() > 0) {
                return getMiniClueFromList(moderateChoices);
            } else {
                return getMiniClueFromList(easyChoices);
            }
        }
    }

    private MiniClue getMiniClueFromList(List<MiniClue> choices) {
        if (choices.size() == 0) return null;

        int rand = (int) Math.floor(Math.random() * choices.size());
        return choices.get(rand);
    }

    public BoardRep checkMini(String userId, CheckType type, BoardRep boardRep) {
        MiniSolutionRep miniSolution = MiniBoardCache.getBoardForUser(userId);
        if (miniSolution == null) {
            logger.error("Cannot check mini, no current mini exists for user: " + userId);
            return null;
        }
        Board board = new Board(miniSolution.solution);
        board.check(type, boardRep.grid, boardRep.selection);
        miniSolution.checked = true;
        MiniBoardCache.addOrUpdateBoardForUser(userId, miniSolution);
        return boardRep;
    }

    public BoardRep revealMini(String userId, CheckType type, BoardRep boardRep) {
        MiniSolutionRep miniSolution = MiniBoardCache.getBoardForUser(userId);
        if (miniSolution == null) {
            logger.error("Cannot reveal mini, no current mini exists for user: " + userId);
            return null;
        }
        Board board = new Board(miniSolution.solution);
        board.reveal(type, boardRep.grid, boardRep.selection);
        miniSolution.revealed = true;
        MiniBoardCache.addOrUpdateBoardForUser(userId, miniSolution);
        return boardRep;
    }

    public BoardRep miniIsComplete(String userId, BoardRep boardRep) {
        MiniSolutionRep miniSolution = MiniBoardCache.getBoardForUser(userId);
        if (miniSolution == null) {
            logger.error("Cannot determine completeness of mini, no current mini exists for user: " + userId);
            return null;
        }
        Board board = new Board(miniSolution.solution);
        boolean solved = board.gridIsSolved(boardRep.grid);
        if (solved) {
            statsDAO.updateMiniCompleted(userId,
                    new MiniCompletedRep(miniSolution.size, miniSolution.difficulty, boardRep.numSeconds,
                            miniSolution.checked, miniSolution.revealed));
            MiniBoardCache.resetBoardForUser(userId);
            LeaderboardCache.markLeaderboardChanged();
            boardRep.completed = true;
        }
        return boardRep;
    }

    public void recordMiniStarted(String userToken, MiniSolutionRep miniSolution) {
        if (userDAO.validateToken(userToken) != null) {
            statsDAO.updateMiniStarted(userToken, miniSolution.size, miniSolution.difficulty);
            MiniBoardCache.addOrUpdateBoardForUser(userToken, miniSolution);
            LeaderboardCache.markLeaderboardChanged();
            logger.debug("User " + userToken + " began a mini puzzle");
        } else {
            logger.error("Unable to log start of mini puzzle, user does not exist: " + userToken);
        }
    }

    public MiniStatsRep getMiniStats(String userToken) {
        if (userDAO.validateToken(userToken) != null) {
            return statsDAO.getMiniStats(userToken);
        } else {
            logger.error("Cannot get stats, this user does not exist: " + userToken);
            return null;
        }
    }

    public LeaderboardRep getLeaderboard(String userToken) {
        return LeaderboardCache.getLeaderboard();
    }
}
