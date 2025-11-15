package pension_management_system.pension.report.entity;

/**
 * ReportType Enum - Types of reports that can be generated
 *
 * Purpose: Categorize different report types for tracking and auditing
 */
public enum ReportType {
    MEMBER_STATEMENT,        // Individual member's contribution statement
    EMPLOYER_REPORT,         // Report of all members under an employer
    CONTRIBUTION_SUMMARY,    // Summary of all contributions in a period
    BENEFIT_CLAIMS,          // Report of all benefit claims
    ANALYTICS_DASHBOARD,     // Dashboard statistics snapshot
    AUDIT_TRAIL             // System audit report
}
