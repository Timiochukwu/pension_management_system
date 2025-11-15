package pension_management_system.pension.webhook.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Webhook - Webhook registration entity
 *
 * Purpose: Store webhook subscriptions from external systems
 *
 * Webhooks allow external systems to receive real-time notifications:
 * - Member registered
 * - Contribution created
 * - Payment successful
 * - Benefit approved
 *
 * Enterprise feature required by integration partners
 */
@Entity
@Table(name = "webhooks", indexes = {
        @Index(name = "idx_webhook_url", columnList = "url"),
        @Index(name = "idx_webhook_active", columnList = "active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Webhook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String url; // Endpoint to send events

    @Column(nullable = false, length = 100)
    private String secret; // For HMAC signature verification

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "webhook_events", joinColumns = @JoinColumn(name = "webhook_id"))
    @Column(name = "event_type")
    @Builder.Default
    private List<String> events = new ArrayList<>(); // Events to subscribe to

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(length = 500)
    private String description;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 3; // Number of retries on failure

    @Column(name = "timeout_seconds")
    @Builder.Default
    private Integer timeoutSeconds = 30;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_triggered_at")
    private LocalDateTime lastTriggeredAt;

    @Column(name = "failure_count")
    @Builder.Default
    private Integer failureCount = 0;
}
