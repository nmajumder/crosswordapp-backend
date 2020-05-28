package com.crosswordapp.rep;

import com.crosswordapp.object.MiniDifficulty;

import java.util.List;

public class MiniSolutionRep {
    public List<List<String>> solution;
    public Integer size;
    public MiniDifficulty difficulty;
    public Boolean checked;
    public Boolean revealed;

    public MiniSolutionRep() {}

    public MiniSolutionRep(List<List<String>> solution, Integer size, MiniDifficulty difficulty, Boolean checked, Boolean revealed) {
        this.solution = solution;
        this.size = size;
        this.difficulty = difficulty;
        this.checked = checked;
        this.revealed = revealed;
    }
}
