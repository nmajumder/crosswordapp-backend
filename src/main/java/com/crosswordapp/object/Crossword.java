package com.crosswordapp.object;

import com.crosswordapp.ClueManager;

import java.util.UUID;

public class Crossword {
    private UUID id;
    private String title;
    private String date;
    private Difficulty difficulty;
    private Board board;
    private ClueManager clueManager;

    public Crossword(String title, String date, Difficulty difficulty, Board board, ClueManager clueManager) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.date = date;
        this.difficulty = difficulty;
        this.board = board;
        this.clueManager = clueManager;
    }

    /* IMMUTABLE FIELDS */

    public UUID getId() {
        return id;
    }

    /* MUTABLE FIELDS */

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Board getBoard() { return board; }

    public void setBoard(Board board) { this.board = board; }

    public ClueManager getClueManager() { return clueManager; }

    public void setClueManager(ClueManager clueManager) { this.clueManager = clueManager; }
}
