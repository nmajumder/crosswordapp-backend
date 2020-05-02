package com.crosswordapp.rep;

public class UserCreateRep {
    public String email;
    public String username;
    public String password;

    public UserCreateRep() {}

    public UserCreateRep(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }
}
