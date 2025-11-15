package pension_management_system.pension.benefit.entity;

/**
 * BenefitStatus Enum
 *
 * Purpose: Tracks the current status of a benefit claim through its lifecycle
 *
 * Benefit Claim Lifecycle:
 * 1. Member applies for benefit → PENDING
 * 2. Admin reviews application → UNDER_REVIEW
 * 3. Documents verified, amount calculated → APPROVED
 * 4. Payment sent to member → PAID
 * 5. Or rejected if ineligible → REJECTED
 *
 * Why track status?
 * - Shows progress of benefit claim
 * - Helps admins manage workload
 * - Provides transparency to members
 * - Creates audit trail
 */
public enum BenefitStatus {

    /**
     * PENDING
     * - Benefit claim just submitted
     * - Waiting for admin to review
     * - No action taken yet
     */
    PENDING,

    /**
     * UNDER_REVIEW
     * - Admin is reviewing the claim
     * - Checking eligibility
     * - Verifying documents
     * - Calculating benefit amount
     */
    UNDER_REVIEW,

    /**
     * APPROVED
     * - Benefit claim approved
     * - Amount calculated
     * - Ready for payment
     * - Waiting for finance to process
     */
    APPROVED,

    /**
     * PAID
     * - Benefit payment completed
     * - Money transferred to member
     * - Claim fully processed
     * - Final status for successful claims
     */
    PAID,

    /**
     * REJECTED
     * - Benefit claim rejected
     * - Member not eligible
     * - Or missing required documents
     * - Or failed verification
     * - Reason for rejection should be documented
     */
    REJECTED,

    /**
     * CANCELLED
     * - Benefit claim cancelled
     * - Usually cancelled by member
     * - Before payment was made
     */
    CANCELLED
}
