package pension_management_system.pension.payment.entity;

/**
 * Enum representing the status of a payment
 */
public enum PaymentStatus {
    INITIATED,      // Payment record created, not yet sent to gateway
    PENDING,        // Sent to gateway, waiting for user to pay
    PROCESSING,     // Being verified with gateway
    SUCCESS,        // Payment confirmed successful
    FAILED,         // Payment failed
    EXPIRED,        // Payment link expired
    CANCELLED,      // Payment cancelled by user
    REFUNDED        // Payment was refunded
}
