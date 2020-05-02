package com.crosswordapp.generation;

public class Word {
    public int len;
    public int row;
    public int col;
    public int dir;
    public Square[] slots;
    public Character[] initialSetting;

    public Word(int len, int startRow, int startCol, int dir) {
        this.len = len;
        this.row = startRow;
        this.col = startCol;
        this.dir = dir;
        this.slots = new Square[len];
        this.initialSetting = new Character[len];
        for (int i = 0; i < len; i++) {
            if (dir == 0) {
                slots[i] = new Square(startRow, startCol + i, '?');
                initialSetting[i] = '?';
            } else {
                slots[i] = new Square(startRow + i, startCol, '?');
                initialSetting[i] = '?';
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(slots[i].val);
        }
        return sb.toString();
    }
}
