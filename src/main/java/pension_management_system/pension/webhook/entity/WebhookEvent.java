package pension_management_system.pension.webhook.entity;

/**
 * WebhookEvent - Available webhook event types
 *
 * Events that can trigger webhooks
 */
public enum WebhookEvent {
    // Member events
    MEMBER_CREATED,
    MEMBER_UPDATED,
    MEMBER_DELETED,

    // Contribution events
    CONTRIBUTION_CREATED,
    CONTRIBUTION_UPDATED,
    CONTRIBUTION_COMPLETED,

    // Payment events
    PAYMENT_INITIATED,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,

    // Benefit events
    BENEFIT_CREATED,
    BENEFIT_APPROVED,
    BENEFIT_REJECTED,
    BENEFIT_PAID,

    // System events
    SYSTEM_ALERT,
    FRAUD_DETECTED
}
