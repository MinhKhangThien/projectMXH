package com.example.projectmxh.Model;

import java.util.List;

public class Post {
    private String id;
    private String caption;
    private String postType;
    private String postContentUrl;
    private String postContent = "";
    private String thumbnailUrl;
    private String createdAt;
    private String updatedAt;
    private User user;
    private List<User> taggedUser;

    private boolean isLiked;
    private int likeCount;
    private int commentCount;
    private boolean isSaved;

    public Post(String id, String caption, String postType, String postContent, String postContentUrl, String thumbnailUrl, String createdAt, String updatedAt, User user, List<User> taggedUser, boolean isLiked, int likeCount) {
        this.id = id;
        this.caption = caption;
        this.postType = postType;
        this.postContent = postContent;
        this.postContentUrl = postContentUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.user = user;
        this.taggedUser = taggedUser;
        this.isLiked = false;
        this.likeCount = 0;
        this.commentCount = 0;
        this.isSaved = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public String getPostType() { return postType; }
    public void setPostType(String postType) { this.postType = postType; }

    public String getPostContentUrl() { return postContentUrl; }
    public void setPostContentUrl(String postContentUrl) { this.postContentUrl = postContentUrl; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<User> getTaggedUser() { return taggedUser; }
    public void setTaggedUser(List<User> taggedUser) { this.taggedUser = taggedUser; }

    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean liked) { isLiked = liked; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public boolean isSaved() { return isSaved; }
    public void setSaved(boolean saved) { isSaved = saved; }

    public String getPostContent() { return postContent; }
    public void setPostContent(String postContent) { this.postContent = postContent; }



}