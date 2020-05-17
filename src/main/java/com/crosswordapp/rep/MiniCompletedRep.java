package com.crosswordapp.rep;

import com.crosswordapp.object.MiniDifficulty;

public class MiniCompletedRep {
    public Integer size;
    public MiniDifficulty difficulty;
    public Integer seconds;
    public Boolean checked;
    public Boolean revealed;

    public MiniCompletedRep() {}

    public MiniCompletedRep(Integer size, MiniDifficulty difficulty, Integer seconds,
                            Boolean checked, Boolean revealed) {
        this.size = size;
        this.difficulty = difficulty;
        this.seconds = seconds;
        this.checked = checked;
        this.revealed = revealed;
    }
}
