package com.crosswordapp.rep;

import com.crosswordapp.object.Clue;
import com.crosswordapp.object.Crossword;
import com.crosswordapp.object.Difficulty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CrosswordRep {
    public UUID id;
    public String title;
    public String date;
    public Difficulty difficulty;
    public BoardRep board;
    public List<ClueRep> acrossClues;
    public List<ClueRep> downClues;
    public Map<String, Set<String>> clueRefMap;

    public CrosswordRep(Crossword crossword) {
        this.id = crossword.getId();
        this.title = crossword.getTitle();
        this.date = crossword.getDate();
        this.difficulty = crossword.getDifficulty();
        this.board = new BoardRep(crossword.getBoard(), false);
        this.acrossClues = new ArrayList<>();
        for (Integer i: crossword.getClueManager().getAcrossClueMap().keySet()) {
            Clue clue = crossword.getClueManager().getAcrossClueMap().get(i);
            acrossClues.add(new ClueRep(clue.getText(), clue.getDirection(), i,
                    clue.getCoordinates(), clue.getAnswerLength()));
        }
        this.downClues = new ArrayList<>();
        for (Integer i: crossword.getClueManager().getDownClueMap().keySet()) {
            Clue clue = crossword.getClueManager().getDownClueMap().get(i);
            downClues.add(new ClueRep(clue.getText(), clue.getDirection(), i,
                    clue.getCoordinates(), clue.getAnswerLength()));
        }
        clueRefMap = crossword.getClueManager().getReferenceMap();
    }
}
