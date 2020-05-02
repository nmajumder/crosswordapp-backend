package com.crosswordapp.service;

import com.crosswordapp.dao.UserDAO;
import com.crosswordapp.object.User;
import com.crosswordapp.rep.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {
    private Logger logger = LoggerFactory.getLogger(UserService.class);

    private UserDAO userDAO;

    public UserService() {
        userDAO = new UserDAO();
    }

    public UserResponseRep createUser(UserCreateRep userRep) {
        if (userDAO.userExists(userRep.email)) {
            logger.error("Email is already used on an account: " + userRep.email);
            return new UserResponseRep(false, "This email is already associated with an account.");
        } else {
            User user = userDAO.createUser(userRep.email, userRep.username, userRep.password);
            return new UserResponseRep(true, user);
        }
    }

    public UserResponseRep loginUser(UserLoginRep userRep) {
        if (!userDAO.userExists(userRep.email)) {
            logger.error("No account found with email: " + userRep.email);
            return new UserResponseRep(false, "email error");
        }
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

    public UserResponseRep linkUser(UserLinkRep userRep) {
        if (userDAO.userExists(userRep.newAccount.email)) {
            logger.error("New account email for link is already associated with an account: " + userRep.newAccount.email);
            return new UserResponseRep(false, "The new account email is already associated with an account.");
        } else {
            User user = userDAO.updateUser(userRep.token, userRep.newAccount.email,
                    userRep.newAccount.username, userRep.newAccount.password);
            if (user == null) {
                logger.error("Token for updating could not be found");
                return new UserResponseRep(false, "Nonexistent account error.");
            } else {
                return new UserResponseRep(true, user);
            }
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

}
