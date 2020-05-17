package com.crosswordapp.rep;

public class UserPasswordRep {
    public String email;
    public String password;
    public String newPassword;

    public UserPasswordRep() {}

    public UserPasswordRep(String email, String password, String newPassword) {
        this.email = email;
        this.password = password;
        this.newPassword = newPassword;
    }
}
