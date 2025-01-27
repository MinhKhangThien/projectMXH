package com.example.projectmxh.Model;

public class Message {
    private String id;
    private String senderName;
    private String receiverName;
    private String message; // changed from message
    private String date;
    private Status status;

    public Message() {
    }

    public Message(String id, String senderName, String receiverName, String message, String date, Status status) {
        this.id = id;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.message = message;
        this.date = date;
        this.status = status;
    }

    // Add method to check if message is sent by current user
    public boolean isSentByUser(String currentUser) {
        return senderName != null && senderName.equals(currentUser);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
