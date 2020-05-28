package com.crosswordapp.dao;

import com.crosswordapp.object.BoardSquare;
import com.crosswordapp.object.MiniDifficulty;
import com.crosswordapp.rep.MiniSolutionRep;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.regexp.internal.RE;
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
public class MinisDAO {
    Logger logger = LoggerFactory.getLogger(MinisDAO.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${postgres.url}")
    private String DB_URL;
    @Value("${postgres.user}")
    private String DB_USER;
    @Value("${postgres.password}")
    private String DB_PASS;

    private final static String USER_ID_COL = "user_id";
    private final static String MINI_GRID_COL = "mini_solution";
    private final static String SIZE_COL = "size";
    private final static String DIFFICULTY_COL = "difficulty";
    private final static String CHECKED_COL = "checked";
    private final static String REVEALED_COL = "revealed";

    private final static String CREATE_MINI_ROW_FOR_USER =
            "INSERT INTO miniboards (" + getFieldList(false, "ALL") + ") VALUES (?, ?, ?, ?, ?, ?)";
    private final static String UPDATE_MINI_FOR_USER =
            "UPDATE miniboards SET "
                    + getFieldList(true, MINI_GRID_COL, SIZE_COL, DIFFICULTY_COL, CHECKED_COL, REVEALED_COL)
                    + " WHERE " + getFieldList(true, USER_ID_COL);
    private final static String GET_MINI_SOLUTION_FOR_USER =
            "SELECT " + getFieldList(false, "ALL") + " FROM miniboards"
                    + " WHERE " + getFieldList(true, USER_ID_COL);

    public void createMini(String userId, MiniSolutionRep miniSolution) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(CREATE_MINI_ROW_FOR_USER)) {
            ps.setString(1, userId);
            ps.setObject(2, getJsonGrid(miniSolution.solution));
            ps.setInt(3, miniSolution.size);
            ps.setString(4, miniSolution.difficulty.toString());
            ps.setBoolean(5, miniSolution.checked);
            ps.setBoolean(6, miniSolution.revealed);
            ps.execute();
            logger.info("Successfully created mini board row for user: " + userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create mini board row for user " + userId, e);
        }
    }

    public void updateMini(String userId, MiniSolutionRep miniSolution) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(UPDATE_MINI_FOR_USER)) {
            ps.setObject(1, getJsonGrid(miniSolution.solution));
            ps.setInt(2, miniSolution.size);
            ps.setString(3, miniSolution.difficulty.toString());
            ps.setBoolean(4, miniSolution.checked);
            ps.setBoolean(5, miniSolution.revealed);
            ps.setString(6, userId);
            int recordsUpdated = ps.executeUpdate();
            if (recordsUpdated != 1) {
                logger.error("Unable to update mini board, no row found for user: " + userId);
            } else {
                logger.info("Successfully updated mini board for user: " + userId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update mini board for user: " + userId, e);
        }
    }

    public MiniSolutionRep getMiniSolution(String userId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(GET_MINI_SOLUTION_FOR_USER)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String gridStr = rs.getString(MINI_GRID_COL);
                    List<List<String>> grid = getGridFromJson(gridStr);
                    Integer size = rs.getInt(SIZE_COL);
                    String diffStr = rs.getString(DIFFICULTY_COL);
                    MiniDifficulty difficulty;
                    try {
                        difficulty = MiniDifficulty.valueOf(diffStr);
                    } catch (IllegalArgumentException e) {
                        difficulty = null;
                    }
                    Boolean checked = rs.getBoolean(CHECKED_COL);
                    Boolean revealed = rs.getBoolean(REVEALED_COL);
                    logger.info("Successfully retrieved mini grid for user: " + userId);
                    return new MiniSolutionRep(grid, size, difficulty, checked, revealed);
                }
                logger.error("Unable to find mini grid for user: " + userId);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve mini board solution for user: " + userId, e);
        }
    }

    public void resetMini(String userId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(UPDATE_MINI_FOR_USER)) {
            ps.setObject(1, getJsonGrid(new ArrayList<>()));
            ps.setInt(2, 0);
            ps.setString(3, MiniDifficulty.Easy.toString());
            ps.setBoolean(4, false);
            ps.setBoolean(5, false);
            ps.setString(6, userId);
            int recordsUpdated = ps.executeUpdate();
            if (recordsUpdated != 1) {
                logger.error("Unable to reset mini board, no row found for user: " + userId);
            } else {
                logger.info("Successfully reset mini board for user: " + userId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reset mini board for user: " + userId, e);
        }
    }

    private static PGobject getJsonGrid(List<List<String>> grid) {
        try {
            String gridStr = mapper.writeValueAsString(grid);
            PGobject gridObj = new PGobject();
            gridObj.setType("json");
            gridObj.setValue(gridStr);
            return gridObj;
        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize grid to json: " + grid, e);
        }
    }

    private static List<List<String>> getGridFromJson(String jsonGrid) {
        try {
            List<List<String>> grid = mapper.readValue(jsonGrid, new TypeReference<List<List<String>>>(){});
            return grid;
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize json into grid: " + jsonGrid, e);
        }
    }

    private static String getFieldList(boolean setter, String... args) {
        if (args.length == 0) {
            return "";
        }
        if (args.length == 1 && args[0].equals("ALL")) {
            args = new String[]{USER_ID_COL, MINI_GRID_COL, SIZE_COL, DIFFICULTY_COL, CHECKED_COL, REVEALED_COL};
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
