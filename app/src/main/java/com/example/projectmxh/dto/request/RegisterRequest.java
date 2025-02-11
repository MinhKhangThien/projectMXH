package com.example.projectmxh.dto.request;

import com.example.projectmxh.enums.Gender;

public class RegisterRequest {
    private String username;
    private String password;
    private String displayName;
    private String profile;
    private Gender gender;

    public RegisterRequest(String username, String password, String displayName, Gender gender) {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.profile = "https://i.pinimg.com/736x/5e/d2/05/5ed205da4a518355e7a2fabf48c19a52.jpg";
        this.gender = gender;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getProfile() {
        return profile;
    }

    public Gender getGender() {
        return gender;
    }
}
