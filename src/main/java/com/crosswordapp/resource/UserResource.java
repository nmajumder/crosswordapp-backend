package com.crosswordapp.resource;

import com.crosswordapp.rep.*;
import com.crosswordapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public class UserResource {
    final static String PATH = "/api/user";
    Logger logger = LoggerFactory.getLogger(UserResource.class);

    @Autowired
    UserService userService;

    @PostMapping(PATH + "/create")
    public UserResponseRep createUser(@RequestBody UserCreateRep user) {
        return userService.createUser(user);
    }

    @PostMapping(PATH + "/login")
    public UserResponseRep loginUser(@RequestBody UserLoginRep user) {
        return userService.loginUser(user);
    }

    @PostMapping(PATH + "/validate")
    public UserResponseRep validateUser(@RequestBody UserValidationRep user) {
        return userService.validateUser(user);
    }

    @PutMapping(PATH + "/link")
    public UserResponseRep linkUser(@RequestBody UserLinkRep user) {
        return userService.linkUser(user);
    }

    @PutMapping(PATH + "/settings")
    public UserResponseRep saveSettings(@RequestBody SaveSettingsRep settings) {
        return userService.saveSettings(settings);
    }

    @PutMapping(PATH + "/username")
    public UserResponseRep changeUsername(@RequestBody UserUsernameRep user) {
        return userService.changeUsername(user);
    }

    @PutMapping(PATH + "/password")
    public UserResponseRep changePassword(@RequestBody UserPasswordRep user) {
        return userService.changePassword(user);
    }

    @PutMapping(PATH + "/password/reset")
    public UserResponseRep resetPassword(@RequestBody UserPasswordResetRep user) {
        return userService.resetPassword(user.email);
    }

    @PostMapping(PATH + "/comment")
    public void submitComment(@RequestBody UserCommentRep commentRep) {
        userService.postComment(commentRep);
    }

}
