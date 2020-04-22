package com.crosswordapp.rep;

import com.crosswordapp.object.ClueType;
import org.javatuples.Pair;

public class ClueRep {
    public String text;
    public ClueType direction;
    public Integer number;
    public Integer rowCoord;
    public Integer colCoord;
    public Integer answerLength;

    public ClueRep(String text, ClueType direction, Integer number,
                   Pair<Integer, Integer> coordinates, Integer answerLength) {
        this.text = text;
        this.direction = direction;
        this.number = number;
        this.rowCoord = coordinates.getValue0();
        this.colCoord = coordinates.getValue1();
        this.answerLength = answerLength;
    }
}
