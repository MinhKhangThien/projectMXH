package com.example.projectmxh.Model;

public class User {
    private String id;
    private String userName;
    private String bio;
    private String gender;
    private String fullName;

    public User(String id, String userName, String bio, String gender, String fullName) {
        this.id = id;
        this.userName = userName;
        this.bio = bio;
        this.gender = gender;
        this.fullName = fullName;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
