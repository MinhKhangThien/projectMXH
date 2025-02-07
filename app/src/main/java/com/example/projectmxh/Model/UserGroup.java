package com.example.projectmxh.Model;

import com.example.projectmxh.dto.AppUserDto;

import java.time.LocalDateTime;

public class UserGroup {
    private String id;
    private String userId;
    private String groupChatId;
    private LocalDateTime joinedAt;
    private AppUserDto user;

    public UserGroup(String id, String userId, String groupChatId, LocalDateTime joinedAt, AppUserDto user) {
        this.id = id;
        this.userId = userId;
        this.groupChatId = groupChatId;
        this.joinedAt = joinedAt;
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupChatId() {
        return groupChatId;
    }

    public void setGroupChatId(String groupChatId) {
        this.groupChatId = groupChatId;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public AppUserDto getUser() {
        return user;
    }

    public void setUser(AppUserDto user) {
        this.user = user;
    }
}
