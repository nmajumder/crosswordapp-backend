package com.crosswordapp.rep;

import com.crosswordapp.object.Board;
import com.crosswordapp.object.BoardSquare;
import com.crosswordapp.object.SquareSelection;

import java.util.List;

public class BoardRep {
    public List<List<BoardSquare>> grid;
    public List<List<String>> solution; // this will be null for normal xwords, filled in for generated minis
    public SquareSelection selection;
    public Integer numSeconds;
    public Boolean completed;
    public Integer difficultyRating;
    public Integer enjoymentRating;

    public BoardRep() {}

    public BoardRep(Board board) {
        this.grid = board.getGrid();
        this.selection = board.getSelection();
        this.numSeconds = board.getNumSeconds();
        this.completed = board.getCompleted();
        this.difficultyRating = board.getDifficultyRating();
        this.enjoymentRating = board.getEnjoymentRating();
    }
}
