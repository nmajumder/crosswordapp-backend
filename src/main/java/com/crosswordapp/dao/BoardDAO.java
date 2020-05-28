package com.crosswordapp.dao;

import com.crosswordapp.object.BoardSquare;
import com.crosswordapp.object.Rating;
import com.crosswordapp.object.SquareSelection;
import com.crosswordapp.rep.BoardRep;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class BoardDAO {
    private static Logger logger = LoggerFactory.getLogger(BoardDAO.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${postgres.url}")
    private String DB_URL;
    @Value("${postgres.user}")
    private String DB_USER;
    @Value("${postgres.password}")
    private String DB_PASS;

    private final static String USER_ID_COL = "user_id";
    private final static String CROSSWORD_ID_COL = "crossword_id";
    private final static String GRID_COL = "grid";
    private final static String SELECTION_COL = "selection";
    private final static String SECONDS_COL = "seconds";
    private final static String COMPLETED_COL = "completed";
    private final static String DIF_RATING_COL = "difficulty_rating";
    private final static String ENJ_RATING_COL = "enjoyment_rating";

    private final static String CREATE_BOARD =
            "INSERT INTO boards (" + getFieldList(false, "ALL") + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private final static String UPDATE_BOARD =
            "UPDATE boards SET " + getFieldList(true, GRID_COL, SELECTION_COL, SECONDS_COL,
                                                    COMPLETED_COL, DIF_RATING_COL, ENJ_RATING_COL)
                    + " WHERE " + getFieldList(true, USER_ID_COL)
                    + " AND " + getFieldList(true, CROSSWORD_ID_COL);
    private final static String GET_BOARD =
            "SELECT " + getFieldList(false, GRID_COL, SELECTION_COL, SECONDS_COL,
                                                    COMPLETED_COL, DIF_RATING_COL, ENJ_RATING_COL) + " FROM boards"
                    + " WHERE " + getFieldList(true, USER_ID_COL)
                    + " AND " + getFieldList(true, CROSSWORD_ID_COL);
    private final static String GET_ALL_RATINGS =
            "SELECT " + getFieldList(false, CROSSWORD_ID_COL, DIF_RATING_COL, ENJ_RATING_COL)
                    + " FROM boards";

    public void createBoard(String userId, String crosswordId, BoardRep board) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(CREATE_BOARD)) {
            ps.setString(1, userId);
            ps.setString(2, crosswordId);
            ps.setObject(3, getJsonGrid(board));
            ps.setObject(4, getJsonSelection(board));
            ps.setInt(5, board.numSeconds);
            ps.setBoolean(6, board.completed);
            ps.setInt(7, board.difficultyRating == null ? 0 : board.difficultyRating);
            ps.setInt(8, board.enjoymentRating == null ? 0 : board.enjoymentRating);
            ps.execute();
            logger.info("Successfully created board for crossword " + crosswordId + " for user " + userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create board for crossword " + crosswordId + " for user " + userId, e);
        }
    }

    public void updateBoard(String userId, String crosswordId, BoardRep board) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(UPDATE_BOARD)) {
            ps.setObject(1, getJsonGrid(board));
            ps.setObject(2, getJsonSelection(board));
            ps.setInt(3, board.numSeconds);
            ps.setBoolean(4, board.completed);
            ps.setInt(5, board.difficultyRating == null ? 0 : board.difficultyRating);
            ps.setInt(6, board.enjoymentRating == null ? 0 : board.enjoymentRating);
            ps.setString(7, userId);
            ps.setString(8, crosswordId);
            ps.execute();
            logger.info("Successfully updated board for crossword " + crosswordId + " for user " + userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update board for crossword " + crosswordId + " for user " + userId, e);
        }
    }

    public BoardRep getBoard(String userId, String crosswordId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(GET_BOARD)) {
            ps.setString(1, userId);
            ps.setString(2, crosswordId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String gridStr = rs.getString(GRID_COL);
                    String selectionStr = rs.getString(SELECTION_COL);
                    Integer seconds = rs.getInt(SECONDS_COL);
                    Boolean completed = rs.getBoolean(COMPLETED_COL);
                    Integer difRating = rs.getInt(DIF_RATING_COL);
                    Integer enjRating = rs.getInt(ENJ_RATING_COL);
                    BoardRep board = getBoardFromJson(gridStr, selectionStr, seconds,
                            completed, difRating, enjRating);
                    logger.info("Successfully retrieved board for user " + userId + " and crossword " + crosswordId);
                    return board;
                }
                logger.error("Unable to find board for user " + userId + " and crossword " + crosswordId);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve board for crossword " + crosswordId + " for user " + userId, e);
        }
    }

    public List<Rating> getAllRatings() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(GET_ALL_RATINGS);
             ResultSet rs = ps.executeQuery()) {
            List<Rating> ratings = new ArrayList<>();
            while (rs.next()) {
                String crosswordId = rs.getString(CROSSWORD_ID_COL);
                int difficultyRatings = rs.getInt(DIF_RATING_COL);
                int enjoymentRatings = rs.getInt(ENJ_RATING_COL);
                ratings.add(new Rating(crosswordId, difficultyRatings, enjoymentRatings));
            }
            logger.info("Successfully retrieved " + ratings.size() + " ratings");
            return ratings;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve all boards");
        }
    }

    private static PGobject getJsonGrid(BoardRep board) {
        try {
            String gridStr = mapper.writeValueAsString(board.grid);
            PGobject gridObj = new PGobject();
            gridObj.setType("json");
            gridObj.setValue(gridStr);
            return gridObj;
        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize board rep to json: " + board, e);
        }
    }

    private static PGobject getJsonSelection(BoardRep board) {
        try {
            String selectionStr = mapper.writeValueAsString(board.selection);
            PGobject selectionObj = new PGobject();
            selectionObj.setType("json");
            selectionObj.setValue(selectionStr);
            return selectionObj;
        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize board rep to json: " + board, e);
        }
    }

    private static BoardRep getBoardFromJson(String jsonGrid, String jsonSelection, Integer seconds,
                             Boolean completed, Integer difficultyRating, Integer enjoymentRating) {
        BoardRep board = new BoardRep();
        try {
            List<List<BoardSquare>> grid = mapper.readValue(jsonGrid, new TypeReference<List<List<BoardSquare>>>(){});
            board.grid = grid;
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize json into grid for board rep: " + jsonGrid, e);
        }

        try {
            SquareSelection selection = mapper.readValue(jsonSelection, SquareSelection.class);
            board.selection = selection;
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize json into selection for board rep: " + jsonSelection, e);
        }

        board.numSeconds = seconds;
        board.completed = completed;
        board.difficultyRating = difficultyRating;
        board.enjoymentRating = enjoymentRating;
        return board;
    }

    private static String getFieldList(boolean setter, String... args) {
        if (args.length == 0) {
            return "";
        }
        if (args.length == 1 && args[0].equals("ALL")) {
            args = new String[]{USER_ID_COL, CROSSWORD_ID_COL, GRID_COL, SELECTION_COL,
                                SECONDS_COL, COMPLETED_COL, DIF_RATING_COL, ENJ_RATING_COL};
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (setter) {
                sb.append(" = ?");
            }
            if (i < args.length-1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
