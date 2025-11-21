package pension_management_system.pension.report.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pension_management_system.pension.report.entity.Report;
import pension_management_system.pension.report.entity.ReportType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ReportRepository - Repository for Report entity
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByRequestedByOrderByGeneratedAtDesc(String requestedBy);

    Page<Report> findByReportType(ReportType reportType, Pageable pageable);

    List<Report> findByReportTypeAndEntityIdOrderByGeneratedAtDesc(ReportType reportType, Long entityId);

    List<Report> findByGeneratedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Report> findByStatus(String status);

    @Query("SELECT COALESCE(SUM(r.fileSize), 0) FROM Report r WHERE r.requestedBy = :requestedBy")
    Long getTotalFileSizeByUser(@Param("requestedBy") String requestedBy);

    @Query("SELECT r FROM Report r WHERE r.generatedAt < :cutoffDate")
    List<Report> findOldReports(@Param("cutoffDate") LocalDateTime cutoffDate);
}
