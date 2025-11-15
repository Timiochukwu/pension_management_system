package pension_management_system.pension.report.service;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pension_management_system.pension.report.dto.ReportRequest;
import pension_management_system.pension.report.dto.ReportResponse;
import pension_management_system.pension.report.entity.ReportFormat;
import pension_management_system.pension.report.entity.ReportType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ReportService Interface
 *
 * Purpose: Defines contract for report generation and management operations
 *
 * What is a Service Interface?
 * - Defines WHAT operations are available (method signatures)
 * - Does NOT define HOW they work (implementation)
 * - Allows multiple implementations (e.g., mock for testing, real for production)
 * - Enables dependency injection and loose coupling
 *
 * Why use interface instead of just implementation class?
 * - Flexibility: Can swap implementations without changing code
 * - Testing: Easy to create mock implementations for unit tests
 * - Documentation: Interface shows API contract clearly
 * - Design: Forces thinking about "what" before "how"
 *
 * Service Layer Responsibilities:
 * - Business logic and validation
 * - Transaction management
 * - Coordinate between multiple repositories
 * - Convert entities to DTOs (via mappers)
 * - Handle exceptions and error cases
 *
 * Flow: Controller → Service → Repository → Database
 *
 * Example usage in controller:
 * @Autowired
 * private ReportService reportService; // Spring injects implementation
 *
 * public ResponseEntity<?> generateReport(@RequestBody ReportRequest request) {
 *     ReportResponse response = reportService.generateReport(request);
 *     return ResponseEntity.ok(response);
 * }
 */
public interface ReportService {

    /**
     * GENERATE A NEW REPORT
     *
     * Creates and generates a new report based on the request
     * This is the main method for report creation
     *
     * What it should do (in implementation):
     * 1. Validate request (check permissions, dates, entity exists, etc.)
     * 2. Create Report entity with status = "PENDING"
     * 3. Save to database (get ID)
     * 4. Queue for background generation OR generate synchronously
     * 5. Generate file (PDF/Excel/CSV)
     * 6. Update entity with filePath, fileSize, status = "COMPLETED"
     * 7. Convert entity to DTO and return
     *
     * Sync vs Async:
     * - Synchronous: Generate now, wait for completion, return result
     *   - Pros: Simple, immediate result
     *   - Cons: Slow for large reports, blocks user
     *
     * - Asynchronous: Queue for background processing, return immediately
     *   - Pros: Fast response, better for large reports
     *   - Cons: More complex, need job queue
     *
     * Example request:
     * {
     *   "title": "Member Statement - John Doe",
     *   "reportType": "MEMBER_STATEMENT",
     *   "format": "PDF",
     *   "entityId": 123,
     *   "startDate": "2025-01-01T00:00:00",
     *   "endDate": "2025-01-31T23:59:59",
     *   "requestedBy": "admin@example.com"
     * }
     *
     * Example response:
     * {
     *   "id": 1,
     *   "title": "Member Statement - John Doe",
     *   "status": "COMPLETED", // or "PENDING" for async
     *   "downloadUrl": "/api/v1/reports/1/download",
     *   ...
     * }
     *
     * Possible exceptions:
     * - IllegalArgumentException: Invalid dates, entity not found
     * - SecurityException: User doesn't have permission
     * - RuntimeException: File generation failed
     *
     * @param request Report generation request with all parameters
     * @return Generated report details with download URL
     */
    ReportResponse generateReport(ReportRequest request);

    /**
     * GET REPORT BY ID
     *
     * Retrieve report details by its unique identifier
     *
     * What it should do:
     * 1. Find report in database by ID
     * 2. Check if report exists (throw exception if not)
     * 3. Check user permissions (can they view this report?)
     * 4. Convert entity to DTO
     * 5. Return response
     *
     * Use case:
     * - User clicks "View Details" in report list
     * - System needs to check report status before download
     * - Admin reviewing report metadata
     *
     * Example:
     * ReportResponse report = reportService.getReportById(123L);
     * System.out.println("Status: " + report.getStatus());
     * if (report.isCompleted()) {
     *     downloadFile(report.getDownloadUrl());
     * }
     *
     * @param id Report ID
     * @return Report details
     * @throws RuntimeException if report not found
     */
    ReportResponse getReportById(Long id);

    /**
     * GET ALL REPORTS (PAGINATED)
     *
     * Retrieve all reports with pagination
     * Admin-only function to view system-wide reports
     *
     * What it should do:
     * 1. Fetch reports from database with pagination
     * 2. Convert each entity to DTO
     * 3. Return Page containing results
     *
     * Use case:
     * - Admin dashboard showing all generated reports
     * - Report management interface
     * - System monitoring and analytics
     *
     * Example:
     * Pageable pageable = PageRequest.of(0, 10, Sort.by("generatedAt").descending());
     * Page<ReportResponse> reports = reportService.getAllReports(pageable);
     *
     * System.out.println("Total reports: " + reports.getTotalElements());
     * System.out.println("Page 1 of " + reports.getTotalPages());
     *
     * @param pageable Pagination settings (page, size, sort)
     * @return Page of reports
     */
    Page<ReportResponse> getAllReports(Pageable pageable);

    /**
     * GET REPORTS BY USER
     *
     * Retrieve all reports generated by a specific user
     *
     * What it should do:
     * 1. Find all reports where requestedBy = username
     * 2. Sort by most recent first
     * 3. Convert entities to DTOs
     * 4. Return list
     *
     * Use case:
     * - "My Reports" page showing user's own reports
     * - User viewing their download history
     * - Admin checking what reports a user has generated
     *
     * Example:
     * List<ReportResponse> myReports = reportService.getReportsByUser("john@example.com");
     * for (ReportResponse report : myReports) {
     *     System.out.println(report.getTitle() + " - " + report.getGeneratedAt());
     * }
     *
     * @param requestedBy Username or email of user
     * @return List of reports generated by that user
     */
    List<ReportResponse> getReportsByUser(String requestedBy);

    /**
     * GET REPORTS BY TYPE
     *
     * Retrieve all reports of a specific type
     * With pagination support
     *
     * What it should do:
     * 1. Filter reports by reportType
     * 2. Apply pagination
     * 3. Convert to DTOs
     * 4. Return page
     *
     * Use case:
     * - View all member statements
     * - View all employer reports
     * - Analytics: "How many PDFs were generated?"
     *
     * Example:
     * Pageable pageable = PageRequest.of(0, 10);
     * Page<ReportResponse> statements = reportService.getReportsByType(
     *     ReportType.MEMBER_STATEMENT,
     *     pageable
     * );
     *
     * @param reportType Type of report (MEMBER_STATEMENT, EMPLOYER_REPORT, etc.)
     * @param pageable Pagination settings
     * @return Page of reports of that type
     */
    Page<ReportResponse> getReportsByType(ReportType reportType, Pageable pageable);

    /**
     * GET REPORTS BY TYPE AND ENTITY
     *
     * Retrieve reports for a specific entity of a specific type
     *
     * What it should do:
     * 1. Filter by reportType AND entityId
     * 2. Sort by most recent first
     * 3. Convert to DTOs
     * 4. Return list
     *
     * Use case:
     * - Member viewing their own statements
     * - Employer viewing their employer reports
     * - "You already generated this report today" check
     *
     * Example:
     * // Check if member statement was already generated today
     * List<ReportResponse> todayReports = reportService.getReportsByTypeAndEntity(
     *     ReportType.MEMBER_STATEMENT,
     *     123L // member ID
     * );
     *
     * for (ReportResponse report : todayReports) {
     *     if (report.getGeneratedAt().isAfter(LocalDateTime.now().minusHours(24))) {
     *         // Reuse existing report instead of regenerating
     *         return report;
     *     }
     * }
     *
     * @param reportType Type of report
     * @param entityId Entity ID (member ID, employer ID, etc.)
     * @return List of matching reports
     */
    List<ReportResponse> getReportsByTypeAndEntity(ReportType reportType, Long entityId);

    /**
     * GET REPORTS IN DATE RANGE
     *
     * Retrieve reports generated within a date range
     *
     * What it should do:
     * 1. Filter by generatedAt between start and end
     * 2. Sort by date
     * 3. Convert to DTOs
     * 4. Return list
     *
     * Use case:
     * - "Reports generated this month"
     * - Monthly report summary
     * - Billing (count reports per period)
     *
     * Example:
     * LocalDateTime startOfMonth = LocalDateTime.of(2025, 1, 1, 0, 0);
     * LocalDateTime endOfMonth = LocalDateTime.of(2025, 1, 31, 23, 59, 59);
     * List<ReportResponse> januaryReports = reportService.getReportsInDateRange(
     *     startOfMonth,
     *     endOfMonth
     * );
     *
     * System.out.println("Reports generated in January: " + januaryReports.size());
     *
     * @param start Start of date range (inclusive)
     * @param end End of date range (inclusive)
     * @return List of reports generated in that period
     */
    List<ReportResponse> getReportsInDateRange(LocalDateTime start, LocalDateTime end);

    /**
     * DOWNLOAD REPORT FILE
     *
     * Retrieve the actual report file for download
     * Returns file as Spring Resource
     *
     * What it should do:
     * 1. Find report by ID
     * 2. Check status (must be COMPLETED)
     * 3. Check user permissions
     * 4. Load file from disk/storage
     * 5. Return as Resource
     *
     * Use case:
     * - User clicks "Download" button
     * - Browser downloads PDF/Excel/CSV file
     *
     * Example in controller:
     * Resource file = reportService.downloadReport(123L);
     *
     * return ResponseEntity.ok()
     *     .contentType(MediaType.APPLICATION_PDF)
     *     .header(HttpHeaders.CONTENT_DISPOSITION,
     *         "attachment; filename=\"report.pdf\"")
     *     .body(file);
     *
     * Security:
     * - Validate user can access this report
     * - Check report is completed
     * - Sanitize file path (prevent directory traversal)
     * - Log download for audit trail
     *
     * @param id Report ID
     * @return File resource for download
     * @throws RuntimeException if report not found or not completed
     */
    Resource downloadReport(Long id);

    /**
     * DELETE REPORT
     *
     * Delete a report and its associated file
     *
     * What it should do:
     * 1. Find report by ID
     * 2. Check user permissions (admin or owner)
     * 3. Delete physical file from disk
     * 4. Delete database record
     *
     * Use case:
     * - User deleting unwanted report
     * - Admin cleanup of old reports
     * - GDPR data deletion requests
     *
     * Example:
     * reportService.deleteReport(123L);
     *
     * Best practices:
     * - Soft delete option (mark as deleted, keep record)
     * - Archive before delete (for compliance)
     * - Verify file deleted successfully
     * - Log deletion for audit
     *
     * @param id Report ID to delete
     */
    void deleteReport(Long id);

    /**
     * DELETE OLD REPORTS (CLEANUP)
     *
     * Delete reports older than specified date
     * Used for scheduled cleanup jobs
     *
     * What it should do:
     * 1. Find all reports older than cutoffDate
     * 2. For each report:
     *    a. Delete physical file
     *    b. Delete database record
     * 3. Return count of deleted reports
     *
     * Use case:
     * - Scheduled job: Delete reports older than 90 days
     * - Storage management: Free up disk space
     * - Compliance: Enforce data retention policies
     *
     * Example scheduled job:
     * @Scheduled(cron = "0 0 2 * * *") // Run at 2 AM daily
     * public void cleanupOldReports() {
     *     LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
     *     int deleted = reportService.deleteOldReports(cutoff);
     *     log.info("Deleted {} old reports", deleted);
     * }
     *
     * Best practices:
     * - Archive important reports before deletion
     * - Check compliance requirements
     * - Run during off-peak hours
     * - Log deletions
     *
     * @param cutoffDate Delete reports generated before this date
     * @return Number of reports deleted
     */
    int deleteOldReports(LocalDateTime cutoffDate);

    /**
     * GET TOTAL STORAGE USED BY USER
     *
     * Calculate total file size of all reports by a user
     *
     * What it should do:
     * 1. Sum fileSize of all reports by user
     * 2. Return total in bytes
     *
     * Use case:
     * - Enforce storage quotas: "You have 500MB limit"
     * - Usage analytics: "Top 10 users by storage"
     * - Billing: Charge based on storage used
     *
     * Example:
     * Long totalBytes = reportService.getTotalStorageByUser("admin@example.com");
     * double totalMB = totalBytes / 1048576.0;
     *
     * if (totalMB > 1024) { // 1 GB limit
     *     throw new Exception("Storage quota exceeded: " + totalMB + " MB used");
     * }
     *
     * @param requestedBy Username to calculate storage for
     * @return Total storage in bytes
     */
    Long getTotalStorageByUser(String requestedBy);
}
