package com.crosswordapp.object;

public enum MiniClueDay {
    Mon, Tue, Wed, Thu, Fri, Sat, Sun;

    public MiniDifficulty getDifficulty() {
        switch(this) {
            case Mon:
            case Tue: return MiniDifficulty.Easy;
            case Wed:
            case Sun: return MiniDifficulty.Moderate;
            case Thu:
            case Fri:
            case Sat: return MiniDifficulty.Hard;
        }
        return null;
    }
}
