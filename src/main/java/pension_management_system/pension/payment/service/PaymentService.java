package pension_management_system.pension.payment.service;

import pension_management_system.pension.payment.dto.InitializePaymentRequest;
import pension_management_system.pension.payment.dto.PaymentResponse;

/**
 * PaymentService - Interface for payment operations
 *
 * Purpose: Define contract for payment processing
 *
 * Main operations:
 * - Initialize payment with gateway
 * - Verify payment status
 * - Handle webhook notifications
 * - Process refunds
 */
public interface PaymentService {

    /**
     * Initialize a new payment
     *
     * Steps:
     * 1. Validate contribution exists and is unpaid
     * 2. Create payment record
     * 3. Call payment gateway API to initialize
     * 4. Store gateway reference and authorization URL
     * 5. Return payment details with authorization URL
     *
     * @param request Payment initialization details
     * @return Payment response with authorization URL
     */
    PaymentResponse initializePayment(InitializePaymentRequest request);

    /**
     * Verify payment status
     *
     * Steps:
     * 1. Find payment by reference
     * 2. Call gateway verify API
     * 3. Update payment status
     * 4. Update contribution if payment successful
     * 5. Return current payment status
     *
     * @param reference Our payment reference
     * @return Current payment details
     */
    PaymentResponse verifyPayment(String reference);

    /**
     * Handle webhook notification from gateway
     *
     * Webhooks are HTTP POST requests from payment gateway
     * Sent when payment status changes
     *
     * Steps:
     * 1. Validate webhook signature (security)
     * 2. Extract gateway reference
     * 3. Find payment record
     * 4. Verify payment with gateway API
     * 5. Update payment and contribution status
     *
     * @param webhookPayload Raw webhook JSON from gateway
     * @param signature Webhook signature for verification
     * @param gateway Which gateway sent the webhook
     */
    void handleWebhook(String webhookPayload, String signature, String gateway);

    /**
     * Get payment by reference
     *
     * @param reference Our payment reference
     * @return Payment details
     */
    PaymentResponse getPaymentByReference(String reference);
}
