package pension_management_system.pension.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * MemberStatusDistribution DTO - Distribution of members by status
 *
 * Purpose: Shows breakdown of how members are distributed across different statuses
 * Used for pie charts, donut charts, and status analytics
 *
 * What is a Distribution?
 * - Shows how a total is divided into categories
 * - Each category has a count and percentage
 * - All percentages add up to 100%
 * - Helps understand composition
 *
 * Example Use Case:
 * Admin wants to see "What percentage of members are active?"
 * - ACTIVE: 4,890 members (90%)
 * - INACTIVE: 542 members (10%)
 * - SUSPENDED: 20 members (0.4%)
 *
 * Visualization:
 * This data is typically shown as:
 * - Pie chart: Each status as a slice
 * - Donut chart: Hollow pie chart
 * - Bar chart: Horizontal bars showing counts
 * - Table: List with counts and percentages
 *
 * Example JSON Response:
 * {
 *   "distribution": [
 *     {
 *       "status": "ACTIVE",
 *       "count": 4890,
 *       "percentage": 90.0
 *     },
 *     {
 *       "status": "INACTIVE",
 *       "count": 542,
 *       "percentage": 10.0
 *     },
 *     {
 *       "status": "SUSPENDED",
 *       "count": 20,
 *       "percentage": 0.4
 *     }
 *   ]
 * }
 *
 * Client-side Usage (Chart.js):
 * const labels = data.distribution.map(d => d.status);
 * const counts = data.distribution.map(d => d.count);
 * const colors = {
 *   'ACTIVE': 'green',
 *   'INACTIVE': 'gray',
 *   'SUSPENDED': 'red'
 * };
 * createPieChart(labels, counts, colors);
 *
 * Annotations Explained:
 * @Data - Lombok generates getters, setters, toString, equals, hashCode
 * @Builder - Enables builder pattern
 * @NoArgsConstructor - No-argument constructor
 * @AllArgsConstructor - Constructor with all fields
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberStatusDistribution {

    /**
     * STATUS DISTRIBUTION LIST
     *
     * List of status categories with their counts and percentages
     * Typically sorted by count (descending) or by status name
     *
     * Use case:
     * - Display on pie/donut chart
     * - Show in table format
     * - Calculate health metrics: "90% active is good"
     * - Identify issues: "Why do we have 10% inactive?"
     *
     * Typical query:
     * SELECT
     *   status,
     *   COUNT(*) as count,
     *   ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM members), 2) as percentage
     * FROM members
     * GROUP BY status
     * ORDER BY count DESC
     *
     * Example analysis:
     * - Engagement rate: Active / Total
     * - Churn rate: Inactive / Total
     * - Risk assessment: Suspended / Total
     */
    private List<StatusData> distribution;

    /**
     * StatusData - Data for a single status category
     *
     * Nested static class representing count and percentage for one status
     *
     * Why static nested class?
     * - Logically grouped with parent class
     * - Only used in this context (status distribution)
     * - Cleaner than separate file
     * - Clear relationship between classes
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusData {

        /**
         * STATUS NAME
         *
         * The member status category
         *
         * Possible values depend on system:
         * - "ACTIVE": Member is actively contributing
         * - "INACTIVE": Member hasn't contributed recently
         * - "SUSPENDED": Member account suspended
         * - "PENDING": Member registration pending approval
         * - "CLOSED": Member account closed/terminated
         *
         * Use case:
         * - Pie chart label: "Active: 4,890 (90%)"
         * - Legend: Color-coded status names
         * - Filter: "Show only ACTIVE members"
         *
         * Display formatting:
         * - "ACTIVE" → "Active"
         * - "INACTIVE" → "Inactive"
         * - Add icons: ✓ Active, ⏸ Inactive, ⛔ Suspended
         */
        private String status;

        /**
         * MEMBER COUNT
         *
         * Number of members in this status category
         *
         * Use case:
         * - Absolute numbers: "4,890 active members"
         * - Comparison: "More active than inactive"
         * - Trending: "Active count growing each month"
         *
         * Calculation:
         * SELECT COUNT(*)
         * FROM members
         * WHERE status = 'ACTIVE'
         *
         * Visualization:
         * - Pie chart slice size (proportional to count)
         * - Bar length in bar chart
         * - Number displayed in chart
         */
        private Long count;

        /**
         * PERCENTAGE
         *
         * Percentage of total members in this status
         * Should be between 0.0 and 100.0
         * All percentages across statuses should sum to ~100%
         *
         * Use case:
         * - Quick health check: "90% active is good!"
         * - Pie chart labels: "Active (90%)"
         * - KPI: "Target: Keep >85% active"
         * - Alert: "Inactive percentage rising!"
         *
         * Calculation:
         * percentage = (count / totalMembers) * 100
         *
         * Example:
         * - Active: 4,890 / 5,432 * 100 = 90.0%
         * - Inactive: 542 / 5,432 * 100 = 10.0%
         *
         * Display format:
         * - 90.0% (one decimal)
         * - 90% (no decimals)
         * - "90.0% (4,890 of 5,432)"
         *
         * Validation:
         * - Should be >= 0.0
         * - Should be <= 100.0
         * - Sum of all should ≈ 100% (may be 99.9% or 100.1% due to rounding)
         */
        private Double percentage;

        /**
         * HELPER: Get display label
         *
         * Formats a nice display string for UI
         * Example: "Active: 4,890 (90.0%)"
         *
         * Not serialized in JSON by default
         *
         * @return Formatted display label
         */
        public String getDisplayLabel() {
            return String.format("%s: %,d (%.1f%%)", status, count, percentage);
        }
    }
}
