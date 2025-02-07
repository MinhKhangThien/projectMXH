package com.example.projectmxh.Model;

import com.example.projectmxh.dto.AppUserDto;

import java.util.List;

public class GroupChat {
    private String id;
    private String name;
    private String image;
    private List<UserGroup> userGroups;
    private List<Message> messages;

    public GroupChat(String id, String name, String image, List<UserGroup> userGroups, List<Message> messages) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.userGroups = userGroups;
        this.messages = messages;
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

    public List<UserGroup> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(List<UserGroup> userGroups) {
        this.userGroups = userGroups;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
