package com.crosswordapp.object;

public class MiniClue {
    private MiniClueDay day;
    private String text;

    public MiniClue() {}

    public MiniClue(MiniClueDay day, String text) {
        this.day = day;
        this.text = text;
    }

    public MiniClueDay getDay() {
        return day;
    }

    public void setDay(MiniClueDay day) {
        this.day = day;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
