package pension_management_system.pension.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pension_management_system.pension.analytics.service.AnalyticsService;
import pension_management_system.pension.audit.service.AuditService;
import pension_management_system.pension.contribution.entity.Contribution;
import pension_management_system.pension.contribution.entity.ContributionType;
import pension_management_system.pension.contribution.repository.ContributionRepository;
import pension_management_system.pension.member.dto.MemberResponse;
import pension_management_system.pension.member.service.MemberService;
import pension_management_system.pension.notification.service.EmailService;
import pension_management_system.pension.payment.entity.Payment;
import pension_management_system.pension.payment.entity.PaymentStatus;
import pension_management_system.pension.payment.repository.PaymentRepository;
import pension_management_system.pension.payment.service.PaymentService;
import pension_management_system.pension.report.service.ReportService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ScheduledTasks - Automated background jobs
 *
 * Purpose: Run tasks automatically at scheduled times
 *
 * Common use cases:
 * - Send monthly contribution reminders
 * - Generate automated reports
 * - Clean up old data
 * - Send birthday wishes
 * - Check payment statuses
 *
 * Cron Expression Format:
 * ┌───────────── second (0-59)
 * │ ┌───────────── minute (0-59)
 * │ │ ┌───────────── hour (0-23)
 * │ │ │ ┌───────────── day of month (1-31)
 * │ │ │ │ ┌───────────── month (1-12)
 * │ │ │ │ │ ┌───────────── day of week (0-6)
 * │ │ │ │ │ │
 * * * * * * *
 *
 * Examples:
 * "0 0 9 * * *" - Daily at 9 AM
 * "0 0 9 1 * *" - First day of month at 9 AM
 * "0 0 9 * * MON" - Every Monday at 9 AM
 * "0 0/30 * * * *" - Every 30 minutes
 *
 * Enable with @EnableScheduling in main class
 *
 * @Component - Spring component
 * @Slf4j - Logging
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final MemberService memberService;
    private final ContributionRepository contributionRepository;
    private final EmailService emailService;
    private final ReportService reportService;
    private final AnalyticsService analyticsService;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final AuditService auditService;

    /**
     * MONTHLY CONTRIBUTION REMINDERS
     *
     * Runs: First day of every month at 9 AM
     * Purpose: Remind members to make monthly contribution
     *
     * Cron: "0 0 9 1 * *"
     * - Second: 0
     * - Minute: 0
     * - Hour: 9
     * - Day: 1 (first day of month)
     * - Month: * (every month)
     * - Day of week: * (any day)
     */
    @Scheduled(cron = "0 0 9 1 * *")
    public void sendMonthlyContributionReminders() {
        log.info("Starting monthly contribution reminder job at {}", LocalDateTime.now());

        try {
            // Get current month and year
            LocalDate now = LocalDate.now();
            int currentYear = now.getYear();
            int currentMonth = now.getMonthValue();

            // Find all active members
            List<MemberResponse> activeMembers = memberService.getAllActiveMembers();
            log.info("Found {} active members to check for contributions", activeMembers.size());

            int remindersSent = 0;

            for (MemberResponse memberResponse : activeMembers) {
                try {
                    // Check if member has made this month's contribution
                    Optional<Contribution> monthlyContribution = contributionRepository
                            .findMonthlyContributionByMemberIdAndYearMonth(
                                    memberResponse.getId(),
                                    ContributionType.MONTHLY,
                                    currentYear,
                                    currentMonth
                            );

                    // If no contribution found, send reminder
                    if (monthlyContribution.isEmpty()) {
                        emailService.sendMonthlyContributionReminder(
                                memberResponse.getEmail(),
                                memberResponse.getFirstName() + " " + memberResponse.getLastName(),
                                "₦10,000.00" // Default amount, could be customized per member
                        );
                        remindersSent++;
                        log.debug("Sent reminder to: {}", memberResponse.getEmail());
                    }
                } catch (Exception e) {
                    log.error("Error sending reminder to member {}: {}",
                            memberResponse.getMemberId(), e.getMessage());
                }
            }

            log.info("Completed monthly contribution reminder job. Sent {} reminders", remindersSent);

        } catch (Exception e) {
            log.error("Error in monthly contribution reminder job", e);
        }
    }

    /**
     * CLEAN UP OLD REPORTS
     *
     * Runs: Every Sunday at 2 AM
     * Purpose: Delete reports older than 90 days
     *
     * Cron: "0 0 2 * * SUN"
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void cleanupOldReports() {
        log.info("Starting report cleanup job at {}", LocalDateTime.now());

        try {
            // Delete reports older than 90 days
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
            int deletedCount = reportService.deleteOldReports(cutoffDate);

            log.info("Completed report cleanup job. Deleted {} old reports", deletedCount);
        } catch (Exception e) {
            log.error("Error in report cleanup job", e);
        }
    }

    /**
     * GENERATE MONTHLY ANALYTICS
     *
     * Runs: Last day of month at 11 PM
     * Purpose: Generate monthly analytics report
     *
     * Cron: "0 0 23 L * *" (L = last day of month)
     */
    @Scheduled(cron = "0 0 23 L * *")
    public void generateMonthlyAnalytics() {
        log.info("Starting monthly analytics generation at {}", LocalDateTime.now());

        try {
            // Generate comprehensive analytics for the current month
            var dashboardStats = analyticsService.getDashboardStatistics();
            log.info("Dashboard Statistics - Total Members: {}, Total Contributions: {}, Total Payments: {}",
                    dashboardStats.getTotalMembers(),
                    dashboardStats.getTotalContributions(),
                    dashboardStats.getTotalPayments());

            var contributionTrend = analyticsService.getContributionTrend(12);
            log.info("Contribution Trend - Generated for last 12 months");

            var memberDistribution = analyticsService.getMemberStatusDistribution();
            log.info("Member Status Distribution - Generated successfully");

            var paymentMethodStats = analyticsService.getContributionByPaymentMethod();
            log.info("Payment Method Statistics - Generated successfully");

            var topEmployers = analyticsService.getTopEmployers(10);
            log.info("Top Employers - Generated top 10 employers");

            log.info("Completed monthly analytics generation successfully");
        } catch (Exception e) {
            log.error("Error in monthly analytics generation", e);
        }
    }

    /**
     * SYNC PAYMENT STATUSES
     *
     * Runs: Every hour
     * Purpose: Check pending payments with gateway
     *
     * Fixed Rate: Every 3600000ms (1 hour)
     */
    @Scheduled(fixedRate = 3600000)
    public void syncPaymentStatuses() {
        log.debug("Syncing payment statuses at {}", LocalDateTime.now());

        try {
            // Find all pending payments
            List<Payment> pendingPayments = paymentRepository.findByStatus(PaymentStatus.PENDING);

            if (pendingPayments.isEmpty()) {
                log.debug("No pending payments to sync");
                return;
            }

            log.info("Found {} pending payments to sync", pendingPayments.size());
            int successfulSyncs = 0;
            int failedSyncs = 0;

            for (Payment payment : pendingPayments) {
                try {
                    // Only sync payments that are older than 5 minutes (to avoid immediate syncs)
                    if (payment.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(5))) {
                        // Verify payment status with gateway
                        paymentService.verifyPayment(payment.getReference());
                        successfulSyncs++;
                        log.debug("Successfully synced payment: {}", payment.getReference());
                    }
                } catch (Exception e) {
                    failedSyncs++;
                    log.error("Failed to sync payment {}: {}", payment.getReference(), e.getMessage());
                }
            }

            log.info("Payment sync completed. Successful: {}, Failed: {}", successfulSyncs, failedSyncs);
        } catch (Exception e) {
            log.error("Error syncing payment statuses", e);
        }
    }

    /**
     * ARCHIVE OLD AUDIT LOGS
     *
     * Runs: First day of month at 3 AM
     * Purpose: Archive logs older than 1 year
     */
    @Scheduled(cron = "0 0 3 1 * *")
    public void archiveOldAuditLogs() {
        log.info("Starting audit log archival at {}", LocalDateTime.now());

        try {
            // Archive logs older than 1 year
            LocalDateTime archiveDate = LocalDateTime.now().minusYears(1);

            // In a real production system, you would:
            // 1. Export logs to external storage (S3, archive database, etc.)
            // 2. Compress the exported data
            // 3. Delete from active database after successful export
            // 4. Keep metadata for compliance tracking

            // For now, we'll log the operation
            // This would be implemented with a proper archival service
            log.info("Audit log archival would archive logs older than {}", archiveDate);
            log.info("In production, logs would be exported to cold storage and removed from active database");

            // Log the archival operation itself
            auditService.logAction(
                    "SYSTEM",
                    "ARCHIVE",
                    "AuditLog",
                    "SCHEDULED_ARCHIVAL"
            );

            log.info("Completed audit log archival");
        } catch (Exception e) {
            log.error("Error archiving audit logs", e);
        }
    }
}

/**
 * CRON EXAMPLES:
 *
 * Every minute:
 * @Scheduled(cron = "0 * * * * *")
 *
 * Every hour:
 * @Scheduled(cron = "0 0 * * * *")
 *
 * Every day at midnight:
 * @Scheduled(cron = "0 0 0 * * *")
 *
 * Every Monday at 9 AM:
 * @Scheduled(cron = "0 0 9 * * MON")
 *
 * First day of month:
 * @Scheduled(cron = "0 0 9 1 * *")
 *
 * Last day of month:
 * @Scheduled(cron = "0 0 9 L * *")
 *
 * FIXED RATE vs FIXED DELAY:
 *
 * Fixed Rate (runs every X ms):
 * @Scheduled(fixedRate = 60000) // Every 60 seconds
 *
 * Fixed Delay (waits X ms after completion):
 * @Scheduled(fixedDelay = 60000) // 60s after previous execution completes
 *
 * ENABLE IN APPLICATION:
 *
 * @SpringBootApplication
 * @EnableScheduling  // Add this
 * public class Application {
 *     public static void main(String[] args) {
 *         SpringApplication.run(Application.class, args);
 *     }
 * }
 */
