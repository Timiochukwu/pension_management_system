package pension_management_system.pension.audit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pension_management_system.pension.audit.entity.AuditLog;
import pension_management_system.pension.audit.repository.AuditLogRepository;

/**
 * AuditService - Service for logging audit events
 *
 * Purpose: Centralized audit logging
 *
 * Key Features:
 * - Async logging (@Async) - non-blocking
 * - Never throws exceptions (fire and forget)
 * - Centralized logging logic
 *
 * Usage:
 * ```java
 * @Autowired
 * private AuditService auditService;
 *
 * auditService.log(AuditLog.builder()
 *     .action(AuditAction.CREATE)
 *     .entityType("Member")
 *     .entityId(member.getId().toString())
 *     .userEmail(getCurrentUser())
 *     .timestamp(LocalDateTime.now())
 *     .build());
 * ```
 *
 * @Service - Spring service
 * @RequiredArgsConstructor - Constructor injection
 * @Slf4j - Logging
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * LOG AUDIT EVENT
     *
     * Async method - returns immediately
     * Actual logging happens in background thread
     *
     * Never throws exceptions:
     * - Audit logging shouldn't break business logic
     * - If logging fails, just log error
     * - Continue with business operation
     *
     * @param auditLog Audit log to save
     */
    @Async
    public void log(AuditLog auditLog) {
        try {
            auditLogRepository.save(auditLog);
            log.debug("Audit log saved: {} - {} - {}",
                    auditLog.getAction(),
                    auditLog.getEntityType(),
                    auditLog.getUserEmail());
        } catch (Exception e) {
            // Log error but don't throw
            // Audit failure shouldn't break business logic
            log.error("Failed to save audit log: {}", e.getMessage(), e);
        }
    }

    /**
     * HELPER: Log simple action
     *
     * Quick method for basic logging
     *
     * @param userEmail Who performed action
     * @param action What action
     * @param entityType What entity type
     * @param entityId Which entity
     */
    @Async
    public void logAction(String userEmail, String action, String entityType, String entityId) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .timestamp(java.time.LocalDateTime.now())
                    .userEmail(userEmail)
                    .action(pension_management_system.pension.audit.entity.AuditAction.valueOf(action))
                    .entityType(entityType)
                    .entityId(entityId)
                    .success(true)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage(), e);
        }
    }
}
