package com.crosswordapp;

import com.crosswordapp.bean.crossword.ClueBean;
import com.crosswordapp.bean.crossword.CrosswordBean;
import com.crosswordapp.bean.crossword.CrosswordListBean;
import com.crosswordapp.object.Board;
import com.crosswordapp.object.Clue;
import com.crosswordapp.object.ClueType;
import com.crosswordapp.object.Crossword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.*;

public class StaticCrosswordService {
    static Logger logger = LoggerFactory.getLogger(StaticCrosswordService.class);

    static Map<String, Crossword> crosswordMap;

    static {
        crosswordMap = new HashMap<>();
    }

    public static Map<String, Crossword> getCrosswordMap() {
        return crosswordMap;
    }

    public static Crossword getCrossword(String id) {
        return crosswordMap.containsKey(id) ? crosswordMap.get(id) : null;
    }

    public static void initializeCrosswords() {
        try {
            CrosswordListBean crosswordListBean = deserializeFromXml();
            initializeStaticCrosswordList(crosswordListBean);
            logger.info("Successfully initialized " + StaticCrosswordService.getCrosswordMap().size()
                    + " crosswords from xml source");
        } catch (JAXBException e) {
            logger.error("Unable to parse crosswords xml file", e);
            throw new RuntimeException(e);
        }
    }

    private static void initializeStaticCrosswordList(CrosswordListBean clb) {
        for (CrosswordBean crb: clb.getCrosswordList()) {
            logger.info("Initializing crossword: " + crb.getTitle());
            Board board = new Board(crb.getGrid().getRows(), crb.getSymmetry());
            List<Clue> acrossClues = new ArrayList<>();
            for (ClueBean clueBean: crb.getAcrossClues().getClueList()) {
                Clue acrossClue = new Clue(clueBean.getClue(), ClueType.Across, clueBean.getNumber(),
                        board.getCoordinatesOfNumber(clueBean.getNumber()),
                        board.getLengthOfAnswer(clueBean.getNumber(), ClueType.Across));
                acrossClues.add(acrossClue);
            }
            List<Clue> downClues = new ArrayList<>();
            for (ClueBean clueBean: crb.getDownClues().getClueList()) {
                Clue downClue = new Clue(clueBean.getClue(), ClueType.Down, clueBean.getNumber(),
                        board.getCoordinatesOfNumber(clueBean.getNumber()),
                        board.getLengthOfAnswer(clueBean.getNumber(), ClueType.Down));
                downClues.add(downClue);
            }
            logger.info("Found {} across clues and {} down clues", acrossClues.size(), downClues.size());
            ClueManager clueManager = new ClueManager(acrossClues, downClues);
            Crossword crossword = new Crossword(crb.getId(), crb.getTitle(), crb.getDate(), crb.getDifficulty(), board, clueManager);
            crosswordMap.put(crossword.getId(), crossword);
        }
    }

    private static CrosswordListBean deserializeFromXml() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(CrosswordListBean.class);
        Unmarshaller un = context.createUnmarshaller();
        InputStream is = StaticCrosswordService.class.getClassLoader()
                .getResourceAsStream("crosswords.xml");
        CrosswordListBean crosswordList = (CrosswordListBean) un.unmarshal(is);

        return crosswordList;
    }
}
