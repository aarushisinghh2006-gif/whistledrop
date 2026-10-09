package com.whistledrop.repository;

import com.whistledrop.entity.Report;
import com.whistledrop.entity.ReportUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportUpdateRepository extends JpaRepository<ReportUpdate, UUID> {

    List<ReportUpdate> findByReportOrderByCreatedAtAsc(Report report);

    List<ReportUpdate> findByReportIdOrderByCreatedAtAsc(UUID reportId);
}
