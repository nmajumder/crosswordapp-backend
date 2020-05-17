package com.crosswordapp.bean.crossword;

import com.crosswordapp.object.Difficulty;
import com.crosswordapp.object.Symmetry;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@XmlRootElement(name = "crossword")
@XmlAccessorType(XmlAccessType.FIELD)
public class CrosswordBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "id")
    String id;

    @XmlElement(name = "title")
    String title;

    @XmlElement(name = "date")
    String date;

    @XmlElement(name = "grid")
    GridBean grid;

    @XmlElement(name = "symmetry")
    Symmetry symmetry;

    @XmlElement(name = "difficulty")
    Difficulty difficulty;

    @XmlElement(name = "acrossClues")
    ClueListBean acrossClues;

    @XmlElement(name = "downClues")
    ClueListBean downClues;

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

    public GridBean getGrid() {
        return grid;
    }

    public void setGrid(GridBean grid) {
        this.grid = grid;
    }

    public Symmetry getSymmetry() {
        return symmetry;
    }

    public void setSymmetry(Symmetry symmetry) {
        this.symmetry = symmetry;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public ClueListBean getAcrossClues() {
        return acrossClues;
    }

    public void setAcrossClues(ClueListBean acrossClues) {
        this.acrossClues = acrossClues;
    }

    public ClueListBean getDownClues() {
        return downClues;
    }

    public void setDownClues(ClueListBean downClues) {
        this.downClues = downClues;
    }
}
