package com.example.projectmxh.dto.response;

import com.example.projectmxh.dto.AppUserDto;
import java.time.LocalDateTime;
import java.util.UUID;

public class ReportResponse {
    private UUID id;
    private AppUserDto reporterId;
    private AppUserDto reportedUserId;
    private UUID reportedContentId;
    private String reportedContentType;
    private String reason;
    private String status;
    private String additionalText;
    private String createdAt;

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AppUserDto getReporterId() {
        return reporterId;
    }

    public void setReporterId(AppUserDto reporterId) {
        this.reporterId = reporterId;
    }

    public AppUserDto getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(AppUserDto reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public UUID getReportedContentId() {
        return reportedContentId;
    }

    public void setReportedContentId(UUID reportedContentId) {
        this.reportedContentId = reportedContentId;
    }

    public String getReportedContentType() {
        return reportedContentType;
    }

    public void setReportedContentType(String reportedContentType) {
        this.reportedContentType = reportedContentType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdditionalText() {
        return additionalText;
    }

    public void setAdditionalText(String additionalText) {
        this.additionalText = additionalText;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}