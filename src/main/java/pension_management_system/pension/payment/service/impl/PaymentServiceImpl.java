package pension_management_system.pension.payment.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pension_management_system.pension.contribution.entity.Contribution;
import pension_management_system.pension.contribution.entity.ContributionStatus;
import pension_management_system.pension.contribution.repository.ContributionRepository;
import pension_management_system.pension.payment.dto.InitializePaymentRequest;
import pension_management_system.pension.payment.dto.PaymentResponse;
import pension_management_system.pension.payment.entity.Payment;
import pension_management_system.pension.payment.entity.PaymentGateway;
import pension_management_system.pension.payment.entity.PaymentStatus;
import pension_management_system.pension.payment.mapper.PaymentMapper;
import pension_management_system.pension.payment.repository.PaymentRepository;
import pension_management_system.pension.payment.service.PaymentService;
import pension_management_system.pension.payment.service.gateway.FlutterwaveService;
import pension_management_system.pension.payment.service.gateway.PaystackService;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * PaymentServiceImpl - Implementation of payment processing logic
 *
 * Purpose: Orchestrate payment flow with different gateways
 *
 * Responsibilities:
 * 1. Initialize payments with chosen gateway
 * 2. Verify payment status
 * 3. Update contribution status on success
 * 4. Handle webhook notifications
 * 5. Maintain payment audit trail
 *
 * Flow:
 * 1. User clicks "Pay" → initializePayment()
 * 2. User redirected to gateway → waits
 * 3. User completes payment → gateway sends webhook
 * 4. handleWebhook() → verifyPayment()
 * 5. Update payment & contribution status
 *
 * Annotations:
 * @Service - Spring service component
 * @Transactional - All methods run in database transaction
 * @RequiredArgsConstructor - Lombok constructor injection
 * @Slf4j - Logging
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    /**
     * DEPENDENCIES
     *
     * Repositories for database operations
     * Mappers for DTO conversion
     * Gateway services for API calls
     */
    private final PaymentRepository paymentRepository;
    private final ContributionRepository contributionRepository;
    private final PaymentMapper paymentMapper;
    private final PaystackService paystackService;
    private final FlutterwaveService flutterwaveService;
    private final ObjectMapper objectMapper;

    /**
     * INITIALIZE PAYMENT
     *
     * Step 1: Start payment process
     *
     * What it does:
     * 1. Validate contribution exists and is unpaid
     * 2. Create payment record (status: INITIATED)
     * 3. Call gateway API to initialize
     * 4. Update payment with gateway details
     * 5. Return authorization URL for redirect
     *
     * Error handling:
     * - Contribution not found → Exception
     * - Contribution already paid → Exception
     * - Gateway API error → Exception
     * - Payment record saved even if gateway fails (for retry)
     *
     * @param request Payment initialization details
     * @return Payment response with authorization URL
     */
    @Override
    public PaymentResponse initializePayment(InitializePaymentRequest request) {
        log.info("Initializing payment for contribution: {}", request.getContributionId());

        // STEP 1: Validate contribution exists
        Contribution contribution = contributionRepository
                .findById(request.getContributionId())
                .orElseThrow(() -> new RuntimeException(
                        "Contribution not found: " + request.getContributionId()
                ));

        // STEP 2: Check contribution is not already paid
        if (contribution.getStatus() == ContributionStatus.COMPLETED) {
            throw new RuntimeException("Contribution already paid");
        }

        // STEP 3: Check amount matches
        if (!contribution.getContributionAmount().equals(request.getAmount())) {
            throw new RuntimeException("Payment amount does not match contribution amount");
        }

        // STEP 4: Create payment record
        Payment payment = Payment.builder()
                .contribution(contribution)
                .amount(request.getAmount())
                .gateway(request.getGateway())
                .status(PaymentStatus.INITIATED)
                .reference(generatePaymentReference())
                .callbackUrl(request.getCallbackUrl())
                .metadata(request.getMetadata())
                .build();

        // Save initial payment record
        payment = paymentRepository.save(payment);
        log.info("Payment record created: {}", payment.getReference());

        try {
            // STEP 5: Initialize with gateway
            if (request.getGateway() == PaymentGateway.PAYSTACK) {
                initializePaystack(payment, request);
            } else if (request.getGateway() == PaymentGateway.FLUTTERWAVE) {
                initializeFlutterwave(payment, request);
            } else {
                throw new RuntimeException("Unsupported payment gateway: " + request.getGateway());
            }

            // STEP 6: Update status to PENDING
            payment.setStatus(PaymentStatus.PENDING);
            payment = paymentRepository.save(payment);

            log.info("Payment initialized successfully: {}", payment.getReference());

        } catch (Exception e) {
            // Gateway initialization failed
            payment.markAsFailed("Gateway initialization failed: " + e.getMessage(), null);
            paymentRepository.save(payment);
            throw e;
        }

        // STEP 7: Return response
        return paymentMapper.toResponse(payment);
    }

    /**
     * INITIALIZE WITH PAYSTACK
     *
     * Private method to handle Paystack-specific initialization
     *
     * @param payment Payment entity to update
     * @param request Original request
     */
    private void initializePaystack(Payment payment, InitializePaymentRequest request) {
        log.info("Initializing with Paystack");

        // Call Paystack API
        Map<String, Object> response = paystackService.initializeTransaction(request);

        // Extract data from response
        String authorizationUrl = paystackService.getAuthorizationUrl(response);
        String gatewayReference = payment.getReference(); // We sent our reference

        // Update payment
        payment.setAuthorizationUrl(authorizationUrl);
        payment.setGatewayReference(gatewayReference);
        payment.setGatewayResponse(convertToJson(response));

        log.info("Paystack initialization successful. Auth URL: {}", authorizationUrl);
    }

    /**
     * INITIALIZE WITH FLUTTERWAVE
     *
     * Private method to handle Flutterwave-specific initialization
     *
     * @param payment Payment entity to update
     * @param request Original request
     */
    private void initializeFlutterwave(Payment payment, InitializePaymentRequest request) {
        log.info("Initializing with Flutterwave");

        // Call Flutterwave API
        Map<String, Object> response = flutterwaveService.initializePayment(request);

        // Extract data from response
        String paymentLink = flutterwaveService.getPaymentLink(response);

        // Update payment
        payment.setAuthorizationUrl(paymentLink);
        payment.setGatewayReference(payment.getReference()); // We sent our reference as tx_ref
        payment.setGatewayResponse(convertToJson(response));

        log.info("Flutterwave initialization successful. Payment link: {}", paymentLink);
    }

    /**
     * VERIFY PAYMENT
     *
     * Step 3: Confirm payment was successful
     *
     * What it does:
     * 1. Find payment by reference
     * 2. Call gateway verify API
     * 3. Update payment status based on result
     * 4. If successful → Update contribution status
     * 5. Return current payment status
     *
     * When called:
     * - After user returns from gateway
     * - In webhook handler
     * - By admin to check status
     *
     * @param reference Our payment reference
     * @return Current payment details
     */
    @Override
    public PaymentResponse verifyPayment(String reference) {
        log.info("Verifying payment: {}", reference);

        // STEP 1: Find payment
        Payment payment = paymentRepository.findByReference(reference)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + reference));

        // STEP 2: Check if already verified
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.info("Payment already verified as successful");
            return paymentMapper.toResponse(payment);
        }

        // STEP 3: Update status to PROCESSING
        payment.setStatus(PaymentStatus.PROCESSING);
        payment = paymentRepository.save(payment);

        try {
            // STEP 4: Verify with gateway
            boolean isSuccessful = false;
            String gatewayResponse = null;

            if (payment.getGateway() == PaymentGateway.PAYSTACK) {
                Map<String, Object> response = paystackService.verifyTransaction(reference);
                isSuccessful = paystackService.isTransactionSuccessful(response);
                gatewayResponse = convertToJson(response);

            } else if (payment.getGateway() == PaymentGateway.FLUTTERWAVE) {
                Map<String, Object> response = flutterwaveService.verifyTransactionByReference(reference);
                isSuccessful = flutterwaveService.isTransactionSuccessful(response);
                gatewayResponse = convertToJson(response);
            }

            // STEP 5: Update payment based on result
            if (isSuccessful) {
                payment.markAsSuccessful(gatewayResponse);
                log.info("Payment verified as successful: {}", reference);

                // STEP 6: Update contribution status
                Contribution contribution = payment.getContribution();
                contribution.setStatus(ContributionStatus.COMPLETED);
                contribution.setPaymentReference(reference);
                contributionRepository.save(contribution);

                log.info("Contribution marked as completed: {}", contribution.getId());

            } else {
                payment.markAsFailed("Payment was not successful", gatewayResponse);
                log.warn("Payment verification failed: {}", reference);
            }

            payment = paymentRepository.save(payment);

        } catch (Exception e) {
            log.error("Error verifying payment: {}", e.getMessage(), e);
            payment.markAsFailed("Verification error: " + e.getMessage(), null);
            payment = paymentRepository.save(payment);
            throw new RuntimeException("Payment verification failed", e);
        }

        return paymentMapper.toResponse(payment);
    }

    /**
     * HANDLE WEBHOOK
     *
     * Process notification from payment gateway
     *
     * What it does:
     * 1. Verify webhook signature (security)
     * 2. Extract payment reference
     * 3. Call verifyPayment() to update status
     *
     * Security:
     * - MUST verify signature first
     * - Reject if signature invalid
     * - Prevent fraud/spoofing
     *
     * Why webhooks?
     * - Real-time notifications
     * - Don't rely on user returning to callback
     * - More reliable than polling
     *
     * @param webhookPayload Raw JSON from gateway
     * @param signature Signature from webhook header
     * @param gateway Which gateway sent webhook
     */
    @Override
    public void handleWebhook(String webhookPayload, String signature, String gateway) {
        log.info("Handling webhook from: {}", gateway);

        try {
            // STEP 1: Verify signature
            boolean isValid = false;

            if ("paystack".equalsIgnoreCase(gateway)) {
                isValid = paystackService.verifyWebhookSignature(webhookPayload, signature);
            } else if ("flutterwave".equalsIgnoreCase(gateway)) {
                isValid = flutterwaveService.verifyWebhook(signature);
            }

            if (!isValid) {
                log.error("Invalid webhook signature from: {}", gateway);
                throw new SecurityException("Invalid webhook signature");
            }

            // STEP 2: Parse webhook payload
            Map<String, Object> payload = objectMapper.readValue(webhookPayload, Map.class);

            // STEP 3: Extract reference
            String reference = extractReferenceFromWebhook(payload, gateway);

            if (reference == null) {
                log.error("Could not extract reference from webhook");
                return;
            }

            // STEP 4: Verify payment
            log.info("Webhook triggered verification for: {}", reference);
            verifyPayment(reference);

        } catch (Exception e) {
            log.error("Error handling webhook: {}", e.getMessage(), e);
            // Don't throw - webhooks should be fault-tolerant
        }
    }

    /**
     * GET PAYMENT BY REFERENCE
     *
     * Retrieve payment details
     *
     * @param reference Payment reference
     * @return Payment details
     */
    @Override
    public PaymentResponse getPaymentByReference(String reference) {
        log.info("Fetching payment: {}", reference);

        Payment payment = paymentRepository.findByReference(reference)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + reference));

        return paymentMapper.toResponse(payment);
    }

    /**
     * HELPER: Generate unique payment reference
     *
     * Format: PMT-{timestamp}-{random}
     * Example: PMT-1705318245000-AB12CD34
     *
     * @return Unique reference
     */
    private String generatePaymentReference() {
        long timestamp = System.currentTimeMillis();
        String random = java.util.UUID.randomUUID().toString()
                .substring(0, 8)
                .toUpperCase();
        return "PMT-" + timestamp + "-" + random;
    }

    /**
     * HELPER: Convert object to JSON string
     *
     * @param object Object to convert
     * @return JSON string
     */
    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("Error converting to JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * HELPER: Extract reference from webhook payload
     *
     * Different gateways structure webhooks differently
     *
     * @param payload Webhook JSON
     * @param gateway Gateway name
     * @return Payment reference
     */
    private String extractReferenceFromWebhook(Map<String, Object> payload, String gateway) {
        try {
            if ("paystack".equalsIgnoreCase(gateway)) {
                // Paystack: data.reference
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                return (String) data.get("reference");

            } else if ("flutterwave".equalsIgnoreCase(gateway)) {
                // Flutterwave: data.tx_ref
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                return (String) data.get("tx_ref");
            }

            return null;
        } catch (Exception e) {
            log.error("Error extracting reference: {}", e.getMessage());
            return null;
        }
    }
}
