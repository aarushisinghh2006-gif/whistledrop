package com.whistledrop.controller;

import com.whistledrop.dto.CreateReportRequest;
import com.whistledrop.dto.ReportResponse;
import com.whistledrop.dto.ReportSubmissionResponse;
import com.whistledrop.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports (Public)", description = "Anonymous report submission and case tracking")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    @Operation(summary = "Submit anonymous report", description = "Submits a new confidential report without requiring an account or personal details. Returns a secret case code.")
    public ResponseEntity<ReportSubmissionResponse> submitReport(@Valid @RequestBody CreateReportRequest request) {
        ReportSubmissionResponse response = reportService.submitReport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{caseCode}")
    @Operation(summary = "Track report status", description = "Checks the status and timeline updates for a submitted report using its secret case code.")
    public ResponseEntity<ReportResponse> getReportByCaseCode(@PathVariable String caseCode) {
        ReportResponse response = reportService.getReportByCaseCode(caseCode);
        return ResponseEntity.ok(response);
    }
}
