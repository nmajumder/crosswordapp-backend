package com.crosswordapp.service;

import com.crosswordapp.dao.UserDAO;
import com.crosswordapp.object.User;
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

    public List<User> findAll() {
        return userDAO.getAllUsers();
    }

    public User findByUsername(String username) {
        return userDAO.getUser(username);
    }

    public boolean createUser(String username, String password) {
        if (userDAO.getUser(username) == null) {
            logger.error("There is already a user with username: " + username);
            return false;
        } else {
            userDAO.createUser(username, password);
            return true;
        }
    }
}
