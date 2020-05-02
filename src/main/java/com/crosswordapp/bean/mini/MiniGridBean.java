package com.crosswordapp.bean.mini;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

@XmlRootElement(name = "grid")
@XmlAccessorType(XmlAccessType.FIELD)
public class MiniGridBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute
    private String black;

    @XmlElement(name = "row")
    private List<String> rows;

    public String getBlack() {
        return black;
    }

    public void setBlack(String black) {
        this.black = black;
    }

    public List<String> getRows() {
        return rows;
    }

    public void setRows(List<String> rows) {
        this.rows = rows;
    }
}
