package pension_management_system.pension.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * RecentActivityResponse DTO - Recent system activities
 *
 * Purpose: Shows recent activities across the pension system
 * Used for dashboards to give users a quick overview of system activity
 *
 * Example Use Case:
 * Dashboard showing:
 * - Recent member registrations
 * - Recent contributions
 * - Recent benefit claims
 * - Recent employer registrations
 *
 * Example JSON Response:
 * {
 *   "recentActivities": [
 *     {
 *       "activityType": "CONTRIBUTION",
 *       "description": "John Doe made a monthly contribution",
 *       "amount": 50000.00,
 *       "timestamp": "2025-01-15T10:30:45"
 *     },
 *     {
 *       "activityType": "MEMBER_REGISTRATION",
 *       "description": "New member Jane Smith registered",
 *       "timestamp": "2025-01-15T09:15:30"
 *     }
 *   ]
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityResponse {

    /**
     * RECENT ACTIVITIES LIST
     *
     * List of recent activities sorted by timestamp (most recent first)
     * Typically limited to last 50-100 activities
     */
    private List<ActivityItem> recentActivities;

    /**
     * ActivityItem - Single activity entry
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityItem {

        /**
         * ACTIVITY TYPE
         *
         * Type of activity that occurred
         * Examples: CONTRIBUTION, MEMBER_REGISTRATION, BENEFIT_CLAIM, EMPLOYER_REGISTRATION
         */
        private String activityType;

        /**
         * DESCRIPTION
         *
         * Human-readable description of the activity
         * Example: "John Doe made a monthly contribution of ₦50,000"
         */
        private String description;

        /**
         * AMOUNT
         *
         * Monetary amount involved (if applicable)
         * Null for activities without amounts (like registrations)
         */
        private BigDecimal amount;

        /**
         * TIMESTAMP
         *
         * When the activity occurred
         */
        private LocalDateTime timestamp;

        /**
         * ENTITY ID
         *
         * ID of the related entity (member ID, contribution ID, etc.)
         */
        private String entityId;

        /**
         * ENTITY NAME
         *
         * Name of the related entity (member name, employer name, etc.)
         */
        private String entityName;
    }
}
