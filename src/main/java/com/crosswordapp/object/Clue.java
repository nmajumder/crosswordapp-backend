package com.crosswordapp.object;

import org.javatuples.Pair;

public class Clue {
    private String text;
    private ClueType direction;
    private Integer number;
    private Pair<Integer, Integer> coordinates;
    private Integer answerLength;

    public Clue(String text, ClueType direction, Integer number,
                Pair<Integer, Integer> coordinates, Integer answerLength) {
        this.text = text;
        this.direction = direction;
        this.number = number;
        this.coordinates = coordinates;
        this.answerLength = answerLength;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ClueType getDirection() {
        return direction;
    }

    public void setDirection(ClueType direction) {
        this.direction = direction;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Pair<Integer, Integer> getCoordinates() { return coordinates; }

    public void setCoordinates(Pair<Integer, Integer> coordinates) { this.coordinates = coordinates; }

    public Integer getAnswerLength() { return answerLength; }

    public void setAnswerLength(Integer answerLength) { this.answerLength = answerLength; }
}
