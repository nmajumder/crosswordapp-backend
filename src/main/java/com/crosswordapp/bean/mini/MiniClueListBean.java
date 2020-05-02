package com.crosswordapp.bean.mini;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@XmlRootElement(name = "clueList")
@XmlAccessorType(XmlAccessType.FIELD)
public class MiniClueListBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "clue")
    private List<MiniClueBean> clueList;

    public List<MiniClueBean> getClueList() {
        return clueList;
    }

    public void setClueList(List<MiniClueBean> clueList) {
        this.clueList = clueList;
    }
}
