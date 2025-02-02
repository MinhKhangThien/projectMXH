package com.example.projectmxh.dto.request;

public class CreatePostRequest {
    private String caption;
    private String postType;
    private String postContentFileUrl;

    public CreatePostRequest(String caption, String postType, String postContentFileUrl) {
        this.caption = caption;
        this.postType = postType;
        this.postContentFileUrl = postContentFileUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }

    public String getPostContentFileUrl() {
        return postContentFileUrl;
    }

    public void setPostContentFileUrl(String postContentFileUrl) {
        this.postContentFileUrl = postContentFileUrl;
    }
}
