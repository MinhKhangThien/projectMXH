package com.example.projectmxh.Model;

import com.example.projectmxh.dto.AppUserDto;

public class ChatList {
    private String id;
    private AppUserDto user;
    private AppUserDto contact;
    private String lastMessage;
    private String lastMessageTime;
    private Integer unreadCount;

    public ChatList(String id, AppUserDto user, AppUserDto contact, String lastMessage, String lastMessageTime, Integer unreadCount) {
        this.id = id;
        this.user = user;
        this.contact = contact;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AppUserDto getUser() {
        return user;
    }

    public void setUser(AppUserDto user) {
        this.user = user;
    }

    public AppUserDto getContact() {
        return contact;
    }

    public void setContact(AppUserDto contact) {
        this.contact = contact;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }
}
