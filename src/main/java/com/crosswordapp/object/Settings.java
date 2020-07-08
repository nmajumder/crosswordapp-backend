package com.crosswordapp.object;

public class Settings {
    private Integer colorScheme;
    private Integer inactivityTimer;

    public Settings() {
        this.colorScheme = 0;
        this.inactivityTimer = 60;
    }

    public Settings(Integer colorScheme, Integer inactivityTimer) {
        this.colorScheme = colorScheme;
        this.inactivityTimer = inactivityTimer;
    }

    public Integer getColorScheme() {
        return colorScheme;
    }

    public void setColorScheme(Integer colorScheme) {
        this.colorScheme = colorScheme;
    }

    public Integer getInactivityTimer() {
        return inactivityTimer;
    }

    public void setInactivityTimer(Integer inactivityTimer) {
        this.inactivityTimer = inactivityTimer;
    }
}
