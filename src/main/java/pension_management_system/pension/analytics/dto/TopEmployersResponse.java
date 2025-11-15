package pension_management_system.pension.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * TopEmployersResponse DTO - Ranking of employers by contribution volume
 *
 * Purpose: Shows which employers contribute the most to the pension system
 * Used for business analytics, relationship management, and strategic planning
 *
 * Why track top employers?
 * - Revenue concentration: "Top 10 employers = 70% of contributions"
 * - Relationship management: "Focus support on top contributors"
 * - Risk assessment: "Too dependent on one employer"
 * - Sales/marketing: "Target similar companies"
 * - Benchmarking: "Compare employer performance"
 *
 * Example Use Case:
 * Business Development Manager wants to know "Who are our most valuable employers?"
 * 1. Tech Corp Ltd: 850 members, ₦85M contributions
 * 2. Finance Group: 620 members, ₦62M contributions
 * 3. Manufacturing Co: 540 members, ₦54M contributions
 *
 * Insights:
 * - Tech companies tend to have higher contribution amounts
 * - Large employers bring in more revenue
 * - Should we offer special service tiers for top employers?
 *
 * Visualization:
 * - Bar chart: Horizontal bars showing contribution amounts
 * - Table: Ranked list with company names and metrics
 * - Leaderboard: Gamified display of top employers
 *
 * Example JSON Response:
 * {
 *   "topEmployers": [
 *     {
 *       "employerId": "123",
 *       "companyName": "Tech Corp Ltd",
 *       "memberCount": 850,
 *       "totalContributions": 85000000.00
 *     },
 *     {
 *       "employerId": "456",
 *       "companyName": "Finance Group",
 *       "memberCount": 620,
 *       "totalContributions": 62000000.00
 *     },
 *     ...
 *   ]
 * }
 *
 * Client-side Usage:
 * // Display as leaderboard
 * data.topEmployers.forEach((employer, index) => {
 *   console.log(`#${index + 1}: ${employer.companyName} - ₦${employer.totalContributions.toLocaleString()}`);
 * });
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
public class TopEmployersResponse {

    /**
     * TOP EMPLOYERS LIST
     *
     * List of employers ranked by total contributions
     * Typically sorted by totalContributions (descending)
     * Usually limited to top 10, 20, or 50 employers
     *
     * Use case:
     * - Display as leaderboard
     * - Show in bar chart
     * - Identify VIP accounts
     * - Focus relationship management efforts
     *
     * Typical query:
     * SELECT
     *   e.id as employer_id,
     *   e.company_name,
     *   COUNT(DISTINCT m.id) as member_count,
     *   SUM(c.contribution_amount) as total_contributions
     * FROM employers e
     * LEFT JOIN members m ON m.employer_id = e.id
     * LEFT JOIN contributions c ON c.member_id = m.id
     * WHERE c.status = 'COMPLETED'
     * GROUP BY e.id, e.company_name
     * ORDER BY total_contributions DESC
     * LIMIT 10
     *
     * Example analysis:
     * - Revenue concentration: "Top 10 = X% of total"
     * - Growth tracking: "Tech Corp grew 20% YoY"
     * - Churn risk: "If top employer leaves, we lose 15% revenue"
     * - Cross-sell opportunity: "Employer X has high members but low contributions"
     */
    private List<EmployerData> topEmployers;

    /**
     * EmployerData - Data for a single employer
     *
     * Nested static class representing metrics for one employer
     *
     * Why static nested class?
     * - Logically grouped with parent class
     * - Only used in top employers context
     * - Cleaner than separate file
     * - Clear parent-child relationship
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployerData {

        /**
         * EMPLOYER ID
         *
         * Unique identifier for the employer
         * Database primary key
         *
         * Use case:
         * - Link to employer detail page: /employers/{employerId}
         * - Fetch more employer information
         * - Track employer across systems
         *
         * Example: "123", "456", "789"
         *
         * UI usage:
         * <a href={`/employers/${employer.employerId}`}>
         *   {employer.companyName}
         * </a>
         */
        private String employerId;

        /**
         * COMPANY NAME
         *
         * Registered name of the employer company
         *
         * Examples:
         * - "Tech Corp Ltd"
         * - "Finance Group Nigeria"
         * - "Manufacturing Company PLC"
         *
         * Use case:
         * - Display in leaderboard
         * - Show in charts as labels
         * - Search/filter employers
         *
         * Display formatting:
         * - Truncate if too long: "Very Long Company Nam..."
         * - Add logo/icon if available
         * - Highlight if current user's employer
         */
        private String companyName;

        /**
         * MEMBER COUNT
         *
         * Number of members (employees) from this employer
         * Indicates company size and reach
         *
         * Use case:
         * - Size indicator: "850 members = large employer"
         * - Benchmark: "Average employer has 200 members"
         * - Growth tracking: "Gained 50 new members this year"
         * - Capacity planning: "Can system handle 1000+ member employers?"
         *
         * Calculation:
         * SELECT COUNT(DISTINCT id)
         * FROM members
         * WHERE employer_id = '123'
         *
         * Insights:
         * - High memberCount + high totalContributions = Very valuable employer
         * - High memberCount + low totalContributions = Low average contribution (investigate)
         * - Low memberCount + high totalContributions = High-value employees
         *
         * Display:
         * - "850 members"
         * - "850 👥" (with icon)
         */
        private Long memberCount;

        /**
         * TOTAL CONTRIBUTIONS
         *
         * Sum of all contributions from this employer's members
         * Currency value (NGN, USD, etc.)
         * Only includes COMPLETED contributions
         *
         * Use case:
         * - Revenue ranking: "Top contributor: ₦85M"
         * - Market share: "Tech Corp = 12% of total contributions"
         * - Account value: "VIP employer worth ₦85M/year"
         * - Retention priority: "Focus on retaining high-value employers"
         *
         * Calculation:
         * SELECT SUM(c.contribution_amount)
         * FROM contributions c
         * JOIN members m ON c.member_id = m.id
         * WHERE m.employer_id = '123'
         *   AND c.status = 'COMPLETED'
         *
         * Strategic value:
         * - ₦80M+ : Tier 1 - VIP service, dedicated account manager
         * - ₦50M-80M : Tier 2 - Premium service
         * - ₦20M-50M : Tier 3 - Standard plus service
         * - <₦20M : Tier 4 - Standard service
         *
         * Display format:
         * - ₦85,000,000.00
         * - ₦85M (abbreviated)
         * - $85,000 (with conversion)
         *
         * Chart usage:
         * - Bar length proportional to amount
         * - Color-coded by tier
         * - Tooltip shows exact amount
         */
        private BigDecimal totalContributions;

        /**
         * HELPER: Calculate average contribution per member
         *
         * Average contribution amount per member for this employer
         * Indicates employee income level / contribution rate
         *
         * Use case:
         * - Quality metric: "₦100K avg = high-value employees"
         * - Benchmark: "Industry average is ₦75K"
         * - Segmentation: "Tech companies have higher avg"
         *
         * Example:
         * - Tech Corp: ₦85M / 850 members = ₦100,000 avg
         * - Finance Group: ₦62M / 620 members = ₦100,000 avg
         * - Manufacturing: ₦54M / 540 members = ₦100,000 avg
         *
         * Insights:
         * - High avg = High salaries or high contribution rates
         * - Low avg = Entry-level employees or low contribution rates
         * - Trending up = Employee promotions / salary increases
         *
         * @return Average contribution per member
         */
        public BigDecimal getAveragePerMember() {
            if (memberCount == null || memberCount == 0 || totalContributions == null) {
                return BigDecimal.ZERO;
            }
            return totalContributions.divide(BigDecimal.valueOf(memberCount), 2, java.math.RoundingMode.HALF_UP);
        }
    }
}
