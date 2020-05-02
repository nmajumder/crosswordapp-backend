package com.crosswordapp.object;

public class User {
    private String token;
    private String email;
    private String username;
    private Settings settings;

    public User() {}

    public User(String token, String email, String username, Settings settings) {
        this.token = token;
        this.email = email;
        this.username = username;
        this.settings = settings;
    }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getToken() { return token; }

    public void setToken(String token) { this.token = token; }

    public Settings getSettings() { return settings; }

    public void setSettings(Settings settings) { this.settings = settings; }
}
