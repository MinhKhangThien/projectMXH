package com.example.projectmxh.dto;

public class NotificationComponentDto {
    private String entityType;
    private String entityId;
    private String componentName;

    public NotificationComponentDto(String entityType, String entityId, String componentName) {
        this.entityType = entityType;
        this.entityId = entityId;
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

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
}
