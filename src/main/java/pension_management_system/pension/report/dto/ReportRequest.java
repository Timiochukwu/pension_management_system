package pension_management_system.pension.report.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pension_management_system.pension.report.entity.ReportFormat;
import pension_management_system.pension.report.entity.ReportType;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ReportRequest DTO - Data Transfer Object for report generation requests
 *
 * Purpose: Carries data from client (API request) to server when generating a report
 *
 * What is a DTO?
 * - DTO = Data Transfer Object
 * - Separate from Entity (database model)
 * - Contains only data needed for this specific API operation
 * - Includes validation rules
 * - Protects internal Entity structure from API changes
 *
 * Why use DTO instead of Entity?
 * - Security: Don't expose internal database structure
 * - Validation: Enforce rules on incoming data
 * - Flexibility: API can differ from database schema
 * - Versioning: Can change API without changing database
 *
 * How it's used:
 * 1. Client sends JSON in HTTP POST body
 * 2. Spring converts JSON → ReportRequest object
 * 3. Spring validates using @NotNull, @NotBlank annotations
 * 4. If valid → Service processes request
 * 5. If invalid → Return 400 Bad Request with validation errors
 *
 * Annotations Explained:
 * @Data - Lombok generates getters, setters, toString, equals, hashCode
 * @Builder - Enables builder pattern: ReportRequest.builder().title("...").build()
 * @NoArgsConstructor - Generates no-argument constructor (required by Jackson for JSON)
 * @AllArgsConstructor - Generates constructor with all fields (used by @Builder)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {

    /**
     * REPORT TITLE
     *
     * Human-readable title for the report
     * Will be displayed in report list and download history
     *
     * Examples:
     * - "Monthly Contribution Summary - January 2025"
     * - "Member Statement - John Doe"
     * - "Employer Report - Tech Corp Ltd"
     *
     * Validation:
     * @NotBlank - Cannot be null, empty string, or only whitespace
     * - Valid: "Member Statement"
     * - Invalid: null, "", "   "
     *
     * If validation fails:
     * HTTP 400 Bad Request
     * {
     *   "success": false,
     *   "message": "Validation failed",
     *   "errors": ["title: must not be blank"]
     * }
     */
    @NotBlank(message = "Report title is required")
    private String title;

    /**
     * REPORT TYPE
     *
     * Category of report to generate
     * Determines what data is included
     *
     * Possible values (from ReportType enum):
     * - MEMBER_STATEMENT: Individual member's contribution history
     * - EMPLOYER_REPORT: Employer's members and contributions
     * - CONTRIBUTION_SUMMARY: Contribution totals by period
     * - BENEFIT_CLAIMS: Benefit claims summary
     * - ANALYTICS_DASHBOARD: System-wide analytics export
     * - AUDIT_TRAIL: System activity logs
     *
     * JSON example:
     * {
     *   "reportType": "MEMBER_STATEMENT",
     *   ...
     * }
     *
     * Validation:
     * @NotNull - Cannot be null (but can be any valid enum value)
     *
     * If invalid enum value sent:
     * {
     *   "reportType": "INVALID_TYPE"
     * }
     * Result: HTTP 400 with error "Invalid enum value"
     */
    @NotNull(message = "Report type is required")
    private ReportType reportType;

    /**
     * OUTPUT FORMAT
     *
     * File format for the generated report
     *
     * Possible values (from ReportFormat enum):
     * - PDF: Portable Document Format (best for official documents)
     * - EXCEL: Microsoft Excel spreadsheet (best for data analysis)
     * - CSV: Comma-Separated Values (best for importing to other systems)
     *
     * JSON example:
     * {
     *   "format": "PDF",
     *   ...
     * }
     *
     * Different use cases:
     * - PDF: Member statements, archival, printing
     * - EXCEL: Financial analysis, charts, pivot tables
     * - CSV: Data transfer, bulk imports, external systems
     *
     * Validation:
     * @NotNull - Must specify a format
     */
    @NotNull(message = "Report format is required")
    private ReportFormat format;

    /**
     * ENTITY ID (OPTIONAL)
     *
     * ID of specific entity to generate report for
     * Required for entity-specific reports, null for system-wide reports
     *
     * What to put here depends on report type:
     * - MEMBER_STATEMENT → Member ID (e.g., 123)
     * - EMPLOYER_REPORT → Employer ID (e.g., 456)
     * - CONTRIBUTION_SUMMARY → null (system-wide)
     * - BENEFIT_CLAIMS → Member ID or null (all claims)
     *
     * JSON examples:
     *
     * Member statement (entity-specific):
     * {
     *   "reportType": "MEMBER_STATEMENT",
     *   "entityId": 123,
     *   ...
     * }
     *
     * System-wide contribution summary:
     * {
     *   "reportType": "CONTRIBUTION_SUMMARY",
     *   "entityId": null,
     *   ...
     * }
     *
     * Validation: None (optional field)
     * Service layer should validate if entityId is required for the specific reportType
     */
    private Long entityId;

    /**
     * DATE RANGE START (OPTIONAL)
     *
     * Start date for filtering report data
     * Used for period-based reports
     *
     * Format: ISO 8601 date-time
     * Example: "2025-01-01T00:00:00"
     *
     * @JsonFormat annotation:
     * - Tells Jackson (JSON library) how to parse date strings
     * - pattern: Date format to expect
     * - ISO8601 format: yyyy-MM-dd'T'HH:mm:ss
     *
     * JSON example:
     * {
     *   "startDate": "2025-01-01T00:00:00",
     *   "endDate": "2025-01-31T23:59:59",
     *   ...
     * }
     *
     * Use case:
     * "Generate contribution summary for January 2025"
     * - startDate: 2025-01-01 00:00:00
     * - endDate: 2025-01-31 23:59:59
     *
     * If null: Report includes all data (or from beginning of time)
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    /**
     * DATE RANGE END (OPTIONAL)
     *
     * End date for filtering report data
     *
     * Same format as startDate
     *
     * If null: Report includes data up to current date
     *
     * Validation in service:
     * - Should check: endDate >= startDate
     * - Should check: endDate <= now (can't generate report for future)
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;

    /**
     * ADDITIONAL PARAMETERS (OPTIONAL)
     *
     * Extra filter criteria or settings for report generation
     * Flexible key-value map for report-specific options
     *
     * Different reports need different parameters:
     *
     * Example 1: Contribution Summary with filters
     * {
     *   "reportType": "CONTRIBUTION_SUMMARY",
     *   "parameters": {
     *     "contributionType": "MONTHLY",
     *     "status": "COMPLETED",
     *     "minAmount": 5000
     *   }
     * }
     *
     * Example 2: Member Statement with options
     * {
     *   "reportType": "MEMBER_STATEMENT",
     *   "entityId": 123,
     *   "parameters": {
     *     "includeGraphs": true,
     *     "includeProjections": true,
     *     "currency": "NGN"
     *   }
     * }
     *
     * Example 3: Employer Report with grouping
     * {
     *   "reportType": "EMPLOYER_REPORT",
     *   "entityId": 456,
     *   "parameters": {
     *     "groupBy": "department",
     *     "includeInactive": false
     *   }
     * }
     *
     * Why Map<String, Object>?
     * - Flexible: Each report type can have different parameters
     * - No schema changes: Add new parameters without changing DTO
     * - Type flexibility: Can store strings, numbers, booleans, etc.
     *
     * In service layer:
     * // Extract and validate parameters
     * String groupBy = (String) parameters.get("groupBy");
     * Boolean includeInactive = (Boolean) parameters.get("includeInactive");
     * Integer minAmount = (Integer) parameters.get("minAmount");
     *
     * If null: Use default parameters for the report type
     */
    private Map<String, Object> parameters;

    /**
     * REQUESTED BY
     *
     * Username or email of person requesting the report
     * Used for audit trail and permissions
     *
     * Examples:
     * - "admin@pensionsystem.com"
     * - "john.doe@company.com"
     *
     * In production:
     * - Should be extracted from authentication token
     * - Don't trust client to send correct value
     * - Server should override with authenticated user
     *
     * Example in controller:
     * String authenticatedUser = SecurityContextHolder.getContext()
     *     .getAuthentication().getName();
     * request.setRequestedBy(authenticatedUser);
     *
     * Validation:
     * @NotBlank - Must specify who requested the report
     *
     * Security note:
     * - Check if user has permission to generate this report type
     * - Check if user can access the specified entityId
     * - Log all report generation requests for audit
     */
    @NotBlank(message = "Requested by is required")
    private String requestedBy;
}
