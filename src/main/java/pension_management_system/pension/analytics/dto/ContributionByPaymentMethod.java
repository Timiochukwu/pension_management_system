package pension_management_system.pension.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * ContributionByPaymentMethod DTO - Distribution of contributions by payment method
 *
 * Purpose: Shows breakdown of how contributions are made (payment methods used)
 * Used for financial analytics, payment gateway optimization, and user behavior analysis
 *
 * What is a Payment Method?
 * - The way members pay their contributions
 * - Examples: Bank Transfer, Credit Card, Mobile Money, Cash, Cheque
 * - Different methods have different costs, speeds, and reliability
 *
 * Why analyze by payment method?
 * - Optimize payment gateways: "70% use bank transfer"
 * - Cost analysis: "Card payments cost more in fees"
 * - User experience: "Mobile money is growing"
 * - Risk management: "Cash payments need manual verification"
 *
 * Example Use Case:
 * CFO wants to know "Which payment methods do members prefer?"
 * - Bank Transfer: ₦150M (60%) - 3,200 transactions
 * - Mobile Money: ₦75M (30%) - 2,800 transactions
 * - Card: ₦25M (10%) - 500 transactions
 *
 * Insight: Most members use bank transfer, but mobile money is popular for smaller amounts
 *
 * Visualization:
 * - Pie chart: Percentage by payment method
 * - Bar chart: Amount by payment method
 * - Table: Detailed breakdown with counts and totals
 *
 * Example JSON Response:
 * {
 *   "paymentMethods": [
 *     {
 *       "paymentMethod": "BANK_TRANSFER",
 *       "count": 3200,
 *       "totalAmount": 150000000.00,
 *       "percentage": 60.0
 *     },
 *     {
 *       "paymentMethod": "MOBILE_MONEY",
 *       "count": 2800,
 *       "totalAmount": 75000000.00,
 *       "percentage": 30.0
 *     },
 *     {
 *       "paymentMethod": "CARD",
 *       "count": 500,
 *       "totalAmount": 25000000.00,
 *       "percentage": 10.0
 *     }
 *   ]
 * }
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
public class ContributionByPaymentMethod {

    /**
     * PAYMENT METHOD DISTRIBUTION LIST
     *
     * List of payment methods with their counts, amounts, and percentages
     * Typically sorted by totalAmount (descending) to show most-used methods first
     *
     * Use case:
     * - Display on pie chart
     * - Show in table format
     * - Analyze costs: "Card fees are 2% of amount"
     * - Optimize: "Should we add crypto payments?"
     *
     * Typical query:
     * SELECT
     *   payment_method,
     *   COUNT(*) as count,
     *   SUM(contribution_amount) as total_amount,
     *   ROUND(SUM(contribution_amount) * 100.0 /
     *         (SELECT SUM(contribution_amount) FROM contributions WHERE status = 'COMPLETED'), 2
     *   ) as percentage
     * FROM contributions
     * WHERE status = 'COMPLETED'
     * GROUP BY payment_method
     * ORDER BY total_amount DESC
     *
     * Example analysis:
     * - Most popular method (by count or amount)
     * - Fastest growing method (compare to last year)
     * - Cost per method (transaction fees)
     * - Conversion rate by method
     */
    private List<PaymentMethodData> paymentMethods;

    /**
     * PaymentMethodData - Data for a single payment method
     *
     * Nested static class representing statistics for one payment method
     *
     * Why static nested class?
     * - Logically grouped with parent class
     * - Only used in payment method distribution context
     * - Cleaner than separate file
     * - Clear parent-child relationship
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethodData {

        /**
         * PAYMENT METHOD NAME
         *
         * The payment method type
         *
         * Possible values:
         * - "BANK_TRANSFER": Direct bank transfer (most common)
         * - "DIRECT_DEBIT": Automated bank deduction
         * - "CARD": Credit/Debit card payment
         * - "MOBILE_MONEY": Mobile wallet (e.g., M-Pesa, Paga)
         * - "CASH": Physical cash payment
         * - "CHEQUE": Bank cheque
         *
         * Use case:
         * - Chart label: "Bank Transfer: 60%"
         * - Legend: Color-coded payment types
         * - Icon mapping: 🏦 Bank, 💳 Card, 📱 Mobile
         *
         * Display formatting:
         * - "BANK_TRANSFER" → "Bank Transfer"
         * - "MOBILE_MONEY" → "Mobile Money"
         * - Add icons for visual appeal
         */
        private String paymentMethod;

        /**
         * TRANSACTION COUNT
         *
         * Number of contributions made using this payment method
         *
         * Use case:
         * - Volume analysis: "3,200 bank transfers"
         * - Popularity: "Mobile money is second most used"
         * - Average calculation: totalAmount / count = avg per transaction
         *
         * Calculation:
         * SELECT COUNT(*)
         * FROM contributions
         * WHERE payment_method = 'BANK_TRANSFER'
         *   AND status = 'COMPLETED'
         *
         * Insight example:
         * - If count is high but totalAmount is low:
         *   → Payment method used for small transactions
         * - If count is low but totalAmount is high:
         *   → Payment method used for large transactions
         */
        private Long count;

        /**
         * TOTAL CONTRIBUTION AMOUNT
         *
         * Sum of all contributions made via this payment method
         * Currency value (NGN, USD, etc.)
         * Only includes COMPLETED contributions
         *
         * Use case:
         * - Financial analysis: "₦150M via bank transfer"
         * - Revenue breakdown by payment method
         * - Gateway cost analysis: "Card fees = 2% of ₦25M = ₦500K"
         * - Strategic planning: "Should we incentivize cheaper methods?"
         *
         * Calculation:
         * SELECT SUM(contribution_amount)
         * FROM contributions
         * WHERE payment_method = 'BANK_TRANSFER'
         *   AND status = 'COMPLETED'
         *
         * Cost implications:
         * - Bank Transfer: Low fees (~₦50-100 per transaction)
         * - Card: 2-3% of amount (expensive for large amounts)
         * - Mobile Money: ~1% of amount
         * - Cash: Free but high manual processing cost
         *
         * Display format:
         * - ₦150,000,000.00
         * - ₦150M (abbreviated)
         */
        private BigDecimal totalAmount;

        /**
         * PERCENTAGE OF TOTAL
         *
         * Percentage of total contribution amount via this payment method
         * Should be between 0.0 and 100.0
         * All percentages should sum to ~100%
         *
         * Use case:
         * - Quick insight: "60% via bank transfer"
         * - Pie chart slices
         * - KPI: "Target: <5% via expensive card payments"
         * - Trend: "Mobile money growing from 20% to 30%"
         *
         * Calculation:
         * percentage = (totalAmount / grandTotalAmount) * 100
         *
         * Example:
         * - Bank Transfer: ₦150M / ₦250M * 100 = 60.0%
         * - Mobile Money: ₦75M / ₦250M * 100 = 30.0%
         * - Card: ₦25M / ₦250M * 100 = 10.0%
         *
         * Display format:
         * - 60.0% (one decimal)
         * - "60% (₦150M)"
         *
         * Strategic insights:
         * - High percentage in expensive method → Need to incentivize cheaper methods
         * - Low percentage in cheap method → Opportunity to promote it
         * - Shifting percentages → User behavior changing
         */
        private Double percentage;

        /**
         * HELPER: Calculate average transaction amount
         *
         * Average contribution amount per transaction for this payment method
         * Helps understand typical transaction sizes
         *
         * Example:
         * - Bank Transfer: ₦150M / 3,200 = ₦46,875 avg
         * - Mobile Money: ₦75M / 2,800 = ₦26,786 avg
         * - Card: ₦25M / 500 = ₦50,000 avg
         *
         * Insight: Mobile money used for smaller amounts, cards for larger
         *
         * @return Average contribution amount
         */
        public BigDecimal getAverageAmount() {
            if (count == null || count == 0 || totalAmount == null) {
                return BigDecimal.ZERO;
            }
            return totalAmount.divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP);
        }
    }
}
