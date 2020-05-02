package com.crosswordapp.object;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MiniGridTemplate {
    private static Logger logger = LoggerFactory.getLogger(MiniGridTemplate.class);

    private Integer size;
    private Integer numBlack;
    private List<List<Character>> grid;

    public MiniGridTemplate(Integer size, Integer numBlack, List<String> gridRows) {
        if (gridRows.size() != size) {
            throw new RuntimeException("Unable to validate grid due to difference in specified size (" + size + ") and grid size (" + gridRows.size() + ")");
        }
        this.size = size;
        this.numBlack = numBlack;
        this.grid = new ArrayList<>();
        for (int row = 0; row < gridRows.size(); row++) {
            this.grid.add(new ArrayList<>());
            for (int col = 0; col < gridRows.get(row).length(); col++) {
                Character c = gridRows.get(row).charAt(col);
                this.grid.get(row).add(c);
            }
        }
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getNumBlack() {
        return numBlack;
    }

    public void setNumBlack(Integer numBlack) {
        this.numBlack = numBlack;
    }

    public List<List<Character>> getGrid() {
        return grid;
    }

    public void setGrid(List<List<Character>> grid) {
        this.grid = grid;
    }
}
