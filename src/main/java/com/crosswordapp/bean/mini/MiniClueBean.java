package com.crosswordapp.bean.mini;

import com.crosswordapp.object.MiniClueDay;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "clue")
@XmlAccessorType(XmlAccessType.FIELD)
public class MiniClueBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "day")
    private String day;

    @XmlElement(name = "clueText")
    private String text;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
