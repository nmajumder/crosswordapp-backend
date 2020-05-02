package com.crosswordapp.bean.mini;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

@XmlRootElement(name = "grids")
@XmlAccessorType(XmlAccessType.FIELD)
public class MiniGridListBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute
    private String size;

    @XmlElement(name = "grid")
    private List<MiniGridBean> grids;

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public List<MiniGridBean> getGrids() {
        return grids;
    }

    public void setGrids(List<MiniGridBean> grids) {
        this.grids = grids;
    }
}
