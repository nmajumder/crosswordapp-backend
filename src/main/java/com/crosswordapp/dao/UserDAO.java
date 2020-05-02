package com.crosswordapp.dao;

import com.crosswordapp.object.Settings;
import com.crosswordapp.object.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.UUID;

public class UserDAO {
    private static Logger logger = LoggerFactory.getLogger(UserDAO.class);

    private final static String DB_URL = "jdbc:postgresql://localhost:5432/crosswordapp";
    private final static String DB_USER = "nathanmajumder";
    private final static String DB_PASS = "postgres";

    private final static String EMAIL_COL = "email";
    private final static String USER_COL = "username";
    private final static String TOKEN_COL = "token";
    private final static String COLOR_SCHEME_COL = "colorScheme";
    private final static String INACTIVITY_TIMER_COL = "inactivityTimer";
    private final static String PLAY_SOUND_COL = "playSound";

    private final static String GET_USER_BY_EMAIL =
            "SELECT token, username, colorScheme, inactivityTimer, playSound FROM users WHERE email = ?";
    private final static String GET_USER_BY_PASSWORD =
            "SELECT username, token, colorScheme, inactivityTimer, playSound FROM users WHERE email = ? AND password = ?";
    private final static String GET_USER_BY_TOKEN =
            "SELECT email, username, colorScheme, inactivityTimer, playSound FROM users WHERE token = ?";
    private final static String CREATE_USER =
            "INSERT INTO users (token, email, username, password, colorScheme, inactivityTimer, playSound) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private final static String UPDATE_USER = "UPDATE users SET email = ?, username = ?, password = ? WHERE token = ?";
    private final static String UPDATE_SETTINGS =
            "UPDATE users SET colorScheme = ?, inactivityTimer = ?, playSound = ? WHERE token = ?";

    public User createUser(String email, String username, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(CREATE_USER)) {
            Settings settings = new Settings();
            UUID token = UUID.randomUUID();
            ps.setString(1, token.toString());
            ps.setString(2, email);
            ps.setString(3, username);
            ps.setString(4, password);
            ps.setInt(5, settings.getColorScheme());
            ps.setInt(6, settings.getInactivityTimer());
            ps.setBoolean(7, settings.getPlaySound());
            ps.execute();
            User user = new User(token.toString(), email, username, settings);
            logger.info("Successfully created user with email: " + user.getEmail());
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user with email: " + email, e);
        }
    }

    public User updateUser(String token, String email, String username, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(UPDATE_USER)) {
            ps.setString(1, email);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.setString(4, token);
            int recordsUpdated = ps.executeUpdate();
            if (recordsUpdated != 1) {
                logger.error("Failed to update account, could not find target token: " + token);
                return null;
            }
            return validateToken(token);
        } catch(SQLException e) {
            throw new RuntimeException("Failed to link guest user to email: " + email, e);
        }
    }

    public User updateSettings(String token, Settings settings) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(UPDATE_SETTINGS)) {
            ps.setInt(1, settings.getColorScheme());
            ps.setInt(2, settings.getInactivityTimer());
            ps.setBoolean(3, settings.getPlaySound());
            ps.setString(4, token);
            int recordsUpdated = ps.executeUpdate();
            if (recordsUpdated != 1) {
                logger.error("Failed to update settings, could not find target token: " + token);
                return null;
            }
            logger.info("Successfully saved settings for target token: " + token);
            return validateToken(token);
        } catch(SQLException e) {
            throw new RuntimeException("Failed to update settings for token: " + token, e);
        }
    }

    public boolean userExists(String email) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(GET_USER_BY_EMAIL)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                // should only be 1 result if any
                if (rs.next()) {
                    // doesn't matter what is returned, make sure we find a row
                    logger.info("Successfully found user with email: " + email);
                    return true;
                }
                logger.error("Failed to find user with email: " + email);
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user with email: " + email, e);
        }
    }

    public User validatePassword(String email, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(GET_USER_BY_PASSWORD)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                // should only be 1 result if any
                if (rs.next()) {
                    String username = rs.getString(USER_COL);
                    String token = rs.getString(TOKEN_COL);
                    Integer colorScheme = rs.getInt(COLOR_SCHEME_COL);
                    Integer inactivityTimer = rs.getInt(INACTIVITY_TIMER_COL);
                    Boolean playSound = rs.getBoolean(PLAY_SOUND_COL);
                    Settings settings = new Settings(colorScheme, inactivityTimer, playSound);
                    logger.info("Successfully validated password for email: " + email);
                    return new User(token, email, username, settings);
                }
                logger.error("Failed to validate password for email: " + email);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user with email: " + email, e);
        }
    }

    public User validateToken(String token) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(GET_USER_BY_TOKEN)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                // should only be 1 result if any
                if (rs.next()) {
                    String email = rs.getString(EMAIL_COL);
                    String username = rs.getString(USER_COL);
                    Integer colorScheme = rs.getInt(COLOR_SCHEME_COL);
                    Integer inactivityTimer = rs.getInt(INACTIVITY_TIMER_COL);
                    Boolean playSound = rs.getBoolean(PLAY_SOUND_COL);
                    Settings settings = new Settings(colorScheme, inactivityTimer, playSound);
                    logger.info("Successfully validated token for email: " + email);
                    return new User(token, email, username, settings);
                }
                logger.error("Failed to validate token: " + token);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to validate token: " + token, e);
        }
    }
}
