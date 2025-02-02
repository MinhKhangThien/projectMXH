package com.example.projectmxh.Model;

public class User {
    private String id;
    private String username;
    private String bio;
    private String gender;
    private String displayName;
    private String profilePicture;

    public User(String id, String username, String bio, String gender, String displayName, String profilePicture) {
        this.id = id;
        this.username = username;
        this.bio = bio;
        this.gender = gender;
        this.displayName = displayName;
        this.profilePicture = profilePicture;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserName() { return username; }
    public void setUserName(String username) { this.username = username; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
}
