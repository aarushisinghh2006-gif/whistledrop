package com.whistledrop.dto;

import com.whistledrop.entity.ReportStatus;

public class ReportSubmissionResponse {

    private String message;
    private String caseCode;
    private ReportStatus status;

    public ReportSubmissionResponse() {
    }

    public ReportSubmissionResponse(String message, String caseCode, ReportStatus status) {
        this.message = message;
        this.caseCode = caseCode;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCaseCode() {
        return caseCode;
    }

    public void setCaseCode(String caseCode) {
        this.caseCode = caseCode;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }
}
