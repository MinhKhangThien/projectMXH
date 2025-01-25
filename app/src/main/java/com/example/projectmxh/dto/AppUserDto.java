package com.example.projectmxh.dto;

import java.util.UUID;

enum RoleType {
    USER,
    ADMIN
}

public class AppUserDto {
    private UUID id;
    private String username;
    private String displayName;
    private RoleType role;

    public AppUserDto(UUID id, String username, String displayName, RoleType role) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public RoleType getRole() {
        return role;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }
}
