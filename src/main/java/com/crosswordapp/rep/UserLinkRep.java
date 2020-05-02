package com.crosswordapp.rep;

public class UserLinkRep {
    public String token;
    public UserCreateRep newAccount;

    public UserLinkRep () {}

    public UserLinkRep(String token, UserCreateRep newAcccount) {
        this.token = token;
        this.newAccount = newAcccount;
    }
}
