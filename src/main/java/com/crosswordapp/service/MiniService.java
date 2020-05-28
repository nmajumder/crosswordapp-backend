package com.crosswordapp.service;

import com.crosswordapp.ClueManager;
import com.crosswordapp.StaticMiniClueService;
import com.crosswordapp.StaticMiniGridService;
import com.crosswordapp.dao.MinisDAO;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class MiniService {
    static Logger logger = LoggerFactory.getLogger(MiniService.class);

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private StatsDAO statsDAO;

    @Autowired
    private MinisDAO minisDAO;

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
            if (choice.getDay().getDifficulty().equals(MiniDifficulty.Easy)) {
                easyChoices.add(choice);
            } else if (choice.getDay().getDifficulty().equals(MiniDifficulty.Moderate)){
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
            if (difficulty.equals(MiniDifficulty.Easy)) difficulty = MiniDifficulty.Hard;
            else if (difficulty.equals(MiniDifficulty.Hard)) difficulty = MiniDifficulty.Easy;
        } else if (r < .05) {
            if (difficulty.equals(MiniDifficulty.Moderate)) difficulty = MiniDifficulty.Easy;
        } else if (r < .1) {
            if (difficulty.equals(MiniDifficulty.Easy) || difficulty.equals(MiniDifficulty.Hard))
                difficulty = MiniDifficulty.Moderate;
            else difficulty = MiniDifficulty.Hard;
        }

        // we will try to pull a clue from the difficulty list specified, but may have to go to another if empty
        // EASY empty? -----> try MODERATE then try HARD
        // MODERATE empty? -> 50% chance of EASY or HARD
        // HARD empty? -----> try MODERATE then EASY
        if (difficulty.equals(MiniDifficulty.Easy)) {
            if (easyChoices.size() > 0) {
                return getMiniClueFromList(easyChoices);
            } else if (moderateChoices.size() > 0) {
                return getMiniClueFromList(moderateChoices);
            } else {
                return getMiniClueFromList(hardChoices);
            }
        } else if (difficulty.equals(MiniDifficulty.Moderate)) {
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
        MiniSolutionRep miniSolution = minisDAO.getMiniSolution(userId);
        if (miniSolution == null) {
            logger.error("Cannot check mini, no current mini exists for user: " + userId);
            return null;
        }
        Board board = new Board(miniSolution.solution);
        board.check(type, boardRep.grid, boardRep.selection);
        miniSolution.checked = true;
        minisDAO.updateMini(userId, miniSolution);
        return boardRep;
    }

    public BoardRep revealMini(String userId, CheckType type, BoardRep boardRep) {
        MiniSolutionRep miniSolution = minisDAO.getMiniSolution(userId);
        if (miniSolution == null) {
            logger.error("Cannot reveal mini, no current mini exists for user: " + userId);
            return null;
        }
        Board board = new Board(miniSolution.solution);
        board.reveal(type, boardRep.grid, boardRep.selection);
        miniSolution.revealed = true;
        minisDAO.updateMini(userId, miniSolution);
        return boardRep;
    }

    public BoardRep miniIsComplete(String userId, BoardRep boardRep) {
        MiniSolutionRep miniSolution = minisDAO.getMiniSolution(userId);
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
            minisDAO.resetMini(userId);
            boardRep.completed = true;
        }
        return boardRep;
    }

    public void recordMiniStarted(String userToken, MiniSolutionRep miniSolution) {
        if (userDAO.validateToken(userToken) != null) {
            if (statsDAO.getStats(userToken) == null) {
                statsDAO.createStats(userToken);
            }
            statsDAO.updateMiniStarted(userToken, miniSolution.size, miniSolution.difficulty);
            if (minisDAO.getMiniSolution(userToken) == null) {
                minisDAO.createMini(userToken, miniSolution);
            } else {
                minisDAO.updateMini(userToken, miniSolution);
            }
            logger.debug("User " + userToken + " began a mini puzzle");
        } else {
            logger.error("Unable to log start of mini puzzle, user does not exist: " + userToken);
        }
    }

    public MiniStatsRep getMiniStats(String userToken) {
        if (userDAO.validateToken(userToken) != null) {
            if (statsDAO.getStats(userToken) == null) {
                statsDAO.createStats(userToken);
            }
            return statsDAO.getStats(userToken);
        } else {
            logger.error("Cannot get stats, this user does not exist: " + userToken);
            return null;
        }
    }

    public LeaderboardRep getLeaderboard(String userToken) {
        final int LEN = 10;

        Map<String, User> userCache = new HashMap<>();

        List<MiniStatsRep> allStats = statsDAO.getAllStats();
        LeaderboardRep leaderboard = new LeaderboardRep();
        leaderboard.userStats = statsDAO.getStats(userToken);

        List<MiniStatsRep> sortedGamesList = new ArrayList<>(allStats);
        Collections.sort(sortedGamesList, new StatsComparator(StatsComparator.NUM_GAMES_TYPE));
        leaderboard.mostGamesCompleted = new ArrayList<>();

        List<MiniStatsRep> sortedCompletionList = new ArrayList<>(allStats);
        Collections.sort(sortedCompletionList, new StatsComparator(StatsComparator.COMPLETION_PCT_TYPE));
        leaderboard.bestCompletionPercent = new ArrayList<>();

        List<MiniStatsRep> sortedRevealList = new ArrayList<>(allStats);
        Collections.sort(sortedRevealList, new StatsComparator(StatsComparator.REVEAL_PCT_TYPE));
        leaderboard.lowestRevealPercent = new ArrayList<>();

        List<MiniStatsRep> sortedCurStreakList = new ArrayList<>(allStats);
        Collections.sort(sortedCurStreakList, new StatsComparator(StatsComparator.CUR_STREAK_TYPE));
        leaderboard.currentStreak = new ArrayList<>();

        List<MiniStatsRep> sortedLongStreakList = new ArrayList<>(allStats);
        Collections.sort(sortedLongStreakList, new StatsComparator(StatsComparator.LONG_STREAK_TYPE));
        leaderboard.longestStreak = new ArrayList<>();

        leaderboard.bestTimesPerCategory = new ArrayList<>();
        List<List<MiniStatsRep>> sortedTimeLists = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            List<MiniStatsRep> sortedTimeList = new ArrayList<>(allStats);
            Collections.sort(sortedTimeList, new StatsComparator(StatsComparator.BEST_TIMES_TYPE, i));
            sortedTimeLists.add(sortedTimeList);
            leaderboard.bestTimesPerCategory.add(new ArrayList<>());
        }

        MiniStatsRep curStats;
        User user;
        for (int i = 0; i < LEN; i++) {
            // handle completed game leaderboard
            if (i < sortedGamesList.size()) {
                curStats = sortedGamesList.get(i);
                if (userCache.containsKey(curStats.userToken)) {
                    user = userCache.get(curStats.userToken);
                } else {
                    user = userDAO.validateToken(curStats.userToken);
                    userCache.put(curStats.userToken, user);
                }
                if (getCompletedGames(curStats.completedGames) > 0) {
                    leaderboard.mostGamesCompleted.add(
                            new LeaderboardDataRep(user.getUsername(),
                            new Float(getCompletedGames(curStats.completedGames))));
                }
            }

            // handle completion percent leaderboard
            if (i < sortedCompletionList.size()) {
                curStats = sortedCompletionList.get(i);
                if (userCache.containsKey(curStats.userToken)) {
                    user = userCache.get(curStats.userToken);
                } else {
                    user = userDAO.validateToken(curStats.userToken);
                    userCache.put(curStats.userToken, user);
                }
                if (getCompletedGames(curStats.completedGames) > 0) {
                    leaderboard.bestCompletionPercent.add(
                            new LeaderboardDataRep(user.getUsername(),
                            new Float(getCompletionPercent(curStats.completedGames, curStats.startedGames))));
                }
            }

            // handle reveal percent leaderboard
            if (i < sortedRevealList.size()) {
                curStats = sortedRevealList.get(i);
                if (userCache.containsKey(curStats.userToken)) {
                    user = userCache.get(curStats.userToken);
                } else {
                    user = userDAO.validateToken(curStats.userToken);
                    userCache.put(curStats.userToken, user);
                }
                if (getCompletedGames(curStats.completedGames) > 0) {
                    leaderboard.lowestRevealPercent.add(
                            new LeaderboardDataRep(user.getUsername(),
                            new Float(getRevealPercent(curStats.completedGames, curStats.revealPercents))));
                }
            }

            // handle current streak leaderboard
            if (i < sortedCurStreakList.size()) {
                curStats = sortedCurStreakList.get(i);
                if (userCache.containsKey(curStats.userToken)) {
                    user = userCache.get(curStats.userToken);
                } else {
                    user = userDAO.validateToken(curStats.userToken);
                    userCache.put(curStats.userToken, user);
                }
                if (curStats.currentStreak > 0) {
                    leaderboard.currentStreak.add(
                            new LeaderboardDataRep(user.getUsername(),
                            new Float(curStats.currentStreak)));
                }
            }

            // handle longest streak leaderboard
            if (i < sortedLongStreakList.size()) {
                curStats = sortedLongStreakList.get(i);
                if (userCache.containsKey(curStats.userToken)) {
                    user = userCache.get(curStats.userToken);
                } else {
                    user = userDAO.validateToken(curStats.userToken);
                    userCache.put(curStats.userToken, user);
                }
                if (curStats.longestStreak > 0) {
                    leaderboard.longestStreak.add(
                            new LeaderboardDataRep(user.getUsername(),
                            new Float(curStats.longestStreak)));
                }
            }

            // handle best times
            for (int j = 0; j < 15; j++) {
                if (i < sortedTimeLists.get(j).size()) {
                    curStats = sortedTimeLists.get(j).get(i);
                    if (userCache.containsKey(curStats.userToken)) {
                        user = userCache.get(curStats.userToken);
                    } else {
                        user = userDAO.validateToken(curStats.userToken);
                        userCache.put(curStats.userToken, user);
                    }
                    if (curStats.completedGames[j] > 0) {
                        leaderboard.bestTimesPerCategory.get(j).add(
                                new LeaderboardDataRep(user.getUsername(),
                                new Float(curStats.bestTimes[j])));
                    }
                }
            }
        }
        return leaderboard;
    }

    private int getCompletedGames(Integer[] gameArr) {
        int total = 0;
        for (int i = 0; i < 15; i++) {
            if (gameArr[i] != null) total += gameArr[i];
        }
        return total;
    }

    private float getCompletionPercent(Integer[] completedArr, Integer[] startedArr) {
        float started = 0;
        float completed = 0;
        for (int i = 0; i < 15; i++) {
            if (startedArr[i] != null) started += startedArr[i];
            if (completedArr[i] != null) completed += completedArr[i];
        }
        if (started == 0) return 0;
        else return completed / started;
    }

    private float getRevealPercent(Integer[] completedArr, BigDecimal[] revealArr) {
        float total = 0;
        float completed = 0;
        for (int i = 0; i < 15; i++) {
            if (completedArr[i] != null) {
                total += completedArr[i] * revealArr[i].floatValue();
                completed += completedArr[i];
            }
        }
        return total / completed;
    }

    private class StatsComparator implements Comparator<MiniStatsRep> {
        public static final String NUM_GAMES_TYPE = "numGames";
        public static final String COMPLETION_PCT_TYPE = "completionPct";
        public static final String REVEAL_PCT_TYPE = "revealPct";
        public static final String CUR_STREAK_TYPE = "curStreak";
        public static final String LONG_STREAK_TYPE = "longStreak";
        public static final String BEST_TIMES_TYPE = "bestTimes";

        private String compareType;
        private int bestTimesIndex;

        public StatsComparator(String compareType) {
            this.compareType = compareType;
        }

        public StatsComparator(String compareType, int index) {
            this.compareType = compareType;
            this.bestTimesIndex = index;
        }

        @Override
        public int compare(MiniStatsRep o1, MiniStatsRep o2) {
            if (this.compareType.equals(NUM_GAMES_TYPE)) {
                int tot1 = 0;
                int tot2 = 0;
                for (int i = 0; i < 15; i++) {
                    if (o1.completedGames[i] != null) tot1 += o1.completedGames[i];
                    if (o2.completedGames[i] != null) tot2 += o2.completedGames[i];
                }
                return tot1 - tot2;
            } else if (this.compareType.equals(COMPLETION_PCT_TYPE)) {
                int started1 = 0;
                int started2 = 0;
                int completed1 = 0;
                int completed2 = 0;
                for (int i = 0; i < 15; i++) {
                    if (o1.startedGames[i] != null) started1 += o1.startedGames[i];
                    if (o2.startedGames[i] != null) started2 += o2.startedGames[i];
                    if (o1.completedGames[i] != null) completed1 += o1.completedGames[i];
                    if (o2.completedGames[i] != null) completed2 += o2.completedGames[i];
                }
                float pct1 = 0;
                float pct2 = 0;
                if (started1 != 0) {
                    pct1 = completed1 / started1;
                }
                if (started2 != 0) {
                    pct2 = completed2 / started2;
                }
                if (pct1 < pct2) return -1;
                else if (pct1 == pct2) return 0;
                else return 1;
            } else if (this.compareType.equals(REVEAL_PCT_TYPE)) {
                float total1 = 0;
                float total2 = 0;
                int completed1 = 0;
                int completed2 = 0;
                for (int i = 0; i < 15; i++) {
                    if (o1.completedGames[i] != null) {
                        completed1 += o1.completedGames[i];
                        total1 += o1.completedGames[i] * o1.revealPercents[i].floatValue();
                    }
                    if (o2.completedGames[i] != null){
                        completed2 += o2.completedGames[i];
                        total2 += o2.completedGames[i] * o2.revealPercents[i].floatValue();
                    }
                }
                float pct1 = 1;
                float pct2 = 1;
                if (completed1 != 0) {
                    pct1 = total1 / completed1;
                }
                if (completed2 != 0) {
                    pct2 = total2 / completed2;
                }
                if (pct1 < pct2) return 1;
                else if (pct1 == pct2) return 0;
                else return -1;
            } else if (this.compareType.equals(CUR_STREAK_TYPE)) {
                return o1.currentStreak - o2.currentStreak;
            } else if (this.compareType.equals(LONG_STREAK_TYPE)) {
                return o1.longestStreak - o2.longestStreak;
            } else if (this.compareType.equals(BEST_TIMES_TYPE)) {
                return o2.bestTimes[this.bestTimesIndex] - o1.bestTimes[this.bestTimesIndex];
            }
            return 0;
        }
    }
}
