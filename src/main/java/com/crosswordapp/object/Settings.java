package com.crosswordapp.object;

public class Settings {
    private Integer colorScheme;
    private Integer inactivityTimer;
    private Boolean playSound;

    public Settings() {
        this.colorScheme = 0;
        this.inactivityTimer = 60;
        this.playSound = true;
    }

    public Settings(Integer colorScheme, Integer inactivityTimer, Boolean playSound) {
        this.colorScheme = colorScheme;
        this.inactivityTimer = inactivityTimer;
        this.playSound = playSound;
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

    public Boolean getPlaySound() {
        return playSound;
    }

    public void setPlaySound(Boolean playSound) {
        this.playSound = playSound;
    }
}
