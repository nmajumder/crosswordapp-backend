package com.crosswordapp.bean;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

@XmlRootElement(name = "clue")
@XmlAccessorType(XmlAccessType.FIELD)
public class ClueBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "num")
    Integer number;

    @XmlElement(name = "text")
    String clue;

    public Integer getNumber() { return number; }

    public void setNumber(Integer number) { this.number = number; }

    public String getClue() {
        return clue;
    }

    public void setClue(String clue) {
        this.clue = clue;
    }
}
