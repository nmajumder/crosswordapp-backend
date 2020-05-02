package com.crosswordapp.bean.crossword;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

@XmlRootElement(name = "grid")
@XmlAccessorType(XmlAccessType.FIELD)
public class GridBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "row")
    List<String> rows;

    public List<String> getRows() {
        return rows;
    }

    public void setRows(List<String> rows) {
        this.rows = rows;
    }
}
