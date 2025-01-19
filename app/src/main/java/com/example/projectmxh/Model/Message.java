package com.example.projectmxh.Model;

public class Message {
    private String content; // Nội dung tin nhắn
    private boolean sentByUser; // true nếu tin nhắn được gửi bởi người dùng

    // Constructor
    public Message(String content, boolean sentByUser) {
        this.content = content;
        this.sentByUser = sentByUser;
    }

    // Getter cho nội dung tin nhắn
    public String getContent() {
        return content;
    }

    // Getter để kiểm tra tin nhắn được gửi bởi người dùng hay không
    public boolean isSentByUser() {
        return sentByUser;
    }
}
