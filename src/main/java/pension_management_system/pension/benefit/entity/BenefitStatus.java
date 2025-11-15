package pension_management_system.pension.benefit.entity;

public enum BenefitStatus {
    PENDING,        // Application submitted, awaiting review
    UNDER_REVIEW,   // Being reviewed by administrators
    APPROVED,       // Approved for disbursement
    DISBURSED,      // Payment completed
    REJECTED,       // Application rejected
    CANCELLED       // Cancelled by member or system
}
