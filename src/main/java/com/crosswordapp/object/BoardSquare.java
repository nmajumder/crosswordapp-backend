package com.crosswordapp.object;

public class BoardSquare {
    // the letter in this square (or "_" if black)
    private String value;

    // the index of row and col for this square
    private Integer rowCoord;
    private Integer colCoord;

    // the clue number in this square (or 0 if none)
    private Integer number = 0;

    // the clue number corresponding to this answer in both directions
    private Integer acrossClueNum = null;
    private Integer downClueNum = null;

    // this square's index in the answer each direction
    // will be 0 if it is the start of a word
    private Integer acrossWordIndex = null;
    private Integer downWordIndex = null;

    // whether the square has been revealed or checked
    private SquareCheckStatus status = SquareCheckStatus.Unchecked;

    public BoardSquare() {}

    public BoardSquare (String value, Integer rowCoord, Integer colCoord) {
        this.value = value;
        this.rowCoord = rowCoord;
        this.colCoord = colCoord;
    }

    public BoardSquare(String value, Integer rowCoord, Integer colCoord, Integer number, Integer acrossClueNum,
                       Integer acrossWordIndex, Integer downClueNum, Integer downWordIndex, SquareCheckStatus status) {
        this.value = value;
        this.rowCoord = rowCoord;
        this.colCoord = colCoord;
        this.number = number;
        this.acrossClueNum = acrossClueNum;
        this.acrossWordIndex = acrossWordIndex;
        this.downClueNum = downClueNum;
        this.downWordIndex = downWordIndex;
        this.status = status;
    }

    public String getValue() { return value; }

    public void setValue(String value) { this.value = value; }

    public Integer getRowCoord() { return rowCoord; }

    public void setRowCoord(Integer rowCoord) { this.rowCoord = rowCoord; }

    public Integer getColCoord() { return colCoord; }

    public void setColCoord(Integer colCoord) { this.colCoord = colCoord; }

    public Integer getNumber() { return number; }

    public void setNumber(Integer number) { this.number = number; }

    public Integer getAcrossClueNum() { return acrossClueNum; }

    public void setAcrossClueNum(Integer acrossClueNum) { this.acrossClueNum = acrossClueNum; }

    public Integer getDownClueNum() { return downClueNum; }

    public void setDownClueNum(Integer downClueNum) { this.downClueNum = downClueNum; }

    public Integer getAcrossWordIndex() { return acrossWordIndex; }

    public void setAcrossWordIndex(Integer acrossWordIndex) { this.acrossWordIndex = acrossWordIndex; }

    public Integer getDownWordIndex() { return downWordIndex; }

    public void setDownWordIndex(Integer downWordIndex) { this.downWordIndex = downWordIndex; }

    public SquareCheckStatus getStatus() { return status; }

    public void setStatus(SquareCheckStatus status) { this.status = status; }
}
