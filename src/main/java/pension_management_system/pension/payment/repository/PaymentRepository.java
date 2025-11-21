package pension_management_system.pension.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pension_management_system.pension.payment.entity.Payment;
import pension_management_system.pension.payment.entity.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * PaymentRepository - Database operations for Payment entity
 *
 * Purpose: Query and persist payment records
 *
 * Extends JpaRepository which provides:
 * - save(), findById(), findAll(), delete(), etc.
 * - Custom query methods from method names
 * - Spring Data JPA magic
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by our reference
     *
     * Generated SQL:
     * SELECT * FROM payments WHERE reference = ?
     *
     * Use case: Look up payment when user returns from gateway
     */
    Optional<Payment> findByReference(String reference);

    /**
     * Find payment by gateway reference
     *
     * Generated SQL:
     * SELECT * FROM payments WHERE gateway_reference = ?
     *
     * Use case: Match webhook notification to payment
     */
    Optional<Payment> findByGatewayReference(String gatewayReference);

    /**
     * Find all payments for a contribution
     *
     * Generated SQL:
     * SELECT * FROM payments
     * WHERE contribution_id = ?
     * ORDER BY created_at DESC
     *
     * Use case: Show payment history for a contribution
     */
    List<Payment> findByContributionIdOrderByCreatedAtDesc(Long contributionId);

    /**
     * Find expired pending payments
     *
     * Generated SQL:
     * SELECT * FROM payments
     * WHERE status = 'PENDING'
     *   AND created_at < ?
     *
     * Use case: Scheduled job to mark old pending payments as expired
     */
    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime dateTime);

    /**
     * Find all payments by status
     *
     * Generated SQL:
     * SELECT * FROM payments WHERE status = ?
     *
     * Use case: Find all pending payments for synchronization
     */
    List<Payment> findByStatus(PaymentStatus status);
}
