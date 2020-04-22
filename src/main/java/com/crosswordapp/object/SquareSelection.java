package com.crosswordapp.object;

public class SquareSelection {
    private Integer rowCoord;
    private Integer colCoord;

    private ClueType direction;

    public SquareSelection() {}

    public SquareSelection(Integer rowCoord, Integer colCoord, ClueType direction) {
        this.rowCoord = rowCoord;
        this.colCoord = colCoord;
        this.direction = direction;
    }

    public Integer getRowCoord() { return rowCoord; }

    public void setRowCoord(Integer rowCoord) { this.rowCoord = rowCoord; }

    public Integer getColCoord() { return colCoord; }

    public void setColCoord(Integer colCoord) { this.colCoord = colCoord; }

    public ClueType getDirection() { return direction; }

    public void setDirection(ClueType direction) { this.direction = direction; }
}
