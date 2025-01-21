package com.example.projectmxh.models;

public class Comment {
    private String id;
    private String userId;
    private String userName;
    private String userAvatar;
    private String content;
    private long timestamp;

    public Comment(String id, String userId, String userName, String userAvatar, String content, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserAvatar() { return userAvatar; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }

    //Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    public void setContent(String content) { this.content = content; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

}