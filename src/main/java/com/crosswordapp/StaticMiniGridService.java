package com.crosswordapp;

import com.crosswordapp.bean.mini.MiniGridBean;
import com.crosswordapp.bean.mini.MiniGridListBean;
import com.crosswordapp.bean.mini.MiniGridShapeListBean;
import com.crosswordapp.bean.mini.WordClueEntryListBean;
import com.crosswordapp.object.Mini;
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

    private static void initializeMiniGridMap(MiniGridShapeListBean gridListsBean) {
        miniGridMap = new HashMap<>();
        for (MiniGridListBean sizeListBean: gridListsBean.getGridLists()) {
            Integer size = Integer.parseInt(sizeListBean.getSize());
            List<MiniGridTemplate> gridList = new ArrayList<>();
            Map<Integer, List<MiniGridTemplate>> numBlackToGrids = new HashMap<>();
            int maxSetSize = 0;
            for (MiniGridBean gridBean: sizeListBean.getGrids()) {
                Integer numBlack = Integer.parseInt(gridBean.getBlack());
                MiniGridTemplate miniGrid = new MiniGridTemplate(size, numBlack, gridBean.getRows());
                gridList.add(miniGrid);

                Integer category = numBlack/2;
                if (numBlackToGrids.containsKey(category)) {
                    numBlackToGrids.get(category).add(miniGrid);
                } else {
                    numBlackToGrids.put(category, new ArrayList<>(Arrays.asList(miniGrid)));
                }
                if (numBlackToGrids.get(category).size() > maxSetSize)
                    maxSetSize = numBlackToGrids.get(category).size();
            }
            // this normalizes the distribution of blacks in the grid chosen by duplicating ones that have
            // an abnormal amount of black squares
            for (Integer category: numBlackToGrids.keySet()) {
                List<MiniGridTemplate> grids = numBlackToGrids.get(category);
                int numGrids = grids.size();
                while (numGrids <= maxSetSize - grids.size()) {
                    for (MiniGridTemplate grid: grids) {
                        gridList.add(grid);
                        numGrids++;
                    }
                }
            }
            Collections.sort(gridList, new GridComparator());
            miniGridMap.put(size, gridList);
            logger.info("Read in " + gridList.size() + " mini grids of size " + size);
        }
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
