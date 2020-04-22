package com.crosswordapp.dao;

import com.crosswordapp.object.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDAO {
    private static Logger logger = LoggerFactory.getLogger(UserDAO.class);

    private final static String DB_URL = "jdbc:postgresql://localhost:5432/crosswordapp";
    private final static String DB_USER = "nathanmajumder";
    private final static String DB_PASS = "postgres";

    private final static String USER_COL = "username";
    private final static String PASS_COL = "password";

    private final static String GET_ALL_USERS = "SELECT * FROM users";
    private final static String GET_USER = "SELECT * FROM users WHERE username = ?";
    private final static String CREATE_USER = "INSERT INTO users (username, password) VALUES (?, ?)";

    public User createUser(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(CREATE_USER)) {
            UUID id = UUID.randomUUID();
            ps.setString(1, username);
            ps.setString(2, password);
            ps.execute();
            logger.info("Successfully created user with username: " + username);
            return new User(username, password);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user with username: " + username, e);
        }
    }

    public User getUser(String username) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(GET_USER)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                // should only be 1 result if any
                if (rs.next()) {
                    String user = rs.getString(USER_COL);
                    String pass = rs.getString(PASS_COL);
                    logger.info("Successfully found user with nickname: " + user);
                    return new User(user, pass);
                }
                logger.error("Failed to find user with username: " + username);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user with username: " + username, e);
        }
    }

    public List<User> getAllUsers() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                PreparedStatement ps = conn.prepareStatement(GET_ALL_USERS);
                ResultSet rs = ps.executeQuery()) {
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                String user = rs.getString(USER_COL);
                String pass = rs.getString(PASS_COL);
                users.add(new User(user, pass));
            }
            logger.info("Successfully found " + users.size() + " users");
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get all users", e);
        }
    }
}
