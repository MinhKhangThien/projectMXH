package com.example.projectmxh.dto;

public class GroupChatDto {
    private String id;
    private String name;
    private String image;
    private AppUserDto host;
    //private LocalDateTime createdAt;

    public GroupChatDto(String id, String name, String image) {
        this.id = id;
        this.name = name;
        this.image = image;
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

    public AppUserDto getHost() {
        return host;
    }

    public void setHost(AppUserDto host) {
        this.host = host;
    }
}
