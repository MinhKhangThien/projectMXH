package com.example.projectmxh.dto;

import java.time.LocalDateTime;
import java.util.Date;

public class NotificationUserResponseDto {
    private String componentName;
    private String entityType;
    private String entityId;
    private String userId;
    private Boolean isRead;
    private Date createdAt;

    public NotificationUserResponseDto(String componentName, String entityType, String entityId, String userId, Boolean isRead, Date createdAt) {
        this.componentName = componentName;
        this.entityType = entityType;
        this.entityId = entityId;
        this.userId = userId;
        this.isRead = isRead;
        this.createdAt = createdAt;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}


