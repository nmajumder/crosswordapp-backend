package com.crosswordapp.object;

public enum MiniClueDay {
    Mon, Tue, Wed, Thu, Fri, Sat, Sun;

    public MiniDifficulty getDifficulty() {
        switch(this) {
            case Mon:
            case Tue: return MiniDifficulty.Standard;
            case Wed:
            case Sun: return MiniDifficulty.Difficult;
            case Thu:
            case Fri:
            case Sat: return MiniDifficulty.Expert;
        }
        return null;
    }
}
