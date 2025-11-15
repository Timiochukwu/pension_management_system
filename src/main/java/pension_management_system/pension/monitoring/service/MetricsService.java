package pension_management_system.pension.monitoring.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pension_management_system.pension.contribution.repository.ContributionRepository;
import pension_management_system.pension.member.repository.MemberRepository;
import pension_management_system.pension.payment.repository.PaymentRepository;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * MetricsService - Custom business metrics for Prometheus
 *
 * Purpose: Track critical business KPIs in production
 *
 * Metrics categories:
 * 1. Business metrics (members, contributions, payments)
 * 2. Performance metrics (API response times)
 * 3. Error metrics (failures, fraud detections)
 * 4. System metrics (cache hits, DB queries)
 *
 * Why metrics matter:
 * - Detect issues before users complain
 * - Understand usage patterns
 * - Capacity planning
 * - SLA monitoring
 *
 * Prometheus scrapes /actuator/prometheus every 15s
 */
@Service
@Slf4j
public class MetricsService {

    private final MeterRegistry meterRegistry;
    private final MemberRepository memberRepository;
    private final ContributionRepository contributionRepository;
    private final PaymentRepository paymentRepository;

    // Counters
    private final Counter memberRegistrations;
    private final Counter contributionCreations;
    private final Counter paymentSuccesses;
    private final Counter paymentFailures;
    private final Counter fraudDetections;
    private final Counter bvnVerifications;
    private final Counter webhookDeliveries;
    private final Counter apiErrors;

    // Timers
    private final Timer paymentProcessingTime;
    private final Timer contributionProcessingTime;
    private final Timer bvnVerificationTime;

    // Gauges (real-time values)
    private final AtomicInteger activeMembersCount = new AtomicInteger(0);
    private final AtomicInteger pendingPaymentsCount = new AtomicInteger(0);

    public MetricsService(
            MeterRegistry meterRegistry,
            MemberRepository memberRepository,
            ContributionRepository contributionRepository,
            PaymentRepository paymentRepository
    ) {
        this.meterRegistry = meterRegistry;
        this.memberRepository = memberRepository;
        this.contributionRepository = contributionRepository;
        this.paymentRepository = paymentRepository;

        // Initialize counters
        this.memberRegistrations = Counter.builder("pension.members.registered")
                .description("Total number of member registrations")
                .register(meterRegistry);

        this.contributionCreations = Counter.builder("pension.contributions.created")
                .description("Total number of contributions created")
                .register(meterRegistry);

        this.paymentSuccesses = Counter.builder("pension.payments.success")
                .description("Total number of successful payments")
                .tag("status", "success")
                .register(meterRegistry);

        this.paymentFailures = Counter.builder("pension.payments.failed")
                .description("Total number of failed payments")
                .tag("status", "failed")
                .register(meterRegistry);

        this.fraudDetections = Counter.builder("pension.fraud.detections")
                .description("Total number of fraud detections")
                .register(meterRegistry);

        this.bvnVerifications = Counter.builder("pension.bvn.verifications")
                .description("Total number of BVN verifications")
                .register(meterRegistry);

        this.webhookDeliveries = Counter.builder("pension.webhooks.delivered")
                .description("Total number of webhook deliveries")
                .register(meterRegistry);

        this.apiErrors = Counter.builder("pension.api.errors")
                .description("Total number of API errors")
                .register(meterRegistry);

        // Initialize timers
        this.paymentProcessingTime = Timer.builder("pension.payment.processing.time")
                .description("Payment processing duration")
                .register(meterRegistry);

        this.contributionProcessingTime = Timer.builder("pension.contribution.processing.time")
                .description("Contribution processing duration")
                .register(meterRegistry);

        this.bvnVerificationTime = Timer.builder("pension.bvn.verification.time")
                .description("BVN verification duration")
                .register(meterRegistry);

        // Initialize gauges
        Gauge.builder("pension.members.active", activeMembersCount, AtomicInteger::get)
                .description("Number of active members")
                .register(meterRegistry);

        Gauge.builder("pension.payments.pending", pendingPaymentsCount, AtomicInteger::get)
                .description("Number of pending payments")
                .register(meterRegistry);

        // Database-backed gauges (fetched on-demand)
        Gauge.builder("pension.members.total", memberRepository, repo -> repo.count())
                .description("Total number of members")
                .register(meterRegistry);

        Gauge.builder("pension.contributions.total", contributionRepository, repo -> repo.count())
                .description("Total number of contributions")
                .register(meterRegistry);

        Gauge.builder("pension.payments.total", paymentRepository, repo -> repo.count())
                .description("Total number of payments")
                .register(meterRegistry);
    }

    // Counter increments
    public void recordMemberRegistration() {
        memberRegistrations.increment();
    }

    public void recordContributionCreation() {
        contributionCreations.increment();
    }

    public void recordPaymentSuccess() {
        paymentSuccesses.increment();
    }

    public void recordPaymentFailure() {
        paymentFailures.increment();
    }

    public void recordFraudDetection() {
        fraudDetections.increment();
    }

    public void recordBvnVerification() {
        bvnVerifications.increment();
    }

    public void recordWebhookDelivery() {
        webhookDeliveries.increment();
    }

    public void recordApiError() {
        apiErrors.increment();
    }

    // Timer recordings
    public Timer.Sample startPaymentTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordPaymentTime(Timer.Sample sample) {
        sample.stop(paymentProcessingTime);
    }

    public Timer.Sample startContributionTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordContributionTime(Timer.Sample sample) {
        sample.stop(contributionProcessingTime);
    }

    public Timer.Sample startBvnVerificationTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordBvnVerificationTime(Timer.Sample sample) {
        sample.stop(bvnVerificationTime);
    }

    // Gauge updates
    public void updateActiveMembersCount(int count) {
        activeMembersCount.set(count);
    }

    public void updatePendingPaymentsCount(int count) {
        pendingPaymentsCount.set(count);
    }

    /**
     * Record custom metric
     */
    public void recordCustomMetric(String name, String description, double value) {
        Gauge.builder(name, () -> value)
                .description(description)
                .register(meterRegistry);
    }
}
