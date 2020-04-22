package com.crosswordapp.object;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Board {
    private static Logger logger = LoggerFactory.getLogger(Board.class);

    public static String BLANK = "_";

    private int height;
    private int width;
    private List<List<BoardSquare>> grid;
    private List<List<String>> solution;

    private SquareSelection selection;

    private Integer numSeconds;

    public Board(List<String> xmlGridRows, Symmetry sym) {
        if (!xmlGridIsValid(xmlGridRows, sym)) {
            throw new RuntimeException("Failed to create new board, check logs for errors");
        }
        this.height = xmlGridRows.size();
        this.width = xmlGridRows.get(0).length();
        this.solution = new ArrayList<>();
        for (String row: xmlGridRows) {
            List<String> solutionRow = Arrays.asList(row.split(""));
            this.solution.add(solutionRow);
        }

        this.grid = new ArrayList<>();
        Integer counter = 1;
        for (int r = 0; r < this.height; r++) {
            this.grid.add(new ArrayList<>());
            for (int c = 0; c < this.width; c++) {
                String gridVal = this.solution.get(r).get(c).equals("_") ? "_" : "";
                this.grid.get(r).add(getBoardSquare(r, c, gridVal, counter));
                if (counter.equals(this.grid.get(r).get(c).getNumber())) {
                    counter++;
                }
            }
        }

        int c = 0;
        while (this.grid.get(0).get(c).getValue().equals("_")) {
            c++;
        }
        selection = new SquareSelection(0, c, ClueType.Across);

        numSeconds = 0;
    }

    private boolean xmlGridIsValid(List<String> rows, Symmetry sym) {
        if (rows == null || rows.size() < 1) {
            logger.error("Xml rows must not be empty");
            return false;
        }
        this.height = rows.size();
        this.width = rows.get(0).length();
        for (int r = 0; r < rows.size(); r++) {
            List<String> rowVals = Arrays.asList(rows.get(r).split(""));
            if (rowVals.size() != this.width) {
                logger.error("All rows must be the same length");
                return false;
            }
            for (int c = 0; c < rowVals.size(); c++) {
                if (rowVals.get(c).equals("_")) {
                    int xind = rowVals.size() - 1 - c;
                    int yind = sym.equals(Symmetry.DIAGONAL) ? rows.size() - 1 - r : r;
                    if (rows.get(yind).charAt(xind) != '_') {
                        logger.error("Blanks do not adhere to " + sym.toString() + " symmetry");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private BoardSquare getBoardSquare(int row, int col, String value, int counter) {
        if (value.equals("_")) {
            return new BoardSquare("_", row, col);
        }

        Integer acrossClueNum = null;
        Integer acrossWordInd = null;
        if (col == 0 || solution.get(row).get(col-1).equals("_")) {
            acrossClueNum = counter;
            acrossWordInd = 0;
        }
        Integer downWordInd = null;
        Integer downClueNum = null;
        if (row == 0 || solution.get(row-1).get(col).equals("_")) {
            downClueNum = counter;
            acrossWordInd = 0;
        }

        // set the clue number on this square if any
        Integer thisNumber = 0;
        if (acrossClueNum != null || downClueNum != null) {
            thisNumber = counter;
        }

        if (acrossClueNum == null) {
            acrossWordInd = 0;
            int r = row;
            int c = col-1;
            while (c >= 0 && !solution.get(r).get(c).equals("_")) {
                if (grid.get(r).get(c).getNumber().intValue() > 0) {
                    acrossClueNum = grid.get(r).get(c).getNumber();
                }
                c--;
                acrossWordInd++;
            }
            acrossWordInd--;
            if (acrossClueNum == null) {
                logger.error("Unable to find across clue num at coords (" + row + "," + col + ")");
            }
        }
        if (downClueNum == null) {
            downWordInd = 0;
            int r = row-1;
            int c = col;
            while (r >= 0 && !solution.get(r).get(c).equals("_")) {
                if (grid.get(r).get(c).getNumber().intValue() > 0) {
                    downClueNum = grid.get(r).get(c).getNumber();
                }
                r--;
                downWordInd++;
            }
            downWordInd--;
            if (downClueNum == null) {
                logger.error("Unable to find down clue num at coords (" + row + "," + col + ")");
            }
        }

        return new BoardSquare(value, row, col, thisNumber, acrossClueNum,
                acrossWordInd, downClueNum, downWordInd, SquareCheckStatus.Unchecked);
    }

    public boolean setGrid(List<List<BoardSquare>> newGrid) {
        if (!gridIsValid(newGrid)) {
            return false;
        }
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                this.grid.get(y).set(x, newGrid.get(y).get(x));
            }
        }
        return true;
    }

    public List<List<BoardSquare>> getGrid() {
        return grid;
    }

    public BoardSquare getBoardSquareAtIndex(int r, int c) {
        if (!indexIsValid(r, c)) {
            logger.error("Invalid grid coordinates (" + r + "," + c + ")");
            return null;
        }
        return this.grid.get(r).get(c);
    }

    public List<List<String>> getSolution() {
        return solution;
    }

    private boolean gridIsValid(List<List<BoardSquare>> newGrid) {
        if (newGrid.size() != this.height) {
            logger.error("Error matching height of new grid, expected " + this.height +
                    " but found " + newGrid.size());
            return false;
        }
        for (int y = 0; y < this.height; y++) {
            if (newGrid.get(y).size() != this.width) {
                logger.error("Error matching new grid width on row " + y + ", expected " +
                        this.width + ", but found " + newGrid.get(y).size());
                return false;
            }
            for (int x = 0; x < this.width; x++) {
                String c1 = this.grid.get(y).get(x).getValue();
                String c2 = newGrid.get(y).get(x).getValue();
                if ((BLANK.equals(c1) && !BLANK.equals(c2)) ||
                        (BLANK.equals(c2) && !BLANK.equals(c1))) {
                    logger.error("Error matching blank squares on new grid on " + y + "," + x);
                    return false;
                }
            }
        }
        return true;
    }

    public boolean gridIsSolved() {
        for (int r = 0; r < this.height; r++) {
            for (int c = 0; c < this.width; c++) {
                if (!grid.get(r).get(c).getValue().equals(this.solution.get(r).get(c))) {
                    return false;
                }
            }
        }
        return true;
    }

    public Pair<Integer, Integer> getCoordinatesOfNumber(Integer clueNumber) {
        if (clueNumber.intValue() > 0) {
            for (int r = 0; r < this.height; r++) {
                for (int c = 0; c < this.width; c++) {
                    if (clueNumber.equals(this.grid.get(r).get(c).getNumber())) {
                        return new Pair<>(r, c);
                    }
                }
            }
        }
        logger.error("Unable to find coordinates of clue number: " + clueNumber);
        return null;
    }

    public Integer getLengthOfAnswer(Integer clueNumber, ClueType type) {
        Pair<Integer, Integer> coords = getCoordinatesOfNumber(clueNumber);
        if (coords == null) {
            logger.error("Unable to get answer length of nonexistent clue number: " + clueNumber);
            return null;
        }
        int r = coords.getValue0();
        int c = coords.getValue1();
        int len = 0;
        if (type.equals(ClueType.Across)) {
            while (c < this.width && !this.solution.get(r).get(c).equals("_")) {
                len++;
                c++;
            }
        } else {
            while (r < this.height && !this.solution.get(r).get(c).equals("_")) {
                len++;
                r++;
            }
        }
        return len;
    }

    private boolean indexIsValid(int row, int col) {
        return row < this.height && row >= 0 && col < this.width && col >= 0;
    }


    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public SquareSelection getSelection() { return selection; }

    public void setSelection(SquareSelection selection) { this.selection = selection; }

    public Integer getNumSeconds() { return numSeconds; }

    public void setNumSeconds(Integer numSeconds) { this.numSeconds = numSeconds; }

    /***** CHECK, REVEAL, CLEAR FUNCTIONS *****/

    public void check(CheckType type) {
        switch (type) {
            case Square:
                checkSquare(this.selection.getRowCoord(), this.selection.getColCoord());
                break;
            case Word:
                checkWord();
                break;
            case Puzzle:
                checkPuzzle();
                break;
        }
    }

    public void reveal(CheckType type) {
        switch (type) {
            case Square:
                revealSquare(this.selection.getRowCoord(), this.selection.getColCoord());
                break;
            case Word:
                revealWord();
                break;
            case Puzzle:
                revealPuzzle();
                break;
        }
    }

    public void clear(CheckType type) {
        switch (type) {
            case Square:
                logger.error("Clearing a square is not supported (just hit the delete key)");
                break;
            case Word:
                clearWord();
                break;
            case Puzzle:
                clearPuzzle();
                break;
        }
    }

    private void checkSquare(int r, int c) {
        BoardSquare square = this.grid.get(r).get(c);
        if (square.getValue().equals("") || square.getValue().equals("_")) {
            return;
        } else if (square.getValue().equals(this.solution.get(r).get(c))) {
            logger.info("Found correct check, setting status to CheckedTrue");
            square.setStatus(SquareCheckStatus.CheckedTrue);
        } else {
            logger.info("Found incorrect check, setting status to CheckedFalse");
            square.setStatus(SquareCheckStatus.CheckedFalse);
        }
    }

    private void revealSquare(int r, int c) {
        BoardSquare square = this.grid.get(r).get(c);
        if (square.getValue().equals("_")) {
            return;
        }
        square.setValue(this.solution.get(r).get(c));
        square.setStatus(SquareCheckStatus.Revealed);
    }

    private void checkWord() {
        int r = this.selection.getRowCoord();
        int c = this.selection.getColCoord();
        if (this.selection.getDirection().equals(ClueType.Across)) {
            int wordInd = this.grid.get(r).get(c).getAcrossWordIndex();
            int x = c - wordInd;
            while (x < this.width && !this.grid.get(r).get(x).getValue().equals("_")) {
                checkSquare(r, x);
                x++;
            }
        } else {
            int wordInd = this.grid.get(r).get(c).getDownWordIndex();
            int y = r - wordInd;
            while (y < this.height && !this.grid.get(y).get(c).getValue().equals("_")) {
                checkSquare(y, c);
                y++;
            }
        }
    }

    private void revealWord() {
        int r = this.selection.getRowCoord();
        int c = this.selection.getColCoord();
        if (this.selection.getDirection().equals(ClueType.Across)) {
            int wordInd = this.grid.get(r).get(c).getAcrossWordIndex();
            int x = c - wordInd;
            while (x < this.width && !this.grid.get(r).get(x).getValue().equals("_")) {
                revealSquare(r, x);
                x++;
            }
        } else {
            int wordInd = this.grid.get(r).get(c).getDownWordIndex();
            int y = r - wordInd;
            while (y < this.height && !this.grid.get(y).get(c).getValue().equals("_")) {
                revealSquare(y, c);
                y++;
            }
        }
    }

    private void clearWord() {
        int r = this.selection.getRowCoord();
        int c = this.selection.getColCoord();
        if (this.selection.getDirection().equals(ClueType.Across)) {
            int wordInd = this.grid.get(r).get(c).getAcrossWordIndex();
            int x = c - wordInd;
            while (x < this.width && !this.grid.get(r).get(x).getValue().equals("_")) {
                this.grid.get(r).get(c).setValue("");
                x++;
            }
        } else {
            int wordInd = this.grid.get(r).get(c).getDownWordIndex();
            int y = r - wordInd;
            while (y < this.height && !this.grid.get(y).get(c).getValue().equals("_")) {
                this.grid.get(y).get(c).setValue("");
                y++;
            }
        }
    }

    private void checkPuzzle() {
        for (int r = 0; r < this.height; r++) {
            for (int c = 0; c < this.width; c++) {
                checkSquare(r, c);
            }
        }
    }

    private void revealPuzzle() {
        for (int r = 0; r < this.height; r++) {
            for (int c = 0; c < this.width; c++) {
                revealSquare(r, c);
            }
        }
    }

    private void clearPuzzle() {
        for (int r = 0; r < this.height; r++) {
            for (int c = 0; c < this.width; c++) {
                this.grid.get(r).get(c).setValue("");
            }
        }
    }
}
