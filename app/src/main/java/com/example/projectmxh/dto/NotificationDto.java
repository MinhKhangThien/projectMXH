package com.example.projectmxh.dto;

import java.time.LocalDateTime;
import java.util.Collection;

public class NotificationDto {
    private NotificationComponentDto component;
    private String message;
    private String description;
    private LocalDateTime createdAt;
    private Collection<String> toUserIds;
    private String createdBy;

    public NotificationDto(NotificationComponentDto component, String message, String description, LocalDateTime createdAt, Collection<String> toUserIds, String createdBy) {
        this.component = component;
        this.message = message;
        this.description = description;
        this.createdAt = createdAt;
        this.toUserIds = toUserIds;
        this.createdBy = createdBy;
    }

    public NotificationComponentDto getComponent() {
        return component;
    }

    public void setComponent(NotificationComponentDto component) {
        this.component = component;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Collection<String> getToUserIds() {
        return toUserIds;
    }

    public void setToUserIds(Collection<String> toUserIds) {
        this.toUserIds = toUserIds;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
