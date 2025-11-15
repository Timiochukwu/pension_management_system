package pension_management_system.pension.payment.entity;

/**
 * PaymentStatus Enum - Status of a payment transaction
 *
 * Purpose: Track the lifecycle of a payment from initiation to completion
 *
 * What is Payment Status?
 * - Current state of a payment transaction
 * - Changes as payment progresses
 * - Used for:
 *   - Showing user what's happening
 *   - Determining next actions
 *   - Reconciliation and reporting
 *   - Handling failures and retries
 *
 * Payment Lifecycle:
 * 1. INITIATED → Payment request created
 * 2. PENDING → Waiting for user to pay
 * 3. PROCESSING → Payment being verified
 * 4. SUCCESS → Payment completed ✓
 * OR
 * 4. FAILED → Payment failed ✗
 * OR
 * 4. CANCELLED → User cancelled
 * OR
 * 4. EXPIRED → Payment link expired
 *
 * Status Transitions:
 * INITIATED → PENDING → PROCESSING → SUCCESS
 *                    ↘ FAILED
 *                    ↘ CANCELLED
 *                    ↘ EXPIRED
 */
public enum PaymentStatus {

    /**
     * INITIATED - Payment process started
     *
     * What it means:
     * - Payment request created in our system
     * - Gateway transaction not yet initialized
     * - User hasn't been redirected to payment page yet
     *
     * When it happens:
     * - User clicks "Make Payment" button
     * - System creates payment record
     * - Before calling payment gateway API
     *
     * Next status:
     * - PENDING (after gateway initialization)
     * - FAILED (if gateway initialization fails)
     *
     * Duration: Milliseconds to seconds
     *
     * User sees:
     * - Loading spinner
     * - "Initializing payment..."
     *
     * Example:
     * Payment payment = Payment.builder()
     *     .status(PaymentStatus.INITIATED)
     *     .build();
     * paymentRepository.save(payment);
     * // Then initialize with gateway
     */
    INITIATED,

    /**
     * PENDING - Waiting for user to complete payment
     *
     * What it means:
     * - Payment gateway initialized successfully
     * - Payment link/page ready
     * - User redirected to payment page
     * - Waiting for user to pay
     *
     * When it happens:
     * - After successful gateway initialization
     * - User on payment gateway's checkout page
     * - Payment not yet submitted
     *
     * Next status:
     * - PROCESSING (user submitted payment)
     * - CANCELLED (user clicked cancel)
     * - EXPIRED (timeout reached)
     *
     * Duration: Minutes to hours
     *
     * User sees:
     * - Payment form (on Paystack/Flutterwave)
     * - Amount to pay
     * - Payment options (card, bank, etc.)
     *
     * System actions:
     * - Monitor for webhook
     * - Allow user to retry if they close page
     * - Auto-expire after timeout (e.g., 30 minutes)
     *
     * Example:
     * payment.setStatus(PaymentStatus.PENDING);
     * payment.setGatewayReference("ref_123456");
     * payment.setAuthorizationUrl("https://checkout.paystack.com/...");
     * paymentRepository.save(payment);
     */
    PENDING,

    /**
     * PROCESSING - Payment submitted, being verified
     *
     * What it means:
     * - User completed payment on gateway
     * - Gateway is processing/verifying
     * - Waiting for final confirmation
     * - Our system verifying with gateway API
     *
     * When it happens:
     * - User clicked "Pay" on gateway page
     * - Webhook received from gateway
     * - We're calling verify endpoint
     *
     * Next status:
     * - SUCCESS (verification confirms payment)
     * - FAILED (verification shows failure)
     *
     * Duration: Seconds to minutes
     *
     * User sees:
     * - "Processing payment..."
     * - Spinner or progress indicator
     * - "Please wait, verifying your payment"
     *
     * System actions:
     * - Call gateway verify API
     * - Check payment details
     * - Update contribution status if successful
     *
     * Example:
     * // Webhook received
     * payment.setStatus(PaymentStatus.PROCESSING);
     * paymentRepository.save(payment);
     *
     * // Verify with gateway
     * boolean verified = paystackService.verifyPayment(reference);
     * if (verified) {
     *     payment.setStatus(PaymentStatus.SUCCESS);
     * }
     */
    PROCESSING,

    /**
     * SUCCESS - Payment completed successfully ✓
     *
     * What it means:
     * - Payment verified and confirmed
     * - Money received by payment gateway
     * - Contribution marked as paid
     * - Transaction complete
     *
     * When it happens:
     * - Gateway verification returns success
     * - Webhook confirms payment
     * - All checks passed
     *
     * Next status:
     * - None (final status)
     * - REFUNDED (if later refunded)
     *
     * Duration: Permanent
     *
     * User sees:
     * - Success message ✓
     * - "Payment successful!"
     * - Receipt/confirmation
     * - Updated account balance
     *
     * System actions:
     * - Update contribution status to COMPLETED
     * - Send confirmation email/SMS
     * - Generate receipt
     * - Update member balance
     * - Record in accounting system
     *
     * Example:
     * payment.setStatus(PaymentStatus.SUCCESS);
     * payment.setPaidAt(LocalDateTime.now());
     * payment.setGatewayResponse(verificationResponse);
     * paymentRepository.save(payment);
     *
     * // Update contribution
     * contribution.setStatus(ContributionStatus.COMPLETED);
     * contribution.setPaymentReference(payment.getReference());
     * contributionRepository.save(contribution);
     *
     * // Send notifications
     * emailService.sendPaymentConfirmation(member);
     */
    SUCCESS,

    /**
     * FAILED - Payment failed ✗
     *
     * What it means:
     * - Payment could not be completed
     * - Transaction rejected or declined
     * - No money transferred
     *
     * Common reasons:
     * - Insufficient funds
     * - Card declined
     * - Invalid card details
     * - Bank rejection
     * - Network timeout
     * - Gateway error
     *
     * When it happens:
     * - Gateway returns failure status
     * - Verification shows failed transaction
     * - Payment timeout exceeded
     *
     * Next status:
     * - INITIATED (if user retries)
     * - None (if user gives up)
     *
     * User sees:
     * - Error message ✗
     * - "Payment failed"
     * - Reason for failure
     * - "Try again" button
     *
     * System actions:
     * - Keep contribution as PENDING
     * - Log failure reason
     * - Allow retry
     * - Notify admin if many failures
     *
     * Example:
     * payment.setStatus(PaymentStatus.FAILED);
     * payment.setFailureReason("Insufficient funds");
     * payment.setGatewayResponse(errorResponse);
     * paymentRepository.save(payment);
     *
     * // Contribution remains PENDING for retry
     * contribution.setStatus(ContributionStatus.PENDING);
     */
    FAILED,

    /**
     * CANCELLED - User cancelled payment
     *
     * What it means:
     * - User chose to cancel
     * - Intentionally did not complete payment
     * - No payment attempt made
     *
     * When it happens:
     * - User clicked "Cancel" on payment page
     * - User closed payment window
     * - User navigated away
     *
     * Next status:
     * - INITIATED (if user retries)
     * - None (if user doesn't retry)
     *
     * User sees:
     * - "Payment cancelled"
     * - Can retry if they want
     *
     * System actions:
     * - Keep contribution as PENDING
     * - Allow user to retry
     * - No alerts or errors
     *
     * Example:
     * payment.setStatus(PaymentStatus.CANCELLED);
     * payment.setCancelledAt(LocalDateTime.now());
     * paymentRepository.save(payment);
     */
    CANCELLED,

    /**
     * EXPIRED - Payment link/session expired
     *
     * What it means:
     * - Payment window timed out
     * - User took too long to pay
     * - Payment link no longer valid
     *
     * When it happens:
     * - 30 minutes (or configured timeout) passed
     * - User didn't complete payment in time
     * - Gateway session expired
     *
     * Next status:
     * - INITIATED (if user requests new link)
     *
     * User sees:
     * - "Payment link expired"
     * - "Please request a new payment link"
     * - Button to retry
     *
     * System actions:
     * - Mark payment as expired
     * - Allow generating new payment link
     * - Clean up old payment records
     *
     * Example:
     * // Scheduled job checks for expired payments
     * List<Payment> expiredPayments = paymentRepository.findByStatusAndCreatedAtBefore(
     *     PaymentStatus.PENDING,
     *     LocalDateTime.now().minusMinutes(30)
     * );
     *
     * for (Payment payment : expiredPayments) {
     *     payment.setStatus(PaymentStatus.EXPIRED);
     *     paymentRepository.save(payment);
     * }
     */
    EXPIRED,

    /**
     * REFUNDED - Payment was refunded
     *
     * What it means:
     * - Money returned to user
     * - Transaction reversed
     * - Originally was SUCCESS
     *
     * When it happens:
     * - Admin initiates refund
     * - User requests refund (within policy)
     * - Duplicate payment
     * - Error correction
     *
     * Process:
     * 1. Admin/system initiates refund
     * 2. Call gateway refund API
     * 3. Gateway processes refund
     * 4. Update status to REFUNDED
     * 5. Reverse contribution if needed
     *
     * User sees:
     * - "Payment refunded"
     * - Amount returned
     * - Reason for refund
     *
     * System actions:
     * - Update payment status
     * - Reverse contribution
     * - Update member balance
     * - Send refund notification
     *
     * Example:
     * // Initiate refund
     * boolean refunded = paystackService.refundPayment(payment.getReference());
     * if (refunded) {
     *     payment.setStatus(PaymentStatus.REFUNDED);
     *     payment.setRefundedAt(LocalDateTime.now());
     *     paymentRepository.save(payment);
     *
     *     // Reverse contribution
     *     contribution.setStatus(ContributionStatus.REVERSED);
     *     contributionRepository.save(contribution);
     * }
     */
    REFUNDED
}
