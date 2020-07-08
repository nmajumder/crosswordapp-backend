package com.crosswordapp;

import com.crosswordapp.bean.mini.MiniClueBean;
import com.crosswordapp.bean.mini.WordClueEntryBean;
import com.crosswordapp.bean.mini.WordClueEntryListBean;
import com.crosswordapp.object.MiniClue;
import com.crosswordapp.object.MiniClueDay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StaticMiniClueService {

    private static class WordComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.length() == o2.length() ? o1.compareTo(o2) : o1.length() - o2.length();
        }
    }

    static Logger logger = LoggerFactory.getLogger(StaticMiniClueService.class);

    private static final String fileFormat = "mini-words_%s.xml";
    public static final int MAX_WORD_LEN = 9;

    private static Pattern acrossPattern = Pattern.compile("[0-9]+-Across");
    private static Pattern downPattern = Pattern.compile("[0-9]+-Down");

    private static Map<String, List<MiniClue>> wordClueMap;
    private static List<String> sortedWordList;

    public static void initializeMiniClueService() {
        wordClueMap = new HashMap<>();
        sortedWordList = new ArrayList<>();
        int numWords = 0;
        for (int i = 3; i <= MAX_WORD_LEN; i++) {
            String xmlFile = String.format(fileFormat, i);
            try {
                WordClueEntryListBean wordClueList = deserializeFromXml(xmlFile);
                initializeMiniWordClueMap(wordClueList);
                logger.info("Successfully read in " + (wordClueMap.size() - numWords) + " words from xml file: " + xmlFile);
                numWords = wordClueMap.size();
            } catch(JAXBException e) {
                logger.error("Unable to parse xml file: " + xmlFile, e);
                throw new RuntimeException(e);
            }
        }
    }

    private static void initializeMiniWordClueMap(WordClueEntryListBean wordClueList) {
        for (WordClueEntryBean entry : wordClueList.getEntryList()) {
            String word = entry.getWord().toUpperCase();
            sortedWordList.add(word);
            List<MiniClue> clueList = new ArrayList<>();
            for (MiniClueBean clue : entry.getClues().getClueList()) {
                MiniClueDay day = MiniClueDay.valueOf(clue.getDay());
                if (day == null) {
                    logger.error("Unable to parse day from: " + clue.getDay());
                    continue;
                }
                if (shouldKeepClue(clue.getText())) {
                    clue.setText(cleanUpClue(clue.getText()));
                    clueList.add(new MiniClue(day, clue.getText()));
                } else {
                    //logger.warn("For word: " + word + ", throwing away clue: " + clue.getText());
                }
            }
            wordClueMap.put(word, clueList);
        }
        Collections.sort(sortedWordList, new WordComparator());
    }

    private static boolean shouldKeepClue(String clueText) {
        Matcher acrossMatcher = acrossPattern.matcher(clueText);
        if (acrossMatcher.find()) return false;

        Matcher downMatcher = downPattern.matcher(clueText);
        if (downMatcher.find()) return false;

        if (clueText.toLowerCase().contains("this puzzle")) return false;
        if (clueText.toLowerCase().contains("theme") && clueText.toLowerCase().contains("puzzle")) return false;
        if (clueText.contains("&lt;-")) return false;
        if (clueText.contains("*")) return false;
        if (clueText.startsWith("...")) return false;

        return true;
    }

    private static String cleanUpClue(String clueText) {
        if (!clueText.contains("_")) return clueText;

        int firstUnderscore = clueText.indexOf("_");
        if (firstUnderscore != 0) {
            char prevChar = clueText.charAt(firstUnderscore - 1);
            if (Character.isLetter(prevChar) || Character.isDigit(prevChar)) {
                StringBuilder sb = new StringBuilder();
                sb.append(clueText, 0, firstUnderscore);
                sb.append(" ");
                sb.append(clueText, firstUnderscore, clueText.length());
                clueText = sb.toString();
            }
        }
        int lastUnderscore = clueText.lastIndexOf("_");
        if (lastUnderscore != clueText.length()-1) {
            char nextChar = clueText.charAt(lastUnderscore + 1);
            if (Character.isLetter(nextChar) || Character.isDigit(nextChar)) {
                StringBuilder sb = new StringBuilder();
                sb.append(clueText, 0, lastUnderscore+1);
                sb.append(" ");
                sb.append(clueText, lastUnderscore+1, clueText.length());
                clueText = sb.toString();
            }
        }
        return clueText;
    }

    private static WordClueEntryListBean deserializeFromXml(String fileName) throws JAXBException {
        logger.info("Deserializing file: " + fileName);
        JAXBContext context = JAXBContext.newInstance(WordClueEntryListBean.class);
        Unmarshaller un = context.createUnmarshaller();
        InputStream is = StaticMiniClueService.class.getClassLoader()
                .getResourceAsStream(fileName);
        WordClueEntryListBean wordClueList = (WordClueEntryListBean) un.unmarshal(is);

        return wordClueList;
    }

    public static Map<String, List<MiniClue>> getWordClueMap() { return wordClueMap; }

    public static void setWordClueMap(Map<String, List<MiniClue>> wordClueMap) { StaticMiniClueService.wordClueMap = wordClueMap; }

    public static List<String> getSortedWordList() { return sortedWordList; }

    public static void setSortedWordList(List<String> sortedWordList) { StaticMiniClueService.sortedWordList = sortedWordList; }
}
