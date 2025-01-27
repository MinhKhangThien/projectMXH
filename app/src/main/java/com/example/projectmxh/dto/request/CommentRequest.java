package com.example.projectmxh.dto.request;

public class CommentRequest {
    private String text;
    private String postId;

    public CommentRequest(String text, String postId) {
        this.text = text;
        this.postId = postId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
