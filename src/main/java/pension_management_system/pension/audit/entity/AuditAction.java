package pension_management_system.pension.audit.entity;

/**
 * AuditAction - Types of actions to audit
 */
public enum AuditAction {
    // CRUD Operations
    CREATE,
    READ,
    UPDATE,
    DELETE,

    // Authentication
    LOGIN,
    LOGOUT,
    LOGIN_FAILED,

    // Workflow
    APPROVE,
    REJECT,
    SUBMIT,

    // Payments
    PAYMENT_INITIATED,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,

    // Reports
    REPORT_GENERATED,
    REPORT_DOWNLOADED,
    REPORT_DELETED,

    // Data Export
    DATA_EXPORTED,

    // Admin Actions
    PERMISSION_GRANTED,
    PERMISSION_REVOKED,
    ROLE_CHANGED,

    // System Events
    SYSTEM_CONFIG_CHANGED,
    BATCH_JOB_EXECUTED
}
