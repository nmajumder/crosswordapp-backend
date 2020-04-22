package com.crosswordapp.rep;

import com.crosswordapp.object.Board;
import com.crosswordapp.object.BoardSquare;
import com.crosswordapp.object.SquareSelection;

import java.util.List;

public class BoardRep {
    public List<List<BoardSquare>> grid;
    public SquareSelection selection;
    public Integer numSeconds;

    public BoardRep() {}

    public BoardRep(Board board) {
        this.grid = board.getGrid();
        this.selection = board.getSelection();
        this.numSeconds = board.getNumSeconds();
    }
}
