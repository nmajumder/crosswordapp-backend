package com.crosswordapp.rep;

public class RatingsRep {
    public String crosswordId;
    public float difficultyScore;
    public int numDifficultyRatings;
    public float enjoymentScore;
    public int numEnjoymentRatings;

    public RatingsRep() {}

    public RatingsRep(String crosswordId) {
        this.crosswordId = crosswordId;
        this.difficultyScore = 0;
        this.numDifficultyRatings = 0;
        this.enjoymentScore = 0;
        this.numEnjoymentRatings = 0;
    }

    public RatingsRep(String crosswordId, float difficultyScore, int numDifficultyRatings,
                      float enjoymentScore, int numEnjoymentRatings) {
        this.crosswordId = crosswordId;
        this.difficultyScore = difficultyScore;
        this.numDifficultyRatings = numDifficultyRatings;
        this.enjoymentScore = enjoymentScore;
        this.numEnjoymentRatings = numEnjoymentRatings;
    }
}
