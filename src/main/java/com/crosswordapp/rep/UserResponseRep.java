package com.crosswordapp.rep;

import com.crosswordapp.object.User;

import java.util.UUID;

public class UserResponseRep {
    public User user;
    public Boolean success;
    public String error;

    public UserResponseRep() {}

    public UserResponseRep(Boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    public UserResponseRep(Boolean success, User user) {
        this.user = user;
        this.success = success;
        this.error = "";
    }
}
