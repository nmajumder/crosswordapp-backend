package com.crosswordapp.resource;

import com.crosswordapp.object.User;
import com.crosswordapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:4200" })
@RestController
public class UserResource {
    final static String PATH = "/api/user";
    Logger logger = LoggerFactory.getLogger(UserResource.class);

    @Autowired
    UserService userService;

    @GetMapping(PATH + "/all")
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @GetMapping(PATH + "/{username}")
    public User getUser(@PathVariable String username) {
        return userService.findByUsername(username);
    }

    @PostMapping(PATH + "/create")
    public boolean createUser(@RequestBody User user) {
        return userService.createUser(user.getUsername(), user.getPassword());
    }
}
