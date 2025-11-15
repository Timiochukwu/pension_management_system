package pension_management_system.pension.monitoring.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pension_management_system.pension.member.entity.MemberStatus;
import pension_management_system.pension.member.repository.MemberRepository;
import pension_management_system.pension.monitoring.service.MetricsService;
import pension_management_system.pension.payment.entity.PaymentStatus;
import pension_management_system.pension.payment.repository.PaymentRepository;

/**
 * MetricsUpdateScheduler - Periodically update metrics
 *
 * Purpose: Update gauge metrics every minute
 *
 * Gauges updated:
 * - Active members count
 * - Pending payments count
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsUpdateScheduler {

    private final MetricsService metricsService;
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;

    @Scheduled(fixedRate = 60000) // Every minute
    public void updateMetrics() {
        try {
            // Update active members count
            long activeMembersCount = memberRepository.findAll().stream()
                    .filter(m -> m.getMemberStatus() == MemberStatus.ACTIVE)
                    .count();
            metricsService.updateActiveMembersCount((int) activeMembersCount);

            // Update pending payments count
            long pendingPaymentsCount = paymentRepository.findAll().stream()
                    .filter(p -> p.getStatus() == PaymentStatus.PENDING ||
                            p.getStatus() == PaymentStatus.PROCESSING)
                    .count();
            metricsService.updatePendingPaymentsCount((int) pendingPaymentsCount);

            log.debug("Metrics updated: activemembers={}, pendingPayments={}",
                    activeMembersCount, pendingPaymentsCount);
        } catch (Exception e) {
            log.error("Failed to update metrics: {}", e.getMessage());
        }
    }
}
