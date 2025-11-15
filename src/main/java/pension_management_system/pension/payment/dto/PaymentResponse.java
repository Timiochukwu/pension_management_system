package pension_management_system.pension.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pension_management_system.pension.payment.entity.PaymentGateway;
import pension_management_system.pension.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PaymentResponse DTO - Payment details returned to client
 *
 * Purpose: Send payment information back to frontend/API consumer
 *
 * Example JSON Response (after initialization):
 * {
 *   "id": 1,
 *   "reference": "PMT-20250115-ABC123",
 *   "contributionId": 123,
 *   "amount": 10000.00,
 *   "gateway": "PAYSTACK",
 *   "gatewayReference": "ref_1234567890",
 *   "status": "PENDING",
 *   "authorizationUrl": "https://checkout.paystack.com/abc123",
 *   "createdAt": "2025-01-15T10:30:00"
 * }
 *
 * Client usage:
 * // Redirect user to authorization URL
 * window.location.href = response.authorizationUrl;
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    /**
     * Payment ID
     */
    private Long id;

    /**
     * Our payment reference
     * Example: "PMT-20250115-ABC123"
     */
    private String reference;

    /**
     * Contribution being paid
     */
    private Long contributionId;

    /**
     * Payment amount
     */
    private BigDecimal amount;

    /**
     * Payment gateway used
     * Values: PAYSTACK, FLUTTERWAVE, MANUAL
     */
    private PaymentGateway gateway;

    /**
     * Gateway's reference
     * Example: "ref_1234567890"
     */
    private String gatewayReference;

    /**
     * Current payment status
     * Values: INITIATED, PENDING, PROCESSING, SUCCESS, FAILED, etc.
     */
    private PaymentStatus status;

    /**
     * URL to redirect user for payment
     * Only present for gateway payments
     */
    private String authorizationUrl;

    /**
     * Payment channel used (if completed)
     * Examples: "card", "bank", "ussd"
     */
    private String paymentChannel;

    /**
     * Failure reason (if failed)
     */
    private String failureReason;

    /**
     * When payment record was created
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * When payment was completed (if successful)
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime paidAt;

    /**
     * HELPER: Check if payment is complete
     */
    public boolean isCompleted() {
        return status == PaymentStatus.SUCCESS;
    }

    /**
     * HELPER: Check if payment can be retried
     */
    public boolean canRetry() {
        return status == PaymentStatus.FAILED ||
               status == PaymentStatus.CANCELLED ||
               status == PaymentStatus.EXPIRED;
    }
}
