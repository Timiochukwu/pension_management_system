package pension_management_system.pension.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DashboardStatisticsResponse DTO - High-level statistics for admin dashboard
 *
 * Purpose: Provides summary statistics about the pension system
 * Used to display key metrics on the admin dashboard
 *
 * What is a Dashboard?
 * - Main overview screen showing system health and key metrics
 * - Displays important numbers at a glance
 * - Helps administrators monitor the system
 * - Tracks trends and identifies issues
 *
 * Example Use Case:
 * Admin logs in and sees dashboard showing:
 * - Total members: 5,432
 * - Active members: 4,890 (90%)
 * - Total contributions this month: ₦45,678,900
 * - Pending contributions: 23
 *
 * Example JSON Response:
 * {
 *   "totalMembers": 5432,
 *   "activeMembers": 4890,
 *   "inactiveMembers": 542,
 *   "totalEmployers": 234,
 *   "totalContributions": 12543,
 *   "totalContributionAmount": 245678900.00,
 *   "monthlyContributionAmount": 180234500.00,
 *   "voluntaryContributionAmount": 65444400.00,
 *   "pendingContributions": 23,
 *   "completedContributions": 12490,
 *   "failedContributions": 30
 * }
 *
 * Annotations Explained:
 * @Data - Lombok generates getters, setters, toString, equals, hashCode
 * @Builder - Enables builder pattern: DashboardStatisticsResponse.builder().totalMembers(100L).build()
 * @NoArgsConstructor - Generates no-argument constructor (required for JSON deserialization)
 * @AllArgsConstructor - Generates constructor with all fields (used by @Builder)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsResponse {

    /**
     * TOTAL MEMBERS
     *
     * Total count of all registered members in the system
     * Includes both active and inactive members
     *
     * Use case:
     * - Dashboard KPI: "Total Members: 5,432"
     * - Growth tracking: Compare with last month
     * - System capacity planning
     *
     * Calculation:
     * SELECT COUNT(*) FROM members
     */
    private Long totalMembers;

    /**
     * ACTIVE MEMBERS
     *
     * Count of members who are currently active
     * Active = Members who have made contributions recently or have active status
     *
     * Use case:
     * - Health metric: "90% of members are active"
     * - Engagement tracking
     * - Alert: "Active members decreased by 5%"
     *
     * Calculation:
     * SELECT COUNT(*) FROM members WHERE status = 'ACTIVE'
     *
     * What makes a member active?
     * - Has made a contribution in last 6 months, OR
     * - Status is explicitly set to ACTIVE, OR
     * - Has active employer relationship
     */
    private Long activeMembers;

    /**
     * INACTIVE MEMBERS
     *
     * Count of members who are not currently active
     *
     * Use case:
     * - Identify members to re-engage
     * - Cleanup candidates
     * - Trend analysis: "Why are members going inactive?"
     *
     * Calculation:
     * SELECT COUNT(*) FROM members WHERE status = 'INACTIVE'
     * OR: totalMembers - activeMembers
     */
    private Long inactiveMembers;

    /**
     * TOTAL EMPLOYERS
     *
     * Total count of registered employers in the system
     *
     * Use case:
     * - Dashboard KPI: "234 Employers"
     * - Business development metric
     * - Partner tracking
     *
     * Calculation:
     * SELECT COUNT(*) FROM employers
     */
    private Long totalEmployers;

    /**
     * TOTAL CONTRIBUTIONS (COUNT)
     *
     * Total number of contribution records
     * Includes all statuses: PENDING, COMPLETED, FAILED
     *
     * Use case:
     * - Volume tracking: "12,543 total contributions"
     * - Transaction throughput
     * - System load monitoring
     *
     * Calculation:
     * SELECT COUNT(*) FROM contributions
     */
    private Long totalContributions;

    /**
     * TOTAL CONTRIBUTION AMOUNT
     *
     * Sum of all contribution amounts (in currency)
     * Includes both MONTHLY and VOLUNTARY contributions
     * Only counts COMPLETED contributions
     *
     * Use case:
     * - Main dashboard metric: "₦245,678,900 total contributions"
     * - Financial reporting
     * - Regulatory compliance
     *
     * Calculation:
     * SELECT SUM(contribution_amount)
     * FROM contributions
     * WHERE status = 'COMPLETED'
     *
     * Display format:
     * - ₦245,678,900.00
     * - $245,678.90 (with currency conversion)
     */
    private BigDecimal totalContributionAmount;

    /**
     * MONTHLY CONTRIBUTION AMOUNT
     *
     * Sum of MONTHLY (regular) contribution amounts
     * Excludes voluntary contributions
     * Only counts COMPLETED contributions
     *
     * Use case:
     * - Breakdown: "₦180M monthly + ₦65M voluntary = ₦245M total"
     * - Predictable income tracking
     * - Budget forecasting
     *
     * Calculation:
     * SELECT SUM(contribution_amount)
     * FROM contributions
     * WHERE contribution_type = 'MONTHLY'
     *   AND status = 'COMPLETED'
     *
     * What is MONTHLY contribution?
     * - Regular payroll deductions
     * - Mandatory contributions
     * - Predictable, recurring amounts
     */
    private BigDecimal monthlyContributionAmount;

    /**
     * VOLUNTARY CONTRIBUTION AMOUNT
     *
     * Sum of VOLUNTARY (additional) contribution amounts
     * Extra contributions beyond mandatory monthly amount
     * Only counts COMPLETED contributions
     *
     * Use case:
     * - Member engagement metric: "₦65M in voluntary contributions"
     * - Bonus tracking
     * - Growth indicator (members saving more)
     *
     * Calculation:
     * SELECT SUM(contribution_amount)
     * FROM contributions
     * WHERE contribution_type = 'VOLUNTARY'
     *   AND status = 'COMPLETED'
     *
     * What is VOLUNTARY contribution?
     * - Extra savings by member
     * - Bonus contributions
     * - One-time additional payments
     */
    private BigDecimal voluntaryContributionAmount;

    /**
     * PENDING CONTRIBUTIONS (COUNT)
     *
     * Number of contributions awaiting processing
     * Status = PENDING or PROCESSING
     *
     * Use case:
     * - Operational metric: "23 pending contributions"
     * - Alert: "High number of pending contributions!"
     * - Processing queue monitoring
     *
     * Calculation:
     * SELECT COUNT(*)
     * FROM contributions
     * WHERE status IN ('PENDING', 'PROCESSING')
     *
     * Why track this?
     * - Identify processing bottlenecks
     * - Monitor system performance
     * - Alert on stuck transactions
     *
     * Action items:
     * - If > 100: Investigate processing delays
     * - If unchanged for 24h: System may have issues
     */
    private Long pendingContributions;

    /**
     * COMPLETED CONTRIBUTIONS (COUNT)
     *
     * Number of successfully processed contributions
     * Status = COMPLETED
     *
     * Use case:
     * - Success rate: "12,490 completed / 12,543 total = 99.6%"
     * - Processing efficiency
     * - Trust metric (high completion rate = reliable system)
     *
     * Calculation:
     * SELECT COUNT(*)
     * FROM contributions
     * WHERE status = 'COMPLETED'
     */
    private Long completedContributions;

    /**
     * FAILED CONTRIBUTIONS (COUNT)
     *
     * Number of contributions that failed to process
     * Status = FAILED
     *
     * Use case:
     * - Error tracking: "30 failed contributions"
     * - Failure rate: "30 / 12,543 = 0.24%"
     * - Alert: "Failure rate above threshold!"
     *
     * Calculation:
     * SELECT COUNT(*)
     * FROM contributions
     * WHERE status = 'FAILED'
     *
     * Common failure reasons:
     * - Payment gateway timeout
     * - Insufficient funds
     * - Invalid account details
     * - Network errors
     *
     * Action items:
     * - Investigate failure reasons
     * - Contact members to retry
     * - Fix system issues
     */
    private Long failedContributions;
}
