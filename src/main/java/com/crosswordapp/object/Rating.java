package com.crosswordapp.object;

public class Rating {
    private String crosswordId;
    private Integer difficultyRating;
    private Integer enjoymentRating;

    public Rating(String crosswordId, Integer difficultyRating, Integer enjoymentRating) {
        this.crosswordId = crosswordId;
        this.difficultyRating = difficultyRating;
        this.enjoymentRating = enjoymentRating;
    }

    public String getCrosswordId() {
        return crosswordId;
    }

    public void setCrosswordId(String crosswordId) {
        this.crosswordId = crosswordId;
    }

    public Integer getDifficultyRating() {
        return difficultyRating;
    }

    public void setDifficultyRating(Integer difficultyRating) {
        this.difficultyRating = difficultyRating;
    }

    public Integer getEnjoymentRating() {
        return enjoymentRating;
    }

    public void setEnjoymentRating(Integer enjoymentRating) {
        this.enjoymentRating = enjoymentRating;
    }
}
