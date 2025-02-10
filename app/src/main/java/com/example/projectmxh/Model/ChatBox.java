package com.example.projectmxh.Model;

public class ChatBox {
    private String id;
    private String name;
    private String username;
    private String image;
    private boolean isGroup;
    private String lastMessage;
    private Integer unreadCount;

    public ChatBox(String id, String name, String username, String image, boolean isGroup, String lastMessage) {
        this.id = id != null ? id : "";
        this.name = name != null ? name : "";
        this.username = username; // Can be null for group chats
        this.image = image != null ? image : "";
        this.isGroup = isGroup;
        this.lastMessage = lastMessage != null ? lastMessage : "";
        this.unreadCount = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }
}
