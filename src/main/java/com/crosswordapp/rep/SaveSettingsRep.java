package com.crosswordapp.rep;

import com.crosswordapp.object.Settings;

public class SaveSettingsRep {
    public String userToken;
    public Settings settings;

    public SaveSettingsRep() {}

    public SaveSettingsRep(String userToken, Settings settings) {
        this.userToken = userToken;
        this.settings = settings;
    }
}
