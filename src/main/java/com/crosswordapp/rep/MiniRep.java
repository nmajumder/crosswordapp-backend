package com.crosswordapp.rep;

import com.crosswordapp.object.Clue;
import com.crosswordapp.object.Mini;
import com.crosswordapp.object.MiniDifficulty;

import java.util.ArrayList;
import java.util.List;

public class MiniRep {
    public MiniDifficulty difficulty;
    public BoardRep board;
    public List<ClueRep> acrossClues;
    public List<ClueRep> downClues;

    public MiniRep(Mini mini) {
        this.difficulty = mini.getDifficulty();
        this.board = new BoardRep(mini.getBoard());
        this.acrossClues = new ArrayList<>();
        for (Integer i: mini.getClueManager().getAcrossClueMap().keySet()) {
            Clue clue = mini.getClueManager().getAcrossClueMap().get(i);
            acrossClues.add(new ClueRep(clue.getText(), clue.getDirection(), i,
                    clue.getCoordinates(), clue.getAnswerLength()));
        }
        this.downClues = new ArrayList<>();
        for (Integer i: mini.getClueManager().getDownClueMap().keySet()) {
            Clue clue = mini.getClueManager().getDownClueMap().get(i);
            downClues.add(new ClueRep(clue.getText(), clue.getDirection(), i,
                    clue.getCoordinates(), clue.getAnswerLength()));
        }
    }
}
