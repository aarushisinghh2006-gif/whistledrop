package com.whistledrop.dto;

import com.whistledrop.entity.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ReportStatus status;

    @NotBlank(message = "Status update message is required")
    private String message;

    public StatusUpdateRequest() {
    }

    public StatusUpdateRequest(ReportStatus status, String message) {
        this.status = status;
        this.message = message;
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
}
