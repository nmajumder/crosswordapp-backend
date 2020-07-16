package com.crosswordapp.dao;

import com.crosswordapp.object.Settings;
import com.crosswordapp.object.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class UserDAO {
    private static Logger logger = LoggerFactory.getLogger(UserDAO.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${postgres.url}")
    private String DB_URL;
    @Value("${postgres.user}")
    private String DB_USER;
    @Value("${postgres.password}")
    private String DB_PASS;

    private final static String EMAIL_COL = "email";
    private final static String USER_COL = "username";
    private final static String TOKEN_COL = "token";
    private final static String PASSWORD_COL = "password";
    private final static String COLOR_SCHEME_COL = "color_scheme";
    private final static String INACTIVITY_TIMER_COL = "inactivity_timer";

    private final static String GET_USER_BY_EMAIL =
            "SELECT " + getFieldList(false, TOKEN_COL, USER_COL, COLOR_SCHEME_COL, INACTIVITY_TIMER_COL)
                    + " FROM users WHERE " + getFieldList(true, EMAIL_COL);
    private final static String GET_USER_BY_PASSWORD =
            "SELECT " + getFieldList(false, TOKEN_COL, USER_COL, COLOR_SCHEME_COL, INACTIVITY_TIMER_COL)
                    + " FROM users WHERE " + getFieldList(true, EMAIL_COL)
                    + " AND " + getFieldList(true, PASSWORD_COL);
    private final static String GET_USER_BY_TOKEN =
            "SELECT " + getFieldList(false, EMAIL_COL, USER_COL, COLOR_SCHEME_COL, INACTIVITY_TIMER_COL)
                    + " FROM users WHERE " + getFieldList(true, TOKEN_COL);
    private final static String CHECK_USER_UNIQUE =
            "SELECT " + getFieldList(false, EMAIL_COL, USER_COL) + " FROM users WHERE "
                    + getFieldList(true, EMAIL_COL) + " OR " + getFieldList(true, USER_COL);
    private final static String UPDATE_PASSWORD =
            "UPDATE users SET " + getFieldList(true, PASSWORD_COL) + " WHERE " + getFieldList(true, EMAIL_COL);
    private final static String GET_PASSWORD_FOR_VALIDATION =
            "SELECT " + getFieldList(false, TOKEN_COL, PASSWORD_COL)
                    + " FROM users WHERE " + getFieldList(true, EMAIL_COL);
    private final static String CREATE_USER =
            "INSERT INTO users (" + getFieldList(false, "ALL") + ") VALUES (?, ?, ?, ?, ?, ?)";
    private final static String UPDATE_USER =
            "UPDATE users SET " + getFieldList(true, EMAIL_COL, USER_COL, PASSWORD_COL)
                    + " WHERE " + getFieldList(true, TOKEN_COL);
    private final static String UPDATE_SETTINGS =
            "UPDATE users SET " + getFieldList(true, COLOR_SCHEME_COL, INACTIVITY_TIMER_COL)
                    + " WHERE " + getFieldList(true, TOKEN_COL);

    public User createUser(String email, String username, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(CREATE_USER)) {
            Settings settings = new Settings();
            UUID token = UUID.randomUUID();
            ps.setString(1, token.toString());
            ps.setString(2, email);
            ps.setString(3, username);
            ps.setString(4, passwordEncoder.encode(password));
            ps.setInt(5, settings.getColorScheme());
            ps.setInt(6, settings.getInactivityTimer());
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
            ps.setString(3, passwordEncoder.encode(password));
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
            ps.setString(3, token);
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

    public String getUserConflict(String email, String username) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(CHECK_USER_UNIQUE)) {
            ps.setString(1, email);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                boolean emailConflict = false;
                boolean userConflict = false;
                while (rs.next()) {
                    String rsEmail = rs.getString(EMAIL_COL);
                    String rsUser = rs.getString(USER_COL);
                    if (rsEmail.equals(email)) {
                        emailConflict = true;
                    }
                    if (rsUser.equals(username)) {
                        userConflict = true;
                    }
                }
                if (emailConflict) return "This email is already associated with an account.";
                if (userConflict) return "This username is already in use.";
                return "";
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute user uniqueness query for email: " + email + ", username: " + username, e);
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

    private String getTokenByEmail(String email) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(GET_USER_BY_EMAIL)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                // should only be 1 result if any
                if (rs.next()) {
                    // doesn't matter what is returned, make sure we find a row
                    String token = rs.getString(TOKEN_COL);
                    logger.info("Successfully found token for email: " + email);
                    return token;
                }
                logger.error("Unable to find token for email: " + email);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find token for email: " + email, e);
        }
    }

    public User changePassword(String email, String newPassword) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            PreparedStatement ps = conn.prepareStatement(UPDATE_PASSWORD)) {
            ps.setString(1, passwordEncoder.encode(newPassword));
            ps.setString(2, email);
            int recordsUpdated = ps.executeUpdate();
            if (recordsUpdated != 1) {
                logger.error("Failed to update password, could not find user with email: " + email);
                return null;
            }
            logger.info("Successfully updated password for user with email: " + email);
            return validatePassword(email, newPassword);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user with email: " + email, e);
        }
    }

    public User validatePassword(String email, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(GET_PASSWORD_FOR_VALIDATION)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                // should only be 1 result if any
                if (rs.next()) {
                    String token = rs.getString(TOKEN_COL);
                    String dbPassword = rs.getString(PASSWORD_COL);
                    if (passwordEncoder.matches(password, dbPassword)) {
                        logger.info("Successfully validated password for email: " + email);
                        return validateToken(token);
                    }
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
                    Settings settings = new Settings(colorScheme, inactivityTimer);
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

    public String resetPassword(String email) {
        UUID randomPassword = UUID.randomUUID();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            PreparedStatement ps = conn.prepareStatement(UPDATE_PASSWORD)) {
            ps.setString(1, passwordEncoder.encode(randomPassword.toString()));
            ps.setString(2, email);
            int recordsUpdated = ps.executeUpdate();
            if (recordsUpdated != 1) {
                logger.error("Failed to reset password, could not find target email: " + email);
                return null;
            }
            logger.info("Successfully reset password for account with email: " + email);
            return randomPassword.toString();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reset password for account with email: " + email, e);
        }
    }

    private static String getFieldList(boolean setter, String... args) {
        if (args.length == 0) {
            return "";
        }
        if (args.length == 1 && args[0].equals("ALL")) {
            args = new String[]{TOKEN_COL, EMAIL_COL, USER_COL, PASSWORD_COL,
                    COLOR_SCHEME_COL, INACTIVITY_TIMER_COL};
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
