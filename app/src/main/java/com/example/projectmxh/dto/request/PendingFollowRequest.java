package com.example.projectmxh.dto.request;

public class PendingFollowRequest {
    private String id;
    private String userName;
    private String fullName;
    private String bio;

    public PendingFollowRequest(String id, String userName, String fullName, String bio) {
        this.id = id;
        this.userName = userName;
        this.fullName = fullName;
        this.bio = bio;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
