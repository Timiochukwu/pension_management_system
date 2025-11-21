package pension_management_system.pension.audit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pension_management_system.pension.audit.entity.AuditAction;
import pension_management_system.pension.audit.entity.AuditLog;
import pension_management_system.pension.audit.repository.AuditLogRepository;

import java.time.LocalDateTime;

/**
 * AuditService - Service for logging audit events
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Log an audit action
     */
    @Async
    public void logAction(String userEmail, String action, String entityType, String entityId) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .timestamp(LocalDateTime.now())
                    .userEmail(userEmail)
                    .action(AuditAction.valueOf(action))
                    .entityType(entityType)
                    .entityId(entityId)
                    .success(true)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} - {} on {}", userEmail, action, entityType);
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage());
        }
    }

    /**
     * Log an audit action with details
     */
    @Async
    public void logAction(String userEmail, String action, String entityType, String entityId, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .timestamp(LocalDateTime.now())
                    .userEmail(userEmail)
                    .action(AuditAction.valueOf(action))
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(details)
                    .success(true)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} - {} on {}", userEmail, action, entityType);
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage());
        }
    }

    /**
     * Log an audit action with full details
     */
    @Async
    public void log(AuditLog auditLog) {
        try {
            if (auditLog.getTimestamp() == null) {
                auditLog.setTimestamp(LocalDateTime.now());
            }
            auditLogRepository.save(auditLog);
            log.debug("Audit log saved: {}", auditLog.getId());
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }

    /**
     * Log a failed action
     */
    @Async
    public void logFailedAction(String userEmail, String action, String entityType, String errorMessage) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .timestamp(LocalDateTime.now())
                    .userEmail(userEmail)
                    .action(AuditAction.valueOf(action))
                    .entityType(entityType)
                    .success(false)
                    .errorMessage(errorMessage)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Failed audit log created: {} - {} on {}", userEmail, action, entityType);
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage());
        }
    }
}
