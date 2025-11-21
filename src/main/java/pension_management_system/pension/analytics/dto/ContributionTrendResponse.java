package pension_management_system.pension.analytics.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * ContributionTrendResponse DTO - Time-series data showing contribution trends
 *
 * Purpose: Provides month-by-month contribution statistics
 * Used to display trend graphs and charts on dashboard
 *
 * What is a Trend Analysis?
 * - Shows how metrics change over time
 * - Helps identify patterns (growth, seasonality, decline)
 * - Enables forecasting future values
 * - Highlights anomalies and issues
 *
 * Example Use Case:
 * Admin wants to see "How did contributions grow over the last 12 months?"
 * - January 2025: ₦15M (320 contributions)
 * - February 2025: ₦18M (385 contributions) ↑ +20%
 * - March 2025: ₦16M (340 contributions) ↓ -11%
 *
 * Visualization:
 * This data is typically shown as:
 * - Line chart: Amount over time
 * - Bar chart: Count per month
 * - Combined chart: Both amount and count
 *
 * Example JSON Response:
 * {
 *   "monthlyTrend": [
 *     {
 *       "month": "JANUARY",
 *       "year": 2025,
 *       "count": 320,
 *       "amount": 15000000.00
 *     },
 *     {
 *       "month": "FEBRUARY",
 *       "year": 2025,
 *       "count": 385,
 *       "amount": 18000000.00
 *     },
 *     ...
 *   ]
 * }
 *
 * Client-side Usage:
 * const labels = data.monthlyTrend.map(d => `${d.month} ${d.year}`);
 * const amounts = data.monthlyTrend.map(d => d.amount);
 * createLineChart(labels, amounts);
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
public class ContributionTrendResponse {

    /**
     * MONTHLY TREND DATA
     *
     * List of monthly statistics, typically sorted by year and month
     * Each element represents one month's contribution data
     *
     * Use case:
     * - Display on line/bar chart
     * - Calculate growth rates: "15% increase from last month"
     * - Identify seasonal patterns: "Contributions spike in December (bonuses)"
     * - Spot anomalies: "Why did contributions drop in March?"
     *
     * Typical query:
     * SELECT
     *   MONTH(contribution_date) as month,
     *   YEAR(contribution_date) as year,
     *   COUNT(*) as count,
     *   SUM(contribution_amount) as amount
     * FROM contributions
     * WHERE contribution_date >= '2024-01-01'
     *   AND status = 'COMPLETED'
     * GROUP BY YEAR(contribution_date), MONTH(contribution_date)
     * ORDER BY year, month
     *
     * Example analysis:
     * - Month-over-month growth
     * - Year-over-year comparison
     * - Forecasting next month
     */
    private List<MonthlyData> monthlyTrend;

    /**
     * MonthlyData - Data for a single month
     *
     * Nested static class representing one month's contribution statistics
     *
     * Why static nested class?
     * - Logically grouped with parent class
     * - Only used in this context
     * - Cleaner than separate file
     * - Easy to understand the relationship
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MonthlyData {

        /**
         * MONTH NAME
         *
         * Name of the month
         *
         * Possible values:
         * - "JANUARY", "FEBRUARY", "MARCH", etc. (uppercase)
         * - "Jan", "Feb", "Mar", etc. (abbreviated)
         * - "01", "02", "03", etc. (numeric)
         *
         * Depends on backend formatting choice
         *
         * Use case:
         * - X-axis label on chart
         * - Display: "January 2025"
         *
         * Client-side formatting:
         * const monthName = {
         *   "JANUARY": "Jan",
         *   "FEBRUARY": "Feb",
         *   ...
         * }[data.month];
         */
        private String month;

        /**
         * YEAR
         *
         * Year for this month's data
         *
         * Examples: 2024, 2025, 2026
         *
         * Use case:
         * - Distinguish same month in different years
         * - Multi-year trend analysis
         * - Display: "January 2025" vs "January 2024"
         *
         * Why separate from month?
         * - Easier to group by year
         * - Simpler date manipulation
         * - Flexible formatting
         */
        private Integer year;

        /**
         * CONTRIBUTION COUNT
         *
         * Number of contributions made in this month
         * Typically only counts COMPLETED contributions
         *
         * Use case:
         * - Transaction volume: "385 contributions in February"
         * - Trend: "Count increasing month-over-month"
         * - Capacity planning: "Can system handle 500/month?"
         *
         * Calculation:
         * SELECT COUNT(*)
         * FROM contributions
         * WHERE MONTH(contribution_date) = 2
         *   AND YEAR(contribution_date) = 2025
         *   AND status = 'COMPLETED'
         *
         * Visualization:
         * - Bar chart showing count per month
         * - Line chart showing count trend
         */
        private Long count;

        /**
         * TOTAL CONTRIBUTION AMOUNT
         *
         * Sum of all contribution amounts in this month
         * Currency value (NGN, USD, etc.)
         * Only includes COMPLETED contributions
         *
         * Use case:
         * - Revenue tracking: "₦18M in February"
         * - Growth calculation: "20% increase from January"
         * - Forecasting: "Expect ₦19M in March"
         * - Variance analysis: "Why is March lower?"
         *
         * Calculation:
         * SELECT SUM(contribution_amount)
         * FROM contributions
         * WHERE MONTH(contribution_date) = 2
         *   AND YEAR(contribution_date) = 2025
         *   AND status = 'COMPLETED'
         *
         * Visualization:
         * - Line chart showing amount trend
         * - Area chart showing cumulative growth
         * - Comparison chart: This year vs last year
         *
         * Display format:
         * - ₦18,000,000.00
         * - ₦18M (abbreviated)
         * - $18,000 (with conversion)
         */
        private BigDecimal amount;

        /**
         * HELPER: Calculate average contribution amount
         *
         * Not serialized in JSON (@JsonIgnore prevents serialization)
         * Can be computed on client or server side
         *
         * @return Average contribution amount for this month
         */
        @JsonIgnore
        public BigDecimal getAverageAmount() {
            if (count == null || count == 0 || amount == null) {
                return BigDecimal.ZERO;
            }
            return amount.divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP);
        }
    }
}
