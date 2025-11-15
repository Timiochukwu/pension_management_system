package pension_management_system.pension.webhook.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * WebhookDelivery - Webhook delivery attempt log
 *
 * Purpose: Track delivery attempts for audit and debugging
 *
 * Records:
 * - When webhook was sent
 * - Response status
 * - Response body
 * - Error messages
 * - Retry attempts
 */
@Entity
@Table(name = "webhook_deliveries", indexes = {
        @Index(name = "idx_delivery_webhook", columnList = "webhook_id"),
        @Index(name = "idx_delivery_status", columnList = "status"),
        @Index(name = "idx_delivery_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_id", nullable = false)
    private Webhook webhook;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "attempt_count")
    @Builder.Default
    private Integer attemptCount = 1;

    @Column(name = "duration_ms")
    private Long durationMs;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    public enum DeliveryStatus {
        SUCCESS,     // 2xx response
        FAILED,      // 4xx/5xx response
        RETRYING,    // Temporary failure, will retry
        CANCELLED    // Max retries exceeded
    }
}
