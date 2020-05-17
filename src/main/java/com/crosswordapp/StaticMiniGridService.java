package com.crosswordapp;

import com.crosswordapp.bean.crossword.GridBean;
import com.crosswordapp.bean.mini.MiniGridBean;
import com.crosswordapp.bean.mini.MiniGridListBean;
import com.crosswordapp.bean.mini.MiniGridShapeListBean;
import com.crosswordapp.object.MiniDifficulty;
import com.crosswordapp.object.MiniGridTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.*;

public class StaticMiniGridService {

    private static class GridComparator implements Comparator<MiniGridTemplate> {
        @Override
        public int compare(MiniGridTemplate o1, MiniGridTemplate o2) {
            return o1.getNumBlack() - o2.getNumBlack();
        }
    }

    static Logger logger = LoggerFactory.getLogger(StaticMiniGridService.class);

    private static final String gridXmlFile = "mini-grid-shapes.xml";

    private static Map<Integer, List<MiniGridTemplate>> miniGridMap;

    public static void initializeMiniGridService() {
        try {
            MiniGridShapeListBean gridListsBean = deserializeFromXml(gridXmlFile);
            initializeMiniGridMap(gridListsBean);
            logger.info("Successfully finished initializing mini grid service");
        } catch(JAXBException e) {
            logger.error("Unable to parse xml file: " + gridXmlFile, e);
            throw new RuntimeException(e);
        }
    }

    public static MiniGridTemplate getMiniGridTemplate(Integer size, MiniDifficulty difficulty) {
        List<MiniGridTemplate> grids = miniGridMap.get(size);
        if (grids == null || grids.size() == 0) {
            logger.error("Unable to find any grids of size " + size);
            return null;
        }
        double rand = Math.random();
        if (difficulty.equals(MiniDifficulty.Hard)) {
            // move 10% closer to 0 (fewer blacks in grid)
            rand *= .9;
        } else if (difficulty.equals(MiniDifficulty.Easy)) {
            // move 10% closer to 1 (more blacks in grid)
            rand += (1-rand) * .1;
        }
        int ind = (int) Math.floor(rand * grids.size());
        return grids.get(ind);
    }

    public static Map<Integer, List<MiniGridTemplate>> getMiniGridMap() {
        return miniGridMap;
    }

    private static void initializeMiniGridMap(MiniGridShapeListBean gridListsBean) {
        miniGridMap = new HashMap<>();
        for (MiniGridListBean sizeListBean: gridListsBean.getGridLists()) {
            Integer size = Integer.parseInt(sizeListBean.getSize());
            List<MiniGridTemplate> gridList = new ArrayList<>();
            for (MiniGridBean gridBean: sizeListBean.getGrids()) {
                Integer numBlack = findNumBlackInGrid(gridBean);
                MiniGridTemplate miniGrid = new MiniGridTemplate(size, numBlack, gridBean.getRows());
                gridList.add(miniGrid);
            }
            Collections.sort(gridList, new GridComparator());
            miniGridMap.put(size, gridList);
            logger.info("Read in " + gridList.size() + " mini grids of size " + size);
        }
    }

    private static int findNumBlackInGrid(MiniGridBean grid) {
        int black = 0;
        for (String row: grid.getRows()) {
            for (char c: row.toCharArray()) {
                if (c == '_') {
                    black++;
                }
            }
        }
        return black;
    }

    private static MiniGridShapeListBean deserializeFromXml(String fileName) throws JAXBException {
        logger.info("Deserializing file: " + fileName);
        JAXBContext context = JAXBContext.newInstance(MiniGridShapeListBean.class);
        Unmarshaller un = context.createUnmarshaller();
        InputStream is = StaticMiniGridService.class.getClassLoader()
                .getResourceAsStream(fileName);
        MiniGridShapeListBean gridList = (MiniGridShapeListBean) un.unmarshal(is);

        return gridList;
    }
}
