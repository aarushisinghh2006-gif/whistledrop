package com.whistledrop.service;

import com.whistledrop.dto.CreateReportRequest;
import com.whistledrop.dto.ReportResponse;
import com.whistledrop.dto.ReportSubmissionResponse;
import com.whistledrop.dto.ReportUpdateResponse;
import com.whistledrop.dto.StatusUpdateRequest;
import com.whistledrop.entity.Report;
import com.whistledrop.entity.ReportCategory;
import com.whistledrop.entity.ReportStatus;
import com.whistledrop.entity.ReportUpdate;
import com.whistledrop.exception.InvalidStatusTransitionException;
import com.whistledrop.exception.ResourceNotFoundException;
import com.whistledrop.repository.ReportRepository;
import com.whistledrop.repository.ReportUpdateRepository;
import com.whistledrop.util.CaseCodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportUpdateRepository reportUpdateRepository;

    public ReportService(ReportRepository reportRepository, ReportUpdateRepository reportUpdateRepository) {
        this.reportRepository = reportRepository;
        this.reportUpdateRepository = reportUpdateRepository;
    }

    @Transactional
    public ReportSubmissionResponse submitReport(CreateReportRequest request) {
        // Generate random case code and hash it
        String caseCode = CaseCodeGenerator.generateCaseCode();
        String caseCodeHash = CaseCodeGenerator.hashCaseCode(caseCode);

        // Sanitize optional evidence URL
        String evidenceUrl = request.getEvidenceUrl();
        if (evidenceUrl != null && evidenceUrl.trim().isEmpty()) {
            evidenceUrl = null;
        }

        // Save new report
        Report report = new Report(
                caseCodeHash,
                request.getCategory(),
                request.getDescription().trim(),
                evidenceUrl
        );
        Report savedReport = reportRepository.save(report);

        // Record initial status update
        ReportUpdate initialUpdate = new ReportUpdate(
                savedReport,
                ReportStatus.SUBMITTED,
                "Report received."
        );
        reportUpdateRepository.save(initialUpdate);

        return new ReportSubmissionResponse(
                "Report submitted successfully.",
                caseCode,
                ReportStatus.SUBMITTED
        );
    }

    @Transactional(readOnly = true)
    public ReportResponse getReportByCaseCode(String caseCode) {
        String caseCodeHash = CaseCodeGenerator.hashCaseCode(caseCode);
        Report report = reportRepository.findByCaseCodeHash(caseCodeHash)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found for the provided case code."));

        List<ReportUpdate> updates = reportUpdateRepository.findByReportOrderByCreatedAtAsc(report);
        List<ReportUpdateResponse> updateResponses = new ArrayList<>();
        for (ReportUpdate update : updates) {
            updateResponses.add(new ReportUpdateResponse(
                    update.getStatus(),
                    update.getMessage(),
                    update.getCreatedAt()
            ));
        }

        return ReportResponse.forAnonymous(
                caseCode,
                report.getCategory(),
                report.getStatus(),
                report.getCreatedAt(),
                updateResponses
        );
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getReportsForModerator(ReportStatus status, ReportCategory category) {
        List<Report> reports;

        if (status != null && category != null) {
            reports = reportRepository.findByStatusAndCategoryOrderByCreatedAtDesc(status, category);
        } else if (status != null) {
            reports = reportRepository.findByStatusOrderByCreatedAtDesc(status);
        } else if (category != null) {
            reports = reportRepository.findByCategoryOrderByCreatedAtDesc(category);
        } else {
            reports = reportRepository.findAllByOrderByCreatedAtDesc();
        }

        List<ReportResponse> responses = new ArrayList<>();
        for (Report report : reports) {
            responses.add(ReportResponse.forModerator(
                    report.getId(),
                    report.getCategory(),
                    report.getDescription(),
                    report.getEvidenceUrl(),
                    report.getStatus(),
                    report.getCreatedAt(),
                    report.getUpdatedAt(),
                    null
            ));
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public ReportResponse getReportByIdForModerator(UUID id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));

        List<ReportUpdate> updates = reportUpdateRepository.findByReportOrderByCreatedAtAsc(report);
        List<ReportUpdateResponse> updateResponses = new ArrayList<>();
        for (ReportUpdate update : updates) {
            updateResponses.add(new ReportUpdateResponse(
                    update.getStatus(),
                    update.getMessage(),
                    update.getCreatedAt()
            ));
        }

        return ReportResponse.forModerator(
                report.getId(),
                report.getCategory(),
                report.getDescription(),
                report.getEvidenceUrl(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getUpdatedAt(),
                updateResponses
        );
    }

    @Transactional
    public ReportResponse updateReportStatus(UUID id, StatusUpdateRequest request) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));

        ReportStatus currentStatus = report.getStatus();
        ReportStatus newStatus = request.getStatus();

        // Validate workflow transition
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new InvalidStatusTransitionException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus + "."
            );
        }

        // Apply status update
        report.setStatus(newStatus);
        report.setUpdatedAt(LocalDateTime.now());
        reportRepository.save(report);

        // Record history update
        ReportUpdate update = new ReportUpdate(
                report,
                newStatus,
                request.getMessage().trim()
        );
        reportUpdateRepository.save(update);

        // Fetch full update history for the response
        List<ReportUpdate> updates = reportUpdateRepository.findByReportOrderByCreatedAtAsc(report);
        List<ReportUpdateResponse> updateResponses = new ArrayList<>();
        for (ReportUpdate u : updates) {
            updateResponses.add(new ReportUpdateResponse(
                    u.getStatus(),
                    u.getMessage(),
                    u.getCreatedAt()
            ));
        }

        return ReportResponse.forModerator(
                report.getId(),
                report.getCategory(),
                report.getDescription(),
                report.getEvidenceUrl(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getUpdatedAt(),
                updateResponses
        );
    }

    private boolean isValidStatusTransition(ReportStatus from, ReportStatus to) {
        if (from == ReportStatus.SUBMITTED && to == ReportStatus.UNDER_REVIEW) {
            return true;
        }
        if (from == ReportStatus.UNDER_REVIEW && (to == ReportStatus.RESOLVED || to == ReportStatus.DISMISSED)) {
            return true;
        }
        return false;
    }
}
