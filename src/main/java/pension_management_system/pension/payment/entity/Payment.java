package pension_management_system.pension.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import pension_management_system.pension.contribution.entity.Contribution;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment entity representing a payment transaction
 */
@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribution_id", nullable = false)
    private Contribution contribution;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentGateway gateway;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false, unique = true)
    private String reference;

    @Column(name = "gateway_reference")
    private String gatewayReference;

    @Column(name = "authorization_url", length = 500)
    private String authorizationUrl;

    @Column(name = "callback_url")
    private String callbackUrl;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Mark payment as successful
     */
    public void markAsSuccessful(String gatewayResponse) {
        this.status = PaymentStatus.SUCCESS;
        this.gatewayResponse = gatewayResponse;
        this.verifiedAt = LocalDateTime.now();
    }

    /**
     * Mark payment as failed
     */
    public void markAsFailed(String reason, String gatewayResponse) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.gatewayResponse = gatewayResponse;
    }
}
