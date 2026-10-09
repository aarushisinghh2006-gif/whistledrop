package com.whistledrop.dto;

import com.whistledrop.entity.ReportCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateReportRequest {

    @NotNull(message = "Category is required")
    private ReportCategory category;

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Pattern(
        regexp = "^(https?://.*)?$",
        message = "Evidence URL must be a valid HTTP or HTTPS URL"
    )
    private String evidenceUrl;

    public CreateReportRequest() {
    }

    public CreateReportRequest(ReportCategory category, String description, String evidenceUrl) {
        this.category = category;
        this.description = description;
        this.evidenceUrl = evidenceUrl;
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
}
