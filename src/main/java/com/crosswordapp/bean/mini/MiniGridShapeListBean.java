package com.crosswordapp.bean.mini;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@XmlRootElement(name = "gridShapes")
@XmlAccessorType(XmlAccessType.FIELD)
public class MiniGridShapeListBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "grids")
    private List<MiniGridListBean> gridLists;

    public List<MiniGridListBean> getGridLists() {
        return gridLists;
    }

    public void setGridLists(List<MiniGridListBean> gridLists) {
        this.gridLists = gridLists;
    }
}
