package com.crosswordapp.generation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.time.Instant;
import java.util.*;

public class GenerationApp {
    private static Logger logger = LoggerFactory.getLogger(GenerationApp.class);

    public static Lexicon lex;

    private static Instant startTime = null;
    private static Boolean timedOut = false;

    public static void initializeGenerationApp() {
        lex = new Lexicon();
    }

    public static Grid generateBoard(List<List<Character>> gridChars) {
        timedOut = false;
        startTime = Instant.now();

        Grid grid = new Grid(gridChars);
        grid.printGrid();

        Deque<Assignment> solution = new ArrayDeque<>();
        List<Integer> chosenValues = new ArrayList<>();
        List<Word> entryOptions = new ArrayList<>();
        for (Word w : grid.words.values()) {
            entryOptions.add(w);
        }
        boolean solved = solve(grid, entryOptions, chosenValues, solution);

        if (solved) {
            grid.printGrid();
            String solveTime = String.valueOf((Instant.now().toEpochMilli() - startTime.toEpochMilli()) / 1000);
            logger.info("Puzzle created in " + solveTime + " seconds");
            return grid;
        } else {
            if (timedOut) {
                logger.info("Could not find solution in time limit");
            } else {
                logger.info("No solution found");
            }
            return null;
        }
    }

    private static boolean solve(Grid grid, List<Word> entryOptions, List<Integer> chosenValues, Deque<Assignment> solution) {
        // check for timeout (15 seconds)
        if (timedOut) return false;
        if (Instant.now().toEpochMilli() - startTime.toEpochMilli() > 12000) {
            timedOut = true;
            grid.printGrid();
            return false;
        }

        // choose slot to fill
        Word chosenEntry = chooseEntry(entryOptions, chosenValues);

        // find words that could fill that slot
        List<Integer> valueOptions = lex.findMatches(chosenEntry.toString(), chosenValues);

        // choose from among these options
        while (!valueOptions.isEmpty()) {
            int chosenValue = chooseValue(grid, chosenEntry, valueOptions, chosenValues);
            //.out.println("Chose value: " + lex.getWord(chosenValue));
            Assignment assignment = new Assignment(chosenEntry, chosenValue);
            grid.setWord(chosenEntry, lex.getWord(chosenValue));
            chosenValues.add(chosenValue);
            solution.push(assignment);
            //grid.printGrid();

            if (entryOptions.isEmpty()) {
                valueOptions = null;
                return true;
            }

            List<Integer> chosenClone = new ArrayList<>(chosenValues);
            List<Word> entryOptionsClone = new ArrayList<>(entryOptions);
            boolean result = solve(grid, entryOptionsClone, chosenClone, solution);
            chosenClone = null;
            entryOptionsClone = null;

            // if we reach here through success we have solved
            if (result) {
                valueOptions = null;
                return true;
            } else {
                if (timedOut) return false;
                // if we reach here through failure, roll back to prev grid
                chosenValues.remove(chosenValues.size()-1);
                solution.pop();
                grid.clear();
                Iterator<Assignment> solutionIt = solution.iterator();
                while (solutionIt.hasNext()) {
                    Assignment a = solutionIt.next();
                    grid.setWord(a.entry, lex.getWord(a.value));
                }
            }
        }
        // if we get here we are out of options so return false
        valueOptions = null;
        return false;
    }

    private static Word chooseEntry(List<Word> entryOptions, List<Integer> chosenValues) {
        Word nextWord = null;
        Word curWord = null;
        int minOptions = -1;
        int curOptions = -1;
        int ind = 0;
        int totSpaces = entryOptions.size();
        for (int i = 0; i < totSpaces; i++) {
            curWord = entryOptions.get(i);
            curOptions = lex.findMatches(curWord.toString(), chosenValues).size();
            if (curOptions < minOptions || minOptions == -1) {
                minOptions = curOptions;
                nextWord = curWord;
                ind = i;
            }
        }
        //System.out.println("Found entry: " + entryOptions.get(ind) + " aka next word: " + nextWord + " with options: " + minOptions);
        entryOptions.remove(ind);
        return nextWord;
    }

    private static int chooseValue(Grid grid, Word entry, List<Integer> valueOptions, List<Integer> chosenValues) {
        int numChoices = 10; // number of random choices to compare against each other
        List<Integer> valInds = new ArrayList<>();
        for (int i = 0; i < numChoices; i++) {
            Integer r = (int) Math.floor(Math.random() * valueOptions.size());
            if (r >= valueOptions.size()) {
                throw new RuntimeException("Randomly selected a value that was too large for the value options array: " + r);
            }
            valInds.add(r);
        }

        String initPattern = entry.toString();
        List<String> patterns = new ArrayList<>();
        List<Integer> options = new ArrayList<>();
        List<Word> words = new ArrayList<>(grid.words.values());
        for (Word word : words) {
            patterns.add(word.toString());
            int numOptions = lex.findMatches(word.toString(), chosenValues).size();
            options.add(numOptions);
        }

        int maxSum = 0;
        int bestWordInd = -1;
        for (int i = 0; i < numChoices; i++) {
            String word = lex.getWord(valueOptions.get(valInds.get(i)));
            grid.setWord(entry, word);
            int sum = 0;
            for (int j = 0; j < words.size(); j++) {
                if (words.get(j).toString().equals(patterns.get(j))) {
                    sum += options.get(j);
                } else {
                    int numOptions = lex.findMatches(words.get(j).toString(), chosenValues).size();
                    sum += numOptions;
                }
            }
            if (sum > maxSum) {
                maxSum = sum;
                bestWordInd = valInds.get(i);
            }
        }
        grid.setWord(entry, initPattern);

        //System.out.println("Choosing value " + lex.getWord(valueOptions.get(bestWordInd)) + " for entry " + entry.toString());

        if (bestWordInd == -1) {
            bestWordInd = (int) Math.floor(Math.random() * valueOptions.size());
        }
        int bestWordKey = valueOptions.get(bestWordInd);
        valueOptions.remove(bestWordInd);
        return bestWordKey;
    }
}
