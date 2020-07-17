package com.crosswordapp.service;

import com.crosswordapp.dao.BoardDAO;
import com.crosswordapp.dao.CommentDAO;
import com.crosswordapp.dao.StatsDAO;
import com.crosswordapp.dao.UserDAO;
import com.crosswordapp.object.User;
import com.crosswordapp.rep.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private BoardDAO boardDAO;

    @Autowired
    private StatsDAO statsDAO;

    @Autowired
    private CommentDAO commentDAO;

    @Autowired
    private JavaMailSender emailSender;

    public UserResponseRep createUser(UserCreateRep userRep) {
        String conflict = userDAO.getUserConflict(userRep.email, userRep.username);
        if ("".equals(conflict)) {
            User user = userDAO.createUser(userRep.email, userRep.username, userRep.password);
            boardDAO.initializeAllBoardsForUser(user.getToken());
            statsDAO.initializeAllStatsForUser(user.getToken());
            return new UserResponseRep(true, user);
        } else {
            logger.warn("Received user creation conflict for user " + userRep.email + ", " + userRep.username);
            return new UserResponseRep(false, conflict);
        }
    }

    public UserResponseRep loginUser(UserLoginRep userRep) {
        User user = userDAO.validatePassword(userRep.email, userRep.password);
        if (user == null) {
            logger.error("Password incorrect for email: " + userRep.email);
            return new UserResponseRep(false, "password error");
        } else {
            return new UserResponseRep(true, user);
        }
    }

    public UserResponseRep validateUser(UserValidationRep userRep) {
        User user = userDAO.validateToken(userRep.token);
        if (user == null) {
            logger.error("Token is not valid or has expired, must log back in");
            return new UserResponseRep(false, "token error");
        } else {
            return new UserResponseRep(true, user);
        }
    }

    public UserResponseRep changePassword(UserPasswordRep userRep) {
        if (userDAO.validatePassword(userRep.email, userRep.password) == null) {
            logger.error("Password incorrect for email: " + userRep.email);
            return new UserResponseRep(false, "password error");
        }
        User user = userDAO.changePassword(userRep.email, userRep.newPassword);
        if (user == null) {
            logger.error("Unexpected error updating password for email: " + userRep.email);
            return new UserResponseRep(false, "password error");
        } else {
            return new UserResponseRep(true, user);
        }
    }

    public UserResponseRep linkUser(UserLinkRep userRep) {
        String conflict = userDAO.getUserConflict(userRep.newAccount.email, userRep.newAccount.username);
        if ("".equals(conflict)) {
            User user = userDAO.updateUser(userRep.token, userRep.newAccount.email,
                    userRep.newAccount.username, userRep.newAccount.password);
            if (user == null) {
                logger.error("Token for updating could not be found");
                return new UserResponseRep(false, "Nonexistent account error.");
            } else {
                return new UserResponseRep(true, user);
            }
        } else {
            logger.warn("Received user creation conflict for user " + userRep.newAccount.email + ", " + userRep.newAccount.username);
            return new UserResponseRep(false, conflict);
        }
    }

    public UserResponseRep saveSettings(SaveSettingsRep settingsRep) {
        User user = userDAO.updateSettings(settingsRep.userToken, settingsRep.settings);
        if (user == null) {
            logger.error("Cannot update settings, no user exists with the token: " + settingsRep.userToken);
            return new UserResponseRep(false, "Nonexistent account error.");
        } else {
            return new UserResponseRep(true, user);
        }
    }

    public UserResponseRep resetPassword(String email) {
        if (!userDAO.userExists(email)) {
            logger.error("Cannot reset password, email is not recognized in database: " + email);
            return new UserResponseRep(false, "Email not recognized");
        } else {
            String tempPassword = userDAO.resetPassword(email);
            sendPasswordMessage(email, tempPassword);
            return new UserResponseRep(true, "");
        }
    }

    private void sendPasswordMessage(String targetEmail, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(targetEmail);
        message.setSubject("Password reset for crossword app");
        String body = "Your temporary password is the code specified below. "
                + "Please copy paste this as your password to login, "
                + "and then go through the normal change password process once you are in.\n\n"
                + "Code: " + tempPassword;
        message.setText(body);
        emailSender.send(message);
    }

    public void postComment(UserCommentRep commentRep) {
        commentDAO.postComment(commentRep);
    }

}
