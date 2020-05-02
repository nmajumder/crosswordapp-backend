package com.crosswordapp.generation;

import com.crosswordapp.StaticMiniClueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Lexicon {
    private static Logger logger = LoggerFactory.getLogger(Lexicon.class);

    private static int maxWordLen = StaticMiniClueService.MAX_WORD_LEN;

    private static List<String> wordList;
    private static List<List<Integer>> fullLetterLists;
    private static List<List<List<List<Integer>>>> indexLists;

    public Lexicon() {
        indexLists = new ArrayList<>();
        for (int i = 0; i <= maxWordLen; i++) {
            List<List<List<Integer>>> nLetterWords = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                List<List<Integer>> nthLetterArray = new ArrayList<>();
                for (int k = 0; k < 26; k++) {
                    nthLetterArray.add(new ArrayList<>());
                }
                nLetterWords.add(nthLetterArray);
            }
            indexLists.add(nLetterWords);
        }

        initWordList();
        logger.info("Read in " + wordList.size() + " words from file");
    }

    private static void initWordList() {
        wordList = new ArrayList<>();
        fullLetterLists = new ArrayList<>();
        for (int i = 0; i <= maxWordLen; i++) {
            fullLetterLists.add(new ArrayList<>());
        }
        int ind = 0;
        for (String word: StaticMiniClueService.getSortedWordList()) {
            wordList.add(word);
            for (int letter = 0; letter < word.length(); letter++) {
                indexLists.get(word.length()).get(letter).get(word.charAt(letter) - 'A').add(ind);
            }
            fullLetterLists.get(word.length()).add(ind);
            ind++;
        }
    }

    public List<Integer> findMatches(String pattern, List<Integer> choices) {
        List<Integer> matches = new ArrayList<>();
        boolean started = false;
        int patternLen = pattern.length();
        for (int letter = 0; letter < patternLen; letter++) {
            if (pattern.charAt(letter) != '?') {
                if (!started) {
                    List<Integer> rawMatches = indexLists.get(patternLen).get(letter).get(pattern.charAt(letter)-'A');
                    for (int i = 0; i < rawMatches.size(); i++) {
                        matches.add(rawMatches.get(i));
                    }
                    started = true;
                } else {
                    List<Integer> newMatches = intersection(matches, indexLists.get(patternLen).get(letter).get(pattern.charAt(letter)-'A'));
                    matches = newMatches;
                }
            }
        }
        if (!started) {
            List<Integer> rawMatches = fullLetterLists.get(patternLen);
            matches.addAll(rawMatches);
        }
        List<Integer> sortedChoices = new ArrayList<>(choices);
        sortedChoices.sort(Comparator.naturalOrder());
        List<Integer> result = difference(matches, sortedChoices);
        return result;
    }

    private List<Integer> intersection(List<Integer> l1, List<Integer> l2) {
        Set<Integer> s1 = new HashSet<>(l1);
        Set<Integer> s2 = new HashSet<>(l2);
        s1.retainAll(s2);
        return new ArrayList<>(s1);
    }

    private List<Integer> difference(List<Integer> l1, List<Integer> l2) {
        Set<Integer> s1 = new HashSet<>(l1);
        Set<Integer> s2 = new HashSet<>(l2);
        s1.removeAll(s2);
        return new ArrayList<>(s1);
    }

    public String getWord(int i) {
        return i < wordList.size() ? wordList.get(i) : "";
    }
}
