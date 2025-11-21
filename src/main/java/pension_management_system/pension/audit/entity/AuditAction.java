package pension_management_system.pension.audit.entity;

/**
 * AuditAction - Enum representing audit log actions
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
    PASSWORD_CHANGE,

    // Business Operations
    APPROVE,
    REJECT,
    EXPORT,
    IMPORT,

    // Payment Operations
    PAYMENT_INITIATED,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,

    // Archive Operations
    ARCHIVE,
    RESTORE,

    // System Operations
    SYSTEM,
    SCHEDULED
}
