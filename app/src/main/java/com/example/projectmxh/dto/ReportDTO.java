package com.example.projectmxh.dto;

import java.util.UUID;

public class ReportDTO {
    private String reportedContentId;
    private String reportedContentType;
    private String reportedUserId;
    private String reason;
    private String additionalText;


    // Getters and setters
    public String getReportedContentId() {
        return reportedContentId;
    }

    public void setReportedContentId(String reportedContentId) {
        this.reportedContentId = reportedContentId;
    }

    public String getReportedContentType() {
        return reportedContentType;
    }

    public void setReportedContentType(String reportedContentType) {
        this.reportedContentType = reportedContentType;
    }

    public String getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(String reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAdditionalText() {
        return additionalText;
    }

    public void setAdditionalText(String additionalText) {
        this.additionalText = additionalText;
    }
}
