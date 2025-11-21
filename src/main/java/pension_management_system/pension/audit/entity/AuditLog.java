package pension_management_system.pension.audit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AuditLog - Record of all system actions
 *
 * Purpose: Track who did what, when for compliance
 *
 * Why audit logging?
 * - Regulatory compliance (financial regulations)
 * - Security monitoring (detect unauthorized access)
 * - Troubleshooting (understand what happened)
 * - Accountability (prove who made changes)
 *
 * What to log:
 * - All CREATE, UPDATE, DELETE operations
 * - Login/logout events
 * - Permission changes
 * - Payment transactions
 * - Benefit approvals/rejections
 *
 * Compliance requirements:
 * - GDPR: Log data access and modifications
 * - SOX: Track financial transactions
 * - PCI-DSS: Log payment operations
 * - Industry-specific: Pension regulations
 *
 * Retention:
 * - Keep audit logs for 7+ years
 * - Never delete (regulatory requirement)
 * - Archive old logs to cheap storage
 *
 * @Entity - JPA entity
 * @Table - Database table
 * @Data - Lombok getters/setters
 * @Builder - Builder pattern
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_user_email", columnList = "userEmail"),
        @Index(name = "idx_timestamp", columnList = "timestamp"),
        @Index(name = "idx_entity_type", columnList = "entityType"),
        @Index(name = "idx_action", columnList = "action")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    /**
     * PRIMARY KEY
     * Auto-generated ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * WHEN
     * Timestamp of action
     * Indexed for fast queries
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * WHO
     * Email of user who performed action
     * Indexed for user activity queries
     *
     * Examples:
     * - "admin@pension.com"
     * - "john.doe@company.com"
     * - "system" (for automated actions)
     */
    @Column(nullable = false, length = 255)
    private String userEmail;

    /**
     * WHAT ACTION
     * Type of operation performed
     *
     * Common values:
     * - CREATE
     * - UPDATE
     * - DELETE
     * - LOGIN
     * - LOGOUT
     * - APPROVE
     * - REJECT
     * - EXPORT
     */
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AuditAction action;

    /**
     * WHAT ENTITY
     * Type of entity affected
     *
     * Examples:
     * - Member
     * - Contribution
     * - Payment
     * - Benefit
     * - Report
     * - User
     */
    @Column(nullable = false, length = 100)
    private String entityType;

    /**
     * WHICH ONE
     * ID of affected entity
     *
     * Examples:
     * - Member ID: 123
     * - Payment reference: PMT-123456
     */
    @Column(length = 100)
    private String entityId;

    /**
     * DETAILS
     * Additional information about action
     *
     * Can be JSON with before/after values:
     * {
     *   "before": {"status": "PENDING"},
     *   "after": {"status": "APPROVED"},
     *   "reason": "Meets all criteria"
     * }
     *
     * @Column(columnDefinition = "TEXT")
     * - Allows storing large JSON strings
     */
    @Column(columnDefinition = "TEXT")
    private String details;

    /**
     * WHERE FROM
     * IP address of request
     *
     * Security:
     * - Detect suspicious activity from unusual IPs
     * - Geo-location analysis
     * - Rate limiting per IP
     */
    @Column(length = 45)
    private String ipAddress;

    /**
     * USER AGENT
     * Browser/client information
     *
     * Examples:
     * - "Mozilla/5.0 (Windows NT 10.0; Win64; x64)..."
     * - "PostmanRuntime/7.26.8"
     * - "Mobile App v1.2.3"
     *
     * Uses:
     * - Detect automated bots
     * - Track mobile vs web usage
     * - Security monitoring
     */
    @Column(length = 500)
    private String userAgent;

    /**
     * SUCCESS STATUS
     * Whether action succeeded or failed
     *
     * true = Success
     * false = Failed (with error details)
     *
     * Failed actions also logged for security
     * Example: Failed login attempts
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean success = true;

    /**
     * ERROR MESSAGE
     * If action failed, why?
     *
     * Examples:
     * - "Insufficient permissions"
     * - "Invalid amount"
     * - "Member not found"
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}

/**
 * USAGE EXAMPLES
 *
 * 1. Log member creation:
 * ```java
 * auditService.log(AuditLog.builder()
 *     .timestamp(LocalDateTime.now())
 *     .userEmail(getCurrentUser())
 *     .action(AuditAction.CREATE)
 *     .entityType("Member")
 *     .entityId(member.getId().toString())
 *     .details("Created new member: " + member.getEmail())
 *     .ipAddress(getClientIP())
 *     .userAgent(getUserAgent())
 *     .success(true)
 *     .build());
 * ```
 *
 * 2. Log payment transaction:
 * ```java
 * String details = String.format(
 *     "{\"amount\": %s, \"gateway\": \"%s\", \"reference\": \"%s\"}",
 *     payment.getAmount(),
 *     payment.getGateway(),
 *     payment.getReference()
 * );
 *
 * auditService.log(AuditLog.builder()
 *     .timestamp(LocalDateTime.now())
 *     .userEmail(member.getEmail())
 *     .action(AuditAction.PAYMENT_COMPLETED)
 *     .entityType("Payment")
 *     .entityId(payment.getReference())
 *     .details(details)
 *     .ipAddress(request.getRemoteAddr())
 *     .success(true)
 *     .build());
 * ```
 *
 * 3. Log failed login:
 * ```java
 * auditService.log(AuditLog.builder()
 *     .timestamp(LocalDateTime.now())
 *     .userEmail(loginRequest.getUsername())
 *     .action(AuditAction.LOGIN_FAILED)
 *     .entityType("User")
 *     .details("Invalid password")
 *     .ipAddress(request.getRemoteAddr())
 *     .userAgent(request.getHeader("User-Agent"))
 *     .success(false)
 *     .errorMessage("Bad credentials")
 *     .build());
 * ```
 *
 * 4. Log benefit approval:
 * ```java
 * String details = String.format(
 *     "{\"before\": {\"status\": \"%s\"}, \"after\": {\"status\": \"%s\"}, \"approver\": \"%s\"}",
 *     "PENDING",
 *     "APPROVED",
 *     getCurrentUser()
 * );
 *
 * auditService.log(AuditLog.builder()
 *     .timestamp(LocalDateTime.now())
 *     .userEmail(getCurrentUser())
 *     .action(AuditAction.APPROVE)
 *     .entityType("Benefit")
 *     .entityId(benefit.getId().toString())
 *     .details(details)
 *     .success(true)
 *     .build());
 * ```
 *
 * QUERYING AUDIT LOGS
 *
 * Find user activity:
 * ```sql
 * SELECT * FROM audit_logs
 * WHERE user_email = 'john@example.com'
 * ORDER BY timestamp DESC
 * LIMIT 100;
 * ```
 *
 * Find failed login attempts:
 * ```sql
 * SELECT * FROM audit_logs
 * WHERE action = 'LOGIN_FAILED'
 * AND timestamp > NOW() - INTERVAL 24 HOUR
 * ORDER BY timestamp DESC;
 * ```
 *
 * Find changes to specific entity:
 * ```sql
 * SELECT * FROM audit_logs
 * WHERE entity_type = 'Member'
 * AND entity_id = '123'
 * ORDER BY timestamp DESC;
 * ```
 *
 * PERFORMANCE CONSIDERATIONS
 *
 * 1. Async Logging:
 *    - Don't slow down main operations
 *    - Use @Async or message queue
 *    - Fire and forget
 *
 * 2. Indexes:
 *    - Index frequently queried columns
 *    - userEmail, timestamp, entityType, action
 *    - Faster audit report generation
 *
 * 3. Partitioning:
 *    - Partition by date (monthly/yearly)
 *    - Faster queries on recent data
 *    - Easier archival
 *
 * 4. Archival:
 *    - Move old logs to archive storage
 *    - Keep 1 year in hot storage
 *    - 7+ years in cold storage
 *
 * SECURITY
 *
 * 1. Immutable:
 *    - Never UPDATE or DELETE audit logs
 *    - Only INSERT allowed
 *    - Proves tampering didn't occur
 *
 * 2. Access Control:
 *    - Only admins can view audit logs
 *    - Separate database user with INSERT-only
 *    - Prevent unauthorized access
 *
 * 3. Encryption:
 *    - Encrypt sensitive data in details
 *    - Hash PII before logging
 *    - Comply with privacy regulations
 */
