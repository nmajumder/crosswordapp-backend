package com.crosswordapp.bean.mini;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "entry")
@XmlAccessorType(XmlAccessType.FIELD)
public class WordClueEntryBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "word")
    private String word;

    @XmlElement(name = "clues")
    private MiniClueListBean clues;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public MiniClueListBean getClues() {
        return clues;
    }

    public void setClues(MiniClueListBean clues) {
        this.clues = clues;
    }
}
