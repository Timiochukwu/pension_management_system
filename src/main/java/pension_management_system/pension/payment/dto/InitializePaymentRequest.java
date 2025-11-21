package pension_management_system.pension.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pension_management_system.pension.payment.entity.PaymentGateway;

import java.math.BigDecimal;

/**
 * Request DTO for initializing a payment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitializePaymentRequest {

    @NotNull(message = "Contribution ID is required")
    private Long contributionId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Gateway is required")
    private PaymentGateway gateway;

    private String email;
    private String callbackUrl;
    private String metadata;
}
