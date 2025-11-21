package pension_management_system.pension.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pension_management_system.pension.payment.entity.PaymentGateway;
import pension_management_system.pension.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for payment operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long contributionId;
    private BigDecimal amount;
    private PaymentGateway gateway;
    private PaymentStatus status;
    private String reference;
    private String gatewayReference;
    private String authorizationUrl;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;

    /**
     * Check if payment is completed
     */
    public boolean isCompleted() {
        return status == PaymentStatus.SUCCESS;
    }
}
