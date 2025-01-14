package com.example.projectmxh.models;

public class User {
    private String uId;
    private String email;
    private String avatar;
    private String name;
    private String bio;

    // Default constructor required for Firebase
    public User() {
    }

    public User(String uId, String email, String avatar, String name, String bio ) {
        this.uId = uId;
        this.email = email;
        this.avatar = avatar;
        this.name = name;
        this.bio = bio;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
