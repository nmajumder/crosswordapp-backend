package com.crosswordapp.rep;

public class UserCommentRep {
    public String userId;
    public String type;
    public String text;

    public UserCommentRep() {}

    public UserCommentRep(String userId, String type, String text) {
        this.userId = userId;
        this.type = type;
        this.text = text;
    }
}
