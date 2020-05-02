package com.crosswordapp.bean.crossword;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

@XmlRootElement(name = "crosswordList")
@XmlAccessorType(XmlAccessType.FIELD)
public class CrosswordListBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "crossword")
    private List<CrosswordBean> crosswordList;

    public List<CrosswordBean> getCrosswordList() {
        return crosswordList;
    }

    public void setCrosswordList(List<CrosswordBean> crosswordList) {
        this.crosswordList = crosswordList;
    }
}
