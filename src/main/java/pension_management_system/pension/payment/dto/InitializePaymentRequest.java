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
 * InitializePaymentRequest DTO - Request to start a payment
 *
 * Purpose: Data needed to initialize payment with gateway
 *
 * Example JSON:
 * {
 *   "contributionId": 123,
 *   "amount": 10000.00,
 *   "gateway": "PAYSTACK",
 *   "email": "member@example.com",
 *   "callbackUrl": "https://pension.com/payment/callback"
 * }
 *
 * Flow:
 * 1. User clicks "Pay Now" for contribution
 * 2. Frontend sends this request to API
 * 3. Backend initializes payment with chosen gateway
 * 4. Returns authorization URL
 * 5. User redirected to payment page
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitializePaymentRequest {

    /**
     * CONTRIBUTION ID
     *
     * Which contribution is being paid
     *
     * Validation: Cannot be null
     */
    @NotNull(message = "Contribution ID is required")
    private Long contributionId;

    /**
     * PAYMENT AMOUNT
     *
     * How much to pay (in NGN or other currency)
     *
     * Validation:
     * - Cannot be null
     * - Must be greater than 0
     */
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    /**
     * PAYMENT GATEWAY
     *
     * Which payment processor to use
     * Values: PAYSTACK, FLUTTERWAVE
     */
    @NotNull(message = "Payment gateway is required")
    private PaymentGateway gateway;

    /**
     * MEMBER EMAIL
     *
     * Email for payment notifications
     * Required by payment gateways
     */
    @NotNull(message = "Email is required")
    private String email;

    /**
     * CALLBACK URL (OPTIONAL)
     *
     * Where to redirect after payment
     * If not provided, use default
     */
    private String callbackUrl;

    /**
     * METADATA (OPTIONAL)
     *
     * Additional context to store with payment
     * Example: {"source": "mobile_app", "campaign": "bonus_2025"}
     */
    private String metadata;
}
