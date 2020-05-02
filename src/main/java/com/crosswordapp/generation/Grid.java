package com.crosswordapp.generation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Grid {
    public int height;
    public int width;

    public List<List<Square>> slots;
    // keys are of form "{rowNum}-{colNum}-{dirNum}"
    public Map<String, Word> words;

    public Grid(List<List<Character>> grid) {
        this.height = grid.size();
        this.width = grid.get(0).size();
        slots = new ArrayList<>();
        words = new HashMap<>();
        for (int row = 0; row < height; row++) {
            slots.add(new ArrayList<>());
            List<Square> curWord = new ArrayList<>();
            for (int col = 0; col < width; col++) {
                Character c = grid.get(row).get(col);
                slots.get(row).add(new Square(row, col, c));

                if (!c.equals('_')) {
                    curWord.add(new Square(row, col, c));
                }
                if (!curWord.isEmpty() && (c.equals('_') || col == width-1)) {
                    addWord(curWord, 0);
                    curWord = new ArrayList<>();
                }
            }
        }
        for (int col = 0; col < width; col++) {
            List<Square> curWord = new ArrayList<>();
            for (int row = 0; row < height; row++) {
                Character c = slots.get(row).get(col).val;
                if (!c.equals('_')) {
                    curWord.add(new Square(row, col, c));
                }
                if (!curWord.isEmpty() && (c.equals('_') || row == height-1)) {
                    addWord(curWord, 1);
                    curWord = new ArrayList<>();
                }
            }
        }
    }

    private void addWord(List<Square> word, int dir) {
        Word w = new Word(word.size(), word.get(0).r, word.get(0).c, dir);
        for (int i = 0; i < word.size(); i++) {
            w.slots[i].val = word.get(i).val;
            w.initialSetting[i] = word.get(i).val;
        }
        words.put(keyOfCoords(word.get(0).r, word.get(0).c, dir), w);
    }

    private String keyOfCoords(int r, int c, int dir) {
        return r + "-" + c + "-" + dir;
    }

    public void clear() {
        for (Word word : words.values()) {
            clearWord(word);
        }
    }

    public void clearWord(Word word) {
        for (int i = 0; i < word.len; i++) {
            if (word.dir == 0) {
                Square square = slots.get(word.row).get(word.col + i);
                square.val = word.initialSetting[i];
                setSquareOfWordInDirection(square, 1);
            } else {
                Square square = slots.get(word.row + i).get(word.col);
                square.val = word.initialSetting[i];
                setSquareOfWordInDirection(square, 0);
            }
            word.slots[i].val = word.initialSetting[i];
        }
    }

    public void setWord(Word word, String newWord) {
        for (int i = 0; i < word.len; i++) {
            if (word.dir == 0) {
                Square square = slots.get(word.row).get(word.col + i);
                square.val = newWord.charAt(i);
                setSquareOfWordInDirection(square, 1);
            } else {
                Square square = slots.get(word.row + i).get(word.col);
                square.val = newWord.charAt(i);
                setSquareOfWordInDirection(square, 0);
            }
            words.get(keyOfCoords(word.row,word.col,word.dir)).slots[i].val = newWord.charAt(i);
        }
    }

    private void setSquareOfWordInDirection(Square s, int dir) {
        int r = s.r;
        int c = s.c;
        String key = keyOfCoords(r, c, dir);
        while (!words.containsKey(key)) {
            if (dir == 0) {
                c -= 1;
            } else {
                r -= 1;
            }
            key = keyOfCoords(r, c, dir);
        }
        // now we are at beginning of a word, find index of word to change
        int i;
        if (dir == 0) {
            i = s.c - c;
        } else {
            i = s.r - r;
        }
        Word word = words.get(key);
        word.slots[i].val = s.val;
    }

    public void printGrid() {
        System.out.println("\n");
        for (int r = 0; r < height; r++) {
            String row = "";
            for (int c = 0; c < width; c++) {
                row += slots.get(r).get(c).val;
            }
            System.out.println(row);
        }
    }
}
