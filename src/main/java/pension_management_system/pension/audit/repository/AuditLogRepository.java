package pension_management_system.pension.audit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pension_management_system.pension.audit.entity.AuditAction;
import pension_management_system.pension.audit.entity.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AuditLogRepository - Database access for audit logs
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Find all logs by user
    List<AuditLog> findByUserEmailOrderByTimestampDesc(String userEmail);

    // Find logs by action type
    List<AuditLog> findByActionOrderByTimestampDesc(AuditAction action);

    // Find logs for specific entity
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, String entityId);

    // Find logs in date range
    Page<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // Find failed actions
    List<AuditLog> findBySuccessFalseOrderByTimestampDesc();
}
