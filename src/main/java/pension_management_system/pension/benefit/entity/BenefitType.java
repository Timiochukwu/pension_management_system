package pension_management_system.pension.benefit.entity;

/**
 * BenefitType Enum
 *
 * Purpose: Defines the different types of pension benefits a member can claim
 *
 * What are Pension Benefits?
 * - Money or services that members receive from their pension fund
 * - Can be received during employment or after retirement
 * - Based on contributions made over time
 *
 * Benefit Types Explained:
 * - RETIREMENT: Monthly payments after member retires (usually age 60+)
 * - DEATH: Lump sum paid to beneficiaries when member dies
 * - DISABILITY: Payments if member becomes permanently disabled
 * - WITHDRAWAL: Early withdrawal of accumulated contributions (penalties may apply)
 * - TEMPORARY_WITHDRAWAL: Small withdrawal while still employed (for specific needs)
 */
public enum BenefitType {

    /**
     * RETIREMENT BENEFIT
     * - Paid when member reaches retirement age (typically 60-65)
     * - Can be monthly pension or lump sum
     * - Based on total contributions plus interest
     */
    RETIREMENT,

    /**
     * DEATH BENEFIT
     * - Paid to beneficiaries when member passes away
     * - Usually paid as lump sum
     * - Includes all contributions plus accrued benefits
     */
    DEATH,

    /**
     * DISABILITY BENEFIT
     * - Paid if member becomes permanently disabled
     * - Can be monthly payments or lump sum
     * - Requires medical certification
     */
    DISABILITY,

    /**
     * WITHDRAWAL BENEFIT
     * - Full withdrawal of all accumulated contributions
     * - Usually when leaving employment or changing jobs
     * - Member loses future pension rights
     */
    WITHDRAWAL,

    /**
     * TEMPORARY WITHDRAWAL
     * - Partial withdrawal while still employed
     * - For specific needs (housing, education, medical emergency)
     * - Subject to rules and limits
     */
    TEMPORARY_WITHDRAWAL
}
