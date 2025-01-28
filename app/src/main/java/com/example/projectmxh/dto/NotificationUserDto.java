package com.example.projectmxh.dto;

import java.util.UUID;

public class NotificationUserDto {
    private NotificationComponentDto component;
    private UUID userId;
    private Boolean isRead;

    public NotificationUserDto(NotificationComponentDto component, UUID userId, Boolean isRead) {
        this.component = component;
        this.userId = userId;
        this.isRead = isRead;
    }

    public NotificationComponentDto getComponent() {
        return component;
    }

    public void setComponent(NotificationComponentDto component) {
        this.component = component;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Boolean getRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }
}
