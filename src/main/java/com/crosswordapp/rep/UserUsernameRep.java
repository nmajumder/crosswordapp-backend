package com.crosswordapp.rep;

public class UserUsernameRep {
    public String token;
    public String email;
    public String newUsername;

    public UserUsernameRep() {}

    public UserUsernameRep(String token, String email, String newUsername) {
        this.token = token;
        this.email = email;
        this.newUsername = newUsername;
    }
}
