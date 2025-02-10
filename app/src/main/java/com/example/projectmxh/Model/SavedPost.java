package com.example.projectmxh.Model;

public class SavedPost {
    private String createdAt;
    private String updatedAt;
    private User user;  // who saved the post
    private Post post;  // the actual post

    public SavedPost(String createdAt, String updatedAt, User user, Post post) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.user = user;
        this.post = post;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
