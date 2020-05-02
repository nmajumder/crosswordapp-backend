package com.crosswordapp.object;

import com.crosswordapp.ClueManager;

public class Mini {
    private MiniDifficulty difficulty;
    private Board board;
    private ClueManager clueManager;

    public Mini() {}

    public Mini(MiniDifficulty difficulty, Board board, ClueManager clueManager) {
        this.difficulty = difficulty;
        this.board = board;
        this.clueManager = clueManager;
    }

    public MiniDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(MiniDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public ClueManager getClueManager() {
        return clueManager;
    }

    public void setClueManager(ClueManager clueManager) {
        this.clueManager = clueManager;
    }
}
