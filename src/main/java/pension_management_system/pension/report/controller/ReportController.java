package pension_management_system.pension.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pension_management_system.pension.common.dto.ApiResponseDto;
import pension_management_system.pension.report.dto.ReportRequest;
import pension_management_system.pension.report.dto.ReportResponse;
import pension_management_system.pension.report.entity.ReportFormat;
import pension_management_system.pension.report.entity.ReportType;
import pension_management_system.pension.report.service.ReportService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ReportController - REST API endpoints for report generation and management
 *
 * Purpose: Expose report functionality through HTTP endpoints
 * Allows clients to generate, view, download, and manage reports
 *
 * Base URL: /api/v1/reports
 *
 * Available Operations:
 * - POST   /api/v1/reports                    → Generate new report
 * - GET    /api/v1/reports/{id}               → Get report details
 * - GET    /api/v1/reports                    → List all reports (paginated)
 * - GET    /api/v1/reports/user/{username}    → Get user's reports
 * - GET    /api/v1/reports/type/{reportType}  → Get reports by type
 * - GET    /api/v1/reports/entity             → Get reports for entity
 * - GET    /api/v1/reports/date-range         → Get reports in date range
 * - GET    /api/v1/reports/{id}/download      → Download report file
 * - DELETE /api/v1/reports/{id}               → Delete report
 * - DELETE /api/v1/reports/cleanup            → Delete old reports
 * - GET    /api/v1/reports/user/{username}/storage → Get user's storage usage
 *
 * REST API Basics:
 * - POST: Create new resource
 * - GET: Read/retrieve resource
 * - PUT: Update entire resource
 * - PATCH: Update partial resource
 * - DELETE: Remove resource
 *
 * HTTP Status Codes Used:
 * - 200 OK: Request successful
 * - 201 CREATED: Resource created successfully
 * - 400 BAD REQUEST: Invalid input
 * - 404 NOT FOUND: Resource doesn't exist
 * - 500 INTERNAL SERVER ERROR: Server error
 *
 * Annotations Explained:
 * @RestController - Combines @Controller + @ResponseBody
 *   - All methods return data (not views)
 *   - Responses automatically converted to JSON
 *
 * @RequestMapping - Base URL for all endpoints
 *   - /api/v1/reports is prepended to all method URLs
 *
 * @RequiredArgsConstructor - Lombok generates constructor for final fields
 *   - Spring injects ReportService dependency
 *
 * @Slf4j - Lombok provides logger
 *   - Use: log.info(), log.error(), log.debug()
 *
 * @Tag - Swagger/OpenAPI documentation
 *   - Groups endpoints in API documentation UI
 *
 * Testing with cURL:
 * # Generate report
 * curl -X POST http://localhost:1110/api/v1/reports \
 *   -H "Content-Type: application/json" \
 *   -d '{"title":"Test Report","reportType":"MEMBER_STATEMENT","format":"PDF","entityId":123,"requestedBy":"admin"}'
 *
 * # Get report
 * curl http://localhost:1110/api/v1/reports/1
 *
 * # Download report
 * curl http://localhost:1110/api/v1/reports/1/download -o report.pdf
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "Report generation and management APIs")
public class ReportController {

    /**
     * DEPENDENCY INJECTION
     *
     * Spring automatically injects ReportService implementation
     * final = immutable after construction
     */
    private final ReportService reportService;

    /**
     * GENERATE NEW REPORT
     *
     * HTTP Method: POST
     * URL: /api/v1/reports
     * Content-Type: application/json
     *
     * Request Body Example:
     * {
     *   "title": "Member Statement - John Doe (January 2025)",
     *   "reportType": "MEMBER_STATEMENT",
     *   "format": "PDF",
     *   "entityId": 123,
     *   "startDate": "2025-01-01T00:00:00",
     *   "endDate": "2025-01-31T23:59:59",
     *   "requestedBy": "admin@example.com",
     *   "parameters": {
     *     "includeGraphs": true,
     *     "currency": "NGN"
     *   }
     * }
     *
     * Success Response (HTTP 201):
     * {
     *   "success": true,
     *   "message": "Report generated successfully",
     *   "data": {
     *     "id": 1,
     *     "title": "Member Statement - John Doe (January 2025)",
     *     "reportType": "MEMBER_STATEMENT",
     *     "format": "PDF",
     *     "status": "COMPLETED",
     *     "downloadUrl": "/api/v1/reports/1/download",
     *     "fileSize": 245760,
     *     "formattedFileSize": "240.0 KB",
     *     ...
     *   }
     * }
     *
     * Error Response (HTTP 400):
     * {
     *   "success": false,
     *   "message": "Validation failed: Start date must be before end date"
     * }
     *
     * @Valid annotation:
     * - Triggers validation on ReportRequest
     * - Checks @NotNull, @NotBlank constraints
     * - Returns 400 if validation fails
     *
     * @RequestBody annotation:
     * - Spring converts JSON → ReportRequest object
     * - Automatic deserialization using Jackson
     */
    @PostMapping
    @Operation(
        summary = "Generate a new report",
        description = "Creates and generates a new report based on the request parameters. " +
                     "Returns report details including download URL once generation is complete."
    )
    public ResponseEntity<ApiResponseDto<ReportResponse>> generateReport(
            @Valid @RequestBody ReportRequest request) {

        log.info("POST /api/v1/reports - Generate report: {}", request.getTitle());

        try {
            // STEP 1: Call service to generate report
            // Service handles validation, file generation, database save
            ReportResponse response = reportService.generateReport(request);

            // STEP 2: Build success response
            ApiResponseDto<ReportResponse> apiResponse = ApiResponseDto.<ReportResponse>builder()
                    .success(true)
                    .message("Report generated successfully")
                    .data(response)
                    .build();

            // STEP 3: Return HTTP 201 CREATED
            // 201 indicates a new resource was created
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);

        } catch (IllegalArgumentException e) {
            // Validation errors (bad input from client)
            log.error("Validation error: {}", e.getMessage());

            ApiResponseDto<ReportResponse> apiResponse = ApiResponseDto.<ReportResponse>builder()
                    .success(false)
                    .message("Validation failed: " + e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);

        } catch (Exception e) {
            // Unexpected errors (server problems)
            log.error("Error generating report: {}", e.getMessage(), e);

            ApiResponseDto<ReportResponse> apiResponse = ApiResponseDto.<ReportResponse>builder()
                    .success(false)
                    .message("Failed to generate report: " + e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * GET BENEFIT CLAIMS REPORTS
     *
     * HTTP Method: GET
     * URL: /api/v1/reports/claims
     *
     * Example: GET /api/v1/reports/claims?page=0&size=10
     *
     * Note: This endpoint must come before /{id} to avoid path conflict
     */
    @GetMapping("/claims")
    @Operation(summary = "Get benefit claims reports", description = "Retrieve all benefit claims reports")
    public ResponseEntity<ApiResponseDto<Page<ReportResponse>>> getBenefitClaimsReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET /api/v1/reports/claims - Get benefit claims reports");

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "generatedAt"));
        Page<ReportResponse> reports = reportService.getReportsByType(ReportType.BENEFIT_CLAIMS, pageable);

        ApiResponseDto<Page<ReportResponse>> apiResponse = ApiResponseDto.<Page<ReportResponse>>builder()
                .success(true)
                .message("Benefit claims reports retrieved successfully")
                .data(reports)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * GET REPORT BY ID
     *
     * HTTP Method: GET
     * URL: /api/v1/reports/{id}
     *
     * Example: GET /api/v1/reports/123
     *
     * Success Response (HTTP 200):
     * {
     *   "success": true,
     *   "message": "Report retrieved successfully",
     *   "data": {
     *     "id": 123,
     *     "title": "Member Statement - John Doe",
     *     "status": "COMPLETED",
     *     "downloadUrl": "/api/v1/reports/123/download",
     *     ...
     *   }
     * }
     *
     * Not Found Response (HTTP 404):
     * {
     *   "success": false,
     *   "message": "Report not found with ID: 123"
     * }
     *
     * @PathVariable annotation:
     * - Extracts {id} from URL path
     * - Converts string → Long automatically
     * - Example: /api/v1/reports/123 → id = 123L
     */
    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Get report by ID", description = "Retrieve report details by its unique identifier")
    public ResponseEntity<ApiResponseDto<ReportResponse>> getReportById(@PathVariable Long id) {

        log.info("GET /api/v1/reports/{} - Get report details", id);

        try {
            ReportResponse response = reportService.getReportById(id);

            ApiResponseDto<ReportResponse> apiResponse = ApiResponseDto.<ReportResponse>builder()
                    .success(true)
                    .message("Report retrieved successfully")
                    .data(response)
                    .build();

            return ResponseEntity.ok(apiResponse);

        } catch (RuntimeException e) {
            log.error("Report not found: {}", e.getMessage());

            ApiResponseDto<ReportResponse> apiResponse = ApiResponseDto.<ReportResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        }
    }

    /**
     * GET ALL REPORTS (PAGINATED)
     *
     * HTTP Method: GET
     * URL: /api/v1/reports
     *
     * Query Parameters:
     * - page: Page number (starts from 0)
     * - size: Number of items per page
     * - sortBy: Field to sort by
     * - sortDirection: ASC or DESC
     *
     * Example URLs:
     * - /api/v1/reports
     * - /api/v1/reports?page=0&size=10
     * - /api/v1/reports?page=1&size=20&sortBy=generatedAt&sortDirection=DESC
     *
     * Response includes:
     * - content: Array of reports
     * - totalElements: Total count across all pages
     * - totalPages: Total number of pages
     * - size: Items per page
     * - number: Current page number
     *
     * @RequestParam annotation:
     * - Extracts query parameters from URL
     * - defaultValue: Used if parameter not provided
     * - Example: ?page=2 → page = 2
     */
    @GetMapping
    @Operation(summary = "Get all reports", description = "Retrieve all reports with pagination and sorting")
    public ResponseEntity<ApiResponseDto<Page<ReportResponse>>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "generatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.info("GET /api/v1/reports - page: {}, size: {}, sort: {} {}", page, size, sortBy, sortDirection);

        // STEP 1: Create sort direction
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        // STEP 2: Create Pageable object
        // Pageable tells Spring Data JPA how to paginate and sort
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // STEP 3: Fetch reports
        Page<ReportResponse> reports = reportService.getAllReports(pageable);

        // STEP 4: Build response
        ApiResponseDto<Page<ReportResponse>> apiResponse = ApiResponseDto.<Page<ReportResponse>>builder()
                .success(true)
                .message("Reports retrieved successfully")
                .data(reports)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * GET REPORTS BY USER
     *
     * HTTP Method: GET
     * URL: /api/v1/reports/user/{username}
     *
     * Example: GET /api/v1/reports/user/admin@example.com
     *
     * Use case:
     * - "My Reports" page showing user's own reports
     * - Admin viewing what reports a specific user has generated
     */
    @GetMapping("/user/{username}")
    @Operation(summary = "Get reports by user", description = "Retrieve all reports generated by a specific user")
    public ResponseEntity<ApiResponseDto<List<ReportResponse>>> getReportsByUser(
            @PathVariable String username) {

        log.info("GET /api/v1/reports/user/{} - Get user's reports", username);

        List<ReportResponse> reports = reportService.getReportsByUser(username);

        ApiResponseDto<List<ReportResponse>> apiResponse = ApiResponseDto.<List<ReportResponse>>builder()
                .success(true)
                .message("User reports retrieved successfully")
                .data(reports)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * GET REPORTS BY TYPE
     *
     * HTTP Method: GET
     * URL: /api/v1/reports/type/{reportType}
     *
     * Example: GET /api/v1/reports/type/MEMBER_STATEMENT?page=0&size=10
     *
     * Valid reportType values:
     * - MEMBER_STATEMENT
     * - EMPLOYER_REPORT
     * - CONTRIBUTION_SUMMARY
     * - BENEFIT_CLAIMS
     * - ANALYTICS_DASHBOARD
     * - AUDIT_TRAIL
     */
    @GetMapping("/type/{reportType}")
    @Operation(summary = "Get reports by type", description = "Retrieve all reports of a specific type")
    public ResponseEntity<ApiResponseDto<Page<ReportResponse>>> getReportsByType(
            @PathVariable ReportType reportType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET /api/v1/reports/type/{} - Get reports by type", reportType);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "generatedAt"));
        Page<ReportResponse> reports = reportService.getReportsByType(reportType, pageable);

        ApiResponseDto<Page<ReportResponse>> apiResponse = ApiResponseDto.<Page<ReportResponse>>builder()
                .success(true)
                .message("Reports by type retrieved successfully")
                .data(reports)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * GET REPORTS BY TYPE AND ENTITY
     *
     * HTTP Method: GET
     * URL: /api/v1/reports/entity
     *
     * Query Parameters:
     * - reportType: Type of report (required)
     * - entityId: ID of entity (required)
     *
     * Example: GET /api/v1/reports/entity?reportType=MEMBER_STATEMENT&entityId=123
     *
     * Use case:
     * - Member viewing their own statement history
     * - Employer viewing their employer report history
     */
    @GetMapping("/entity")
    @Operation(summary = "Get reports by type and entity", description = "Retrieve reports for a specific entity")
    public ResponseEntity<ApiResponseDto<List<ReportResponse>>> getReportsByTypeAndEntity(
            @RequestParam ReportType reportType,
            @RequestParam Long entityId) {

        log.info("GET /api/v1/reports/entity - reportType: {}, entityId: {}", reportType, entityId);

        List<ReportResponse> reports = reportService.getReportsByTypeAndEntity(reportType, entityId);

        ApiResponseDto<List<ReportResponse>> apiResponse = ApiResponseDto.<List<ReportResponse>>builder()
                .success(true)
                .message("Entity reports retrieved successfully")
                .data(reports)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * GET REPORTS IN DATE RANGE
     *
     * HTTP Method: GET
     * URL: /api/v1/reports/date-range
     *
     * Query Parameters:
     * - start: Start date (format: yyyy-MM-dd'T'HH:mm:ss)
     * - end: End date (format: yyyy-MM-dd'T'HH:mm:ss)
     *
     * Example: GET /api/v1/reports/date-range?start=2025-01-01T00:00:00&end=2025-01-31T23:59:59
     *
     * @DateTimeFormat annotation:
     * - Tells Spring how to parse date string from URL
     * - ISO format: yyyy-MM-dd'T'HH:mm:ss
     * - Example: "2025-01-15T10:30:45" → LocalDateTime object
     *
     * Use case:
     * - "Reports generated this month"
     * - Monthly analytics and summaries
     * - Compliance audits
     */
    @GetMapping("/date-range")
    @Operation(summary = "Get reports in date range", description = "Retrieve reports generated within a date range")
    public ResponseEntity<ApiResponseDto<List<ReportResponse>>> getReportsInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        log.info("GET /api/v1/reports/date-range - start: {}, end: {}", start, end);

        List<ReportResponse> reports = reportService.getReportsInDateRange(start, end);

        ApiResponseDto<List<ReportResponse>> apiResponse = ApiResponseDto.<List<ReportResponse>>builder()
                .success(true)
                .message("Reports in date range retrieved successfully")
                .data(reports)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * DOWNLOAD REPORT FILE
     *
     * HTTP Method: GET
     * URL: /api/v1/reports/{id}/download
     *
     * Example: GET /api/v1/reports/123/download
     *
     * Response:
     * - Content-Type: application/pdf (or excel, csv)
     * - Content-Disposition: attachment; filename="report.pdf"
     * - Body: Binary file data
     *
     * Browser behavior:
     * - Opens download dialog
     * - Saves file with suggested filename
     *
     * How to test:
     * 1. Browser: Visit URL directly → triggers download
     * 2. cURL: curl http://localhost:1110/api/v1/reports/1/download -o report.pdf
     * 3. Postman: Send request → Click "Save Response" → Save to file
     *
     * Response headers explained:
     * - ContentType: Tells browser what kind of file
     * - CONTENT_DISPOSITION: Tells browser to download (not display)
     * - attachment: Download instead of inline display
     * - filename: Suggested filename for download
     */
    @GetMapping("/{id}/download")
    @Operation(summary = "Download report file", description = "Download the generated report file")
    public ResponseEntity<Resource> downloadReport(@PathVariable Long id) {

        log.info("GET /api/v1/reports/{}/download - Download report", id);

        try {
            // STEP 1: Get report details
            ReportResponse report = reportService.getReportById(id);

            // STEP 2: Check status
            if (!"COMPLETED".equals(report.getStatus())) {
                log.error("Report not ready for download. Status: {}", report.getStatus());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // STEP 3: Load file
            Resource file = reportService.downloadReport(id);

            // STEP 4: Determine content type based on format
            MediaType contentType = getMediaType(report.getFormat());

            // STEP 5: Generate filename
            // Format: report_title_timestamp.extension
            String filename = generateFilename(report);

            // STEP 6: Build response with file
            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(file);

        } catch (Exception e) {
            log.error("Error downloading report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DELETE REPORT
     *
     * HTTP Method: DELETE
     * URL: /api/v1/reports/{id}
     *
     * Example: DELETE /api/v1/reports/123
     *
     * Success Response (HTTP 200):
     * {
     *   "success": true,
     *   "message": "Report deleted successfully"
     * }
     *
     * Security considerations:
     * - Check user has permission to delete
     * - Admin can delete any report
     * - Regular user can only delete their own reports
     */
    @DeleteMapping("/{id:\\d+}")
    @Operation(summary = "Delete report", description = "Delete a report and its associated file")
    public ResponseEntity<ApiResponseDto<Void>> deleteReport(@PathVariable Long id) {

        log.info("DELETE /api/v1/reports/{} - Delete report", id);

        try {
            reportService.deleteReport(id);

            ApiResponseDto<Void> apiResponse = ApiResponseDto.<Void>builder()
                    .success(true)
                    .message("Report deleted successfully")
                    .build();

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            log.error("Error deleting report: {}", e.getMessage(), e);

            ApiResponseDto<Void> apiResponse = ApiResponseDto.<Void>builder()
                    .success(false)
                    .message("Failed to delete report: " + e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * DELETE OLD REPORTS (CLEANUP)
     *
     * HTTP Method: DELETE
     * URL: /api/v1/reports/cleanup
     *
     * Query Parameter:
     * - cutoffDate: Delete reports before this date
     *
     * Example: DELETE /api/v1/reports/cleanup?cutoffDate=2024-01-01T00:00:00
     *
     * Use case:
     * - Scheduled cleanup job
     * - Storage management
     * - Admin-triggered cleanup
     *
     * Security:
     * - Should be admin-only endpoint
     * - Add authentication/authorization check
     */
    @DeleteMapping("/cleanup")
    @Operation(summary = "Delete old reports", description = "Delete reports older than specified date")
    public ResponseEntity<ApiResponseDto<Integer>> deleteOldReports(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cutoffDate) {

        log.info("DELETE /api/v1/reports/cleanup - cutoffDate: {}", cutoffDate);

        try {
            int deletedCount = reportService.deleteOldReports(cutoffDate);

            ApiResponseDto<Integer> apiResponse = ApiResponseDto.<Integer>builder()
                    .success(true)
                    .message("Deleted " + deletedCount + " old reports")
                    .data(deletedCount)
                    .build();

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            log.error("Error during cleanup: {}", e.getMessage(), e);

            ApiResponseDto<Integer> apiResponse = ApiResponseDto.<Integer>builder()
                    .success(false)
                    .message("Cleanup failed: " + e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * GET USER'S TOTAL STORAGE
     *
     * HTTP Method: GET
     * URL: /api/v1/reports/user/{username}/storage
     *
     * Example: GET /api/v1/reports/user/admin@example.com/storage
     *
     * Response:
     * {
     *   "success": true,
     *   "message": "Storage usage retrieved successfully",
     *   "data": 524288000  // bytes (500 MB)
     * }
     *
     * Client can convert to human-readable:
     * 524288000 bytes = 500 MB
     */
    @GetMapping("/user/{username}/storage")
    @Operation(summary = "Get user's storage usage", description = "Get total file size of all user's reports")
    public ResponseEntity<ApiResponseDto<Long>> getUserStorage(@PathVariable String username) {

        log.info("GET /api/v1/reports/user/{}/storage - Get storage usage", username);

        Long totalBytes = reportService.getTotalStorageByUser(username);

        String message = String.format("User %s has used %d bytes (%.1f MB)",
                username, totalBytes, totalBytes / 1048576.0);

        ApiResponseDto<Long> apiResponse = ApiResponseDto.<Long>builder()
                .success(true)
                .message(message)
                .data(totalBytes)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * HELPER METHOD: Get MediaType for report format
     *
     * Determines correct Content-Type header for download
     */
    private MediaType getMediaType(ReportFormat format) {
        switch (format) {
            case PDF:
                return MediaType.APPLICATION_PDF;

            case EXCEL:
                // MIME type for Excel files
                return MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                );

            case CSV:
                // MIME type for CSV files
                return MediaType.parseMediaType("text/csv");

            default:
                // Fallback: binary data
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    /**
     * HELPER METHOD: Generate filename for download
     *
     * Creates user-friendly filename from report details
     * Removes special characters and spaces
     */
    private String generateFilename(ReportResponse report) {
        // Remove special characters from title
        String sanitizedTitle = report.getTitle()
                .replaceAll("[^a-zA-Z0-9\\s-]", "")
                .replaceAll("\\s+", "_");

        // Get file extension
        String extension = report.getFormat().toString().toLowerCase();

        // Format: title_id.extension
        // Example: Member_Statement_John_Doe_123.pdf
        return String.format("%s_%d.%s", sanitizedTitle, report.getId(), extension);
    }
}
