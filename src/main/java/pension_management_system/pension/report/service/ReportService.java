package pension_management_system.pension.report.service;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pension_management_system.pension.report.dto.ReportRequest;
import pension_management_system.pension.report.dto.ReportResponse;
import pension_management_system.pension.report.entity.ReportType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for report generation and management
 */
public interface ReportService {

    /**
     * Generate a new report
     */
    ReportResponse generateReport(ReportRequest request);

    /**
     * Get report by ID
     */
    ReportResponse getReportById(Long id);

    /**
     * Get all reports with pagination
     */
    Page<ReportResponse> getAllReports(Pageable pageable);

    /**
     * Get reports by user
     */
    List<ReportResponse> getReportsByUser(String username);

    /**
     * Get reports by type
     */
    List<ReportResponse> getReportsByType(ReportType reportType);

    /**
     * Get reports for a specific entity
     */
    List<ReportResponse> getReportsForEntity(String entityType, Long entityId);

    /**
     * Get reports within a date range
     */
    List<ReportResponse> getReportsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Download report file
     */
    Resource downloadReport(Long id);

    /**
     * Delete a report
     */
    void deleteReport(Long id);

    /**
     * Delete old reports
     */
    int deleteOldReports(int daysOld);

    /**
     * Get storage used by user
     */
    long getStorageUsedByUser(String username);
}
