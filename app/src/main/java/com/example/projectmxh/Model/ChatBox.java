package com.example.projectmxh.Model;

public class ChatBox {
    private String id;
    private String name;
    private String username;
    private String image;
    private boolean isGroup;
    private String lastMessage;

    public ChatBox(String id, String name, String username, String image, boolean isGroup, String lastMessage) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.image = image;
        this.isGroup = isGroup;
        this.lastMessage = lastMessage;
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
}
