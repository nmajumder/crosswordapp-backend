package com.crosswordapp;

import com.crosswordapp.object.Clue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClueManager {
    Logger logger = LoggerFactory.getLogger(ClueManager.class);

    private static Pattern acrossPattern = Pattern.compile("[0-9]+-Across");
    private static Pattern downPattern = Pattern.compile("[0-9]+-Down");

    private Map<Integer, Clue> acrossClueMap = new HashMap<>();
    private Map<Integer, Clue> downClueMap = new HashMap<>();

    // keys and values here are of form A# or D#
    private Map<String, Set<String>> referenceMap = new HashMap<>();

    public ClueManager(List<Clue> acrossClues, List<Clue> downClues) {
        for (Clue clue: acrossClues) {
            acrossClueMap.put(clue.getNumber(), clue);
        }
        for (Clue clue: downClues) {
            downClueMap.put(clue.getNumber(), clue);
        }
        initClueReferences();
    }

    private void initClueReferences() {
        List<String> starredReferencers =  new ArrayList<>();
        Set<String> starredClues = new HashSet<>();

        for (Integer i: acrossClueMap.keySet()) {
            String text = acrossClueMap.get(i).getText();
            String key = "A" + i;
            // check for starred clue
            if (text.contains(" starred ")) {
                starredReferencers.add(key);
            } else if (text.startsWith("*")) {
                starredClues.add(key);
            }

            // check for explicit references
            Matcher acrossMatcher = acrossPattern.matcher(text);
            while (acrossMatcher.find()) {
                try {
                    Integer num = Integer.parseInt(acrossMatcher.group().split("-")[0]);
                    //logger.debug("Found reference to " + num + "-Across in clue: " + text);
                    if (referenceMap.containsKey(key)) {
                        referenceMap.get(key).add("A" + num);
                    } else {
                        referenceMap.put(key, new HashSet<>(Arrays.asList("A" + num)));
                    }
                } catch (NumberFormatException e) {
                    logger.error("Unable to parse across reference in text: " + text);
                }
            }
            Matcher downMatcher = downPattern.matcher(text);
            while (downMatcher.find()) {
                try {
                    Integer num = Integer.parseInt(downMatcher.group().split("-")[0]);
                    //logger.debug("Found reference to " + num + "-Down in clue: " + text);
                    if (referenceMap.containsKey(key)) {
                        referenceMap.get(key).add("D" + num);
                    } else {
                        referenceMap.put(key, new HashSet<>(Arrays.asList("D" + num)));
                    }
                } catch (NumberFormatException e) {
                    logger.error("Unable to parse down reference in text: " + text);
                }
            }
        }
        for (Integer i: downClueMap.keySet()) {
            String text = downClueMap.get(i).getText();
            String key = "D" + i;
            // check for starred clue
            if (text.contains(" starred ")) {
                starredReferencers.add(key);
            } else if (text.startsWith("*")) {
                starredClues.add(key);
            }

            // check for explicit references
            Matcher acrossMatcher = acrossPattern.matcher(text);
            while (acrossMatcher.find()) {
                try {
                    Integer num = Integer.parseInt(acrossMatcher.group().split("-")[0]);
                    //logger.debug("Found reference to " + num + "-Across in clue: " + text);
                    if (referenceMap.containsKey(key)) {
                        referenceMap.get(key).add("A" + num);
                    } else {
                        referenceMap.put(key, new HashSet<>(Arrays.asList("A" + num)));
                    }
                } catch (NumberFormatException e) {
                    logger.error("Unable to parse across reference in text: " + text);
                }
            }
            Matcher downMatcher = downPattern.matcher(text);
            while (downMatcher.find()) {
                try {
                    Integer num = Integer.parseInt(downMatcher.group().split("-")[0]);
                    //logger.debug("Found reference to " + num + "-Down in clue: " + text);
                    if (referenceMap.containsKey(key)) {
                        referenceMap.get(key).add("D" + num);
                    } else {
                        referenceMap.put(key, new HashSet<>(Arrays.asList("D" + num)));
                    }
                } catch (NumberFormatException e) {
                    logger.error("Unable to parse down reference in text: " + text);
                }
            }
        }

        // add references for starred clues
        if (starredReferencers.size() > 0 && starredClues.size() > 0) {
            // clue mentioning "starred" (themer) will reference all starred clues,
            // each starred clue will reference back to the themer
            for (String starredReferencer: starredReferencers) {
                if (!referenceMap.containsKey(starredReferencer)) {
                    referenceMap.put(starredReferencer, new HashSet<>());
                }
            }
            for (String key: starredClues) {
                for (String starredReferencer: starredReferencers) {
                    referenceMap.get(starredReferencer).add(key);
                }
                if (!referenceMap.containsKey(key)) {
                    referenceMap.put(key, new HashSet<>(starredReferencers));
                } else {
                    referenceMap.get(key).addAll(starredReferencers);
                }
            }
        } else if (starredReferencers.size() == 0 && starredClues.size() > 0) {
            // if themer does not exist, each starred clue will reference all the others
            for (String key: starredClues) {
                if (!referenceMap.containsKey(key)) {
                    referenceMap.put(key, new HashSet<>());
                }
                for (String other: starredClues) {
                    if (!other.equals(key)) {
                        referenceMap.get(key).add(other);
                    }
                }
            }
        }
    }

    public Map<Integer, Clue> getAcrossClueMap() {
        return acrossClueMap;
    }

    public void setAcrossClueMap(Map<Integer, Clue> acrossClueMap) {
        this.acrossClueMap = acrossClueMap;
    }

    public Map<Integer, Clue> getDownClueMap() {
        return downClueMap;
    }

    public void setDownClueMap(Map<Integer, Clue> downClueMap) {
        this.downClueMap = downClueMap;
    }

    public Map<String, Set<String>> getReferenceMap() {
        return referenceMap;
    }

    public void setReferenceMap(Map<String, Set<String>> referenceMap) {
        this.referenceMap = referenceMap;
    }
}
