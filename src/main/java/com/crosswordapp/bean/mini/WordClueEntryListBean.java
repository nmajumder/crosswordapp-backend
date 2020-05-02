package com.crosswordapp.bean.mini;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@XmlRootElement(name = "wordClueList")
@XmlAccessorType(XmlAccessType.FIELD)
public class WordClueEntryListBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "entry")
    private List<WordClueEntryBean> entryList;


    public List<WordClueEntryBean> getEntryList() {
        return entryList;
    }

    public void setEntryList(List<WordClueEntryBean> entryList) {
        this.entryList = entryList;
    }
}
