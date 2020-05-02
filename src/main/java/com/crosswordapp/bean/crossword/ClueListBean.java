package com.crosswordapp.bean.crossword;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

@XmlRootElement(name = "clueList")
@XmlAccessorType(XmlAccessType.FIELD)
public class ClueListBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "clue")
    List<ClueBean> clueList;

    public List<ClueBean> getClueList() { return clueList; }

    public void setClueList(List<ClueBean> clueList) {
        this.clueList = clueList;
    }
}
