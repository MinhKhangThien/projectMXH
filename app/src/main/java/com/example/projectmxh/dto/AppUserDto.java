package com.example.projectmxh.dto;

import com.example.projectmxh.enums.AccountType;
import com.example.projectmxh.enums.RoleType;

import java.util.Objects;

public class AppUserDto {
    private String id;
    private String username;
    private String bio;
    private String gender;
    private String displayName;
    private RoleType role;
    private String profilePicture;
    private AccountType accountType = AccountType.PUBLIC;
    private Boolean isBanned;


    public AppUserDto(String id, String username, String bio, String gender, String displayName, RoleType role, String profilePicture, AccountType accountType) {
        this.id = id;
        this.username = username;
        this.bio = bio;
        this.gender = gender;
        this.displayName = displayName;
        this.role = role;
        this.profilePicture = profilePicture;
        this.accountType = accountType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public Boolean getIsBanned() {
        return isBanned;
    }

    public void setIsBanned(Boolean banned) {
        isBanned = banned;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppUserDto that = (AppUserDto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
