package com.example.projectmxh.dto;

import java.time.LocalDateTime;
import java.util.Date;

public class NotificationUserResponseDto {
    private String componentName;
    private String entityType;
    private String entityId;
    private AppUserDto sentTo;
    private Boolean isRead;
    private String createdAt;
    private String message;
    private String description;
    private AppUserDto createdBy;

    public NotificationUserResponseDto(String componentName, String entityType, String entityId, AppUserDto sentTo, Boolean isRead, String createdAt, String message, String description, AppUserDto createdBy) {
        this.componentName = componentName;
        this.entityType = entityType;
        this.entityId = entityId;
        this.sentTo = sentTo;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.message = message;
        this.description = description;
        this.createdBy = createdBy;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public AppUserDto getSentTo() {
        return sentTo;
    }

    public void setSentTo(AppUserDto sentTo) {
        this.sentTo = sentTo;
    }

    public Boolean getRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
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

    public AppUserDto getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(AppUserDto createdBy) {
        this.createdBy = createdBy;
    }
}


