package com.whistledrop.controller;

import com.whistledrop.dto.ReportResponse;
import com.whistledrop.dto.StatusUpdateRequest;
import com.whistledrop.entity.ReportCategory;
import com.whistledrop.entity.ReportStatus;
import com.whistledrop.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/moderator/reports")
@Tag(name = "Moderator Operations", description = "Authenticated review and status management of submitted reports")
@SecurityRequirement(name = "bearerAuth")
public class ModeratorController {

    private final ReportService reportService;

    public ModeratorController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    @Operation(summary = "List and filter reports", description = "Retrieve submitted reports with optional status and category filters.")
    public ResponseEntity<List<ReportResponse>> getReports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) ReportCategory category) {
        List<ReportResponse> reports = reportService.getReportsForModerator(status, category);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get report details", description = "Retrieve detailed information and update history for a single report by ID.")
    public ResponseEntity<ReportResponse> getReportById(@PathVariable UUID id) {
        ReportResponse report = reportService.getReportByIdForModerator(id);
        return ResponseEntity.ok(report);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update report status", description = "Transition report to next status according to workflow rules and log moderator update message.")
    public ResponseEntity<ReportResponse> updateReportStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StatusUpdateRequest request) {
        ReportResponse response = reportService.updateReportStatus(id, request);
        return ResponseEntity.ok(response);
    }
}
