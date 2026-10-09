package com.whistledrop.dto;

import com.whistledrop.entity.ReportStatus;

import java.time.LocalDateTime;

public class ReportUpdateResponse {

    private ReportStatus status;
    private String message;
    private LocalDateTime createdAt;

    public ReportUpdateResponse() {
    }

    public ReportUpdateResponse(ReportStatus status, String message, LocalDateTime createdAt) {
        this.status = status;
        this.message = message;
        this.createdAt = createdAt;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
