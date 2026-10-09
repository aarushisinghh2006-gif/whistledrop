package com.whistledrop.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.whistledrop.entity.ReportCategory;
import com.whistledrop.entity.ReportStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportResponse {

    private UUID id;
    private String caseCode;
    private ReportCategory category;
    private String description;
    private String evidenceUrl;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReportUpdateResponse> updates;

    public ReportResponse() {
    }

    // Constructor for anonymous case tracking
    public static ReportResponse forAnonymous(String caseCode, ReportCategory category, ReportStatus status,
                                              LocalDateTime createdAt, List<ReportUpdateResponse> updates) {
        ReportResponse response = new ReportResponse();
        response.setCaseCode(caseCode);
        response.setCategory(category);
        response.setStatus(status);
        response.setCreatedAt(createdAt);
        response.setUpdates(updates);
        return response;
    }

    // Constructor for moderator views
    public static ReportResponse forModerator(UUID id, ReportCategory category, String description,
                                             String evidenceUrl, ReportStatus status, LocalDateTime createdAt,
                                             LocalDateTime updatedAt, List<ReportUpdateResponse> updates) {
        ReportResponse response = new ReportResponse();
        response.setId(id);
        response.setCategory(category);
        response.setDescription(description);
        response.setEvidenceUrl(evidenceUrl);
        response.setStatus(status);
        response.setCreatedAt(createdAt);
        response.setUpdatedAt(updatedAt);
        response.setUpdates(updates);
        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCaseCode() {
        return caseCode;
    }

    public void setCaseCode(String caseCode) {
        this.caseCode = caseCode;
    }

    public ReportCategory getCategory() {
        return category;
    }

    public void setCategory(ReportCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEvidenceUrl() {
        return evidenceUrl;
    }

    public void setEvidenceUrl(String evidenceUrl) {
        this.evidenceUrl = evidenceUrl;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ReportUpdateResponse> getUpdates() {
        return updates;
    }

    public void setUpdates(List<ReportUpdateResponse> updates) {
        this.updates = updates;
    }
}
