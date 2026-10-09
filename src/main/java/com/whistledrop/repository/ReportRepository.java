package com.whistledrop.repository;

import com.whistledrop.entity.Report;
import com.whistledrop.entity.ReportCategory;
import com.whistledrop.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    Optional<Report> findByCaseCodeHash(String caseCodeHash);

    List<Report> findAllByOrderByCreatedAtDesc();

    List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    List<Report> findByCategoryOrderByCreatedAtDesc(ReportCategory category);

    List<Report> findByStatusAndCategoryOrderByCreatedAtDesc(ReportStatus status, ReportCategory category);
}
