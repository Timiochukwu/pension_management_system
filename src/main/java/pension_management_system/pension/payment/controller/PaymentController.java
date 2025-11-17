package pension_management_system.pension.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pension_management_system.pension.common.dto.ApiResponseDto;
import pension_management_system.pension.payment.dto.InitializePaymentRequest;
import pension_management_system.pension.payment.dto.PaymentResponse;
import pension_management_system.pension.payment.service.PaymentService;

/**
 * PaymentController - REST API for payment operations
 *
 * Purpose: Expose payment functionality through HTTP endpoints
 *
 * Base URL: /api/v1/payments
 *
 * Endpoints:
 * - POST /initialize - Start new payment
 * - GET /{reference} - Get payment details
 * - GET /verify/{reference} - Verify payment status
 * - POST /webhook/paystack - Paystack webhook handler
 * - POST /webhook/flutterwave - Flutterwave webhook handler
 * - GET /callback - Payment callback handler
 *
 * Testing with cURL:
 * # Initialize payment
 * curl -X POST http://localhost:1110/api/v1/payments/initialize \
 *   -H "Content-Type: application/json" \
 *   -d '{
 *     "contributionId": 1,
 *     "amount": 10000,
 *     "gateway": "PAYSTACK",
 *     "email": "member@example.com",
 *     "callbackUrl": "http://localhost:1110/api/v1/payments/callback"
 *   }'
 *
 * # Verify payment
 * curl http://localhost:1110/api/v1/payments/verify/PMT-123456-ABC
 *
 * Annotations:
 * @RestController - REST API controller
 * @RequestMapping - Base URL for all endpoints
 * @Tag - Swagger/OpenAPI documentation grouping
 * @RequiredArgsConstructor - Lombok constructor injection
 * @Slf4j - Logging
 * @ConditionalOnProperty - Only load if payment gateways are enabled
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment gateway integration APIs")
@ConditionalOnProperty(
    name = {"paystack.enabled", "flutterwave.enabled"},
    havingValue = "true",
    matchIfMissing = false
)
public class PaymentController {

    /**
     * DEPENDENCY INJECTION
     *
     * Spring automatically injects PaymentService implementation
     */
    private final PaymentService paymentService;

    /**
     * INITIALIZE PAYMENT
     *
     * HTTP Method: POST
     * URL: /api/v1/payments/initialize
     * Content-Type: application/json
     *
     * Request Body:
     * {
     *   "contributionId": 123,
     *   "amount": 10000.00,
     *   "gateway": "PAYSTACK",
     *   "email": "member@example.com",
     *   "callbackUrl": "https://yourapp.com/payment/callback"
     * }
     *
     * Success Response (HTTP 201):
     * {
     *   "success": true,
     *   "message": "Payment initialized successfully",
     *   "data": {
     *     "id": 1,
     *     "reference": "PMT-1705318245000-AB12CD34",
     *     "contributionId": 123,
     *     "amount": 10000.00,
     *     "gateway": "PAYSTACK",
     *     "status": "PENDING",
     *     "authorizationUrl": "https://checkout.paystack.com/abc123",
     *     "createdAt": "2025-01-15T10:30:45"
     *   }
     * }
     *
     * Error Response (HTTP 400):
     * {
     *   "success": false,
     *   "message": "Contribution not found: 123"
     * }
     *
     * Flow after this:
     * 1. Frontend gets authorizationUrl from response
     * 2. Redirects user to authorizationUrl
     * 3. User completes payment on gateway
     * 4. Gateway sends webhook
     * 5. Gateway redirects user to callbackUrl
     *
     * @param request Payment initialization details
     * @return Payment response with authorization URL
     */
    @PostMapping("/initialize")
    @Operation(
            summary = "Initialize payment",
            description = "Start a new payment process with selected gateway. " +
                    "Returns authorization URL to redirect user for payment."
    )
    public ResponseEntity<ApiResponseDto<PaymentResponse>> initializePayment(
            @Valid @RequestBody InitializePaymentRequest request) {

        log.info("POST /api/v1/payments/initialize - Gateway: {}, Amount: {}",
                request.getGateway(), request.getAmount());

        try {
            // Call service to initialize payment
            PaymentResponse response = paymentService.initializePayment(request);

            // Build success response
            ApiResponseDto<PaymentResponse> apiResponse = ApiResponseDto.<PaymentResponse>builder()
                    .success(true)
                    .message("Payment initialized successfully")
                    .data(response)
                    .build();

            // Return HTTP 201 CREATED
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);

        } catch (IllegalArgumentException e) {
            // Validation errors (bad request from client)
            log.error("Validation error: {}", e.getMessage());

            ApiResponseDto<PaymentResponse> apiResponse = ApiResponseDto.<PaymentResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);

        } catch (Exception e) {
            // Unexpected errors (server problems)
            log.error("Error initializing payment: {}", e.getMessage(), e);

            ApiResponseDto<PaymentResponse> apiResponse = ApiResponseDto.<PaymentResponse>builder()
                    .success(false)
                    .message("Failed to initialize payment: " + e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * GET PAYMENT BY REFERENCE
     *
     * HTTP Method: GET
     * URL: /api/v1/payments/{reference}
     *
     * Example: GET /api/v1/payments/PMT-1705318245000-AB12CD34
     *
     * Success Response (HTTP 200):
     * {
     *   "success": true,
     *   "message": "Payment retrieved successfully",
     *   "data": {
     *     "reference": "PMT-1705318245000-AB12CD34",
     *     "status": "SUCCESS",
     *     "amount": 10000.00,
     *     ...
     *   }
     * }
     *
     * Use case:
     * - Check payment status
     * - Display payment details to user
     * - Admin viewing payment records
     *
     * @param reference Payment reference
     * @return Payment details
     */
    @GetMapping("/{reference}")
    @Operation(
            summary = "Get payment details",
            description = "Retrieve payment information by reference"
    )
    public ResponseEntity<ApiResponseDto<PaymentResponse>> getPayment(
            @PathVariable String reference) {

        log.info("GET /api/v1/payments/{} - Get payment details", reference);

        try {
            PaymentResponse response = paymentService.getPaymentByReference(reference);

            ApiResponseDto<PaymentResponse> apiResponse = ApiResponseDto.<PaymentResponse>builder()
                    .success(true)
                    .message("Payment retrieved successfully")
                    .data(response)
                    .build();

            return ResponseEntity.ok(apiResponse);

        } catch (RuntimeException e) {
            log.error("Payment not found: {}", e.getMessage());

            ApiResponseDto<PaymentResponse> apiResponse = ApiResponseDto.<PaymentResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        }
    }

    /**
     * VERIFY PAYMENT
     *
     * HTTP Method: GET
     * URL: /api/v1/payments/verify/{reference}
     *
     * Example: GET /api/v1/payments/verify/PMT-1705318245000-AB12CD34
     *
     * What it does:
     * 1. Calls gateway verify API
     * 2. Updates payment status
     * 3. Updates contribution if successful
     * 4. Returns current status
     *
     * When to call:
     * - After user returns from payment gateway
     * - To check if payment completed
     * - Manual verification by admin
     *
     * Success Response (HTTP 200):
     * {
     *   "success": true,
     *   "message": "Payment verified successfully",
     *   "data": {
     *     "reference": "PMT-1705318245000-AB12CD34",
     *     "status": "SUCCESS",
     *     "paidAt": "2025-01-15T10:35:00",
     *     ...
     *   }
     * }
     *
     * @param reference Payment reference
     * @return Verified payment details
     */
    @GetMapping("/verify/{reference}")
    @Operation(
            summary = "Verify payment",
            description = "Verify payment status with gateway and update records"
    )
    public ResponseEntity<ApiResponseDto<PaymentResponse>> verifyPayment(
            @PathVariable String reference) {

        log.info("GET /api/v1/payments/verify/{} - Verify payment", reference);

        try {
            PaymentResponse response = paymentService.verifyPayment(reference);

            String message = response.isCompleted()
                    ? "Payment verified successfully"
                    : "Payment verification completed - Status: " + response.getStatus();

            ApiResponseDto<PaymentResponse> apiResponse = ApiResponseDto.<PaymentResponse>builder()
                    .success(true)
                    .message(message)
                    .data(response)
                    .build();

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            log.error("Error verifying payment: {}", e.getMessage(), e);

            ApiResponseDto<PaymentResponse> apiResponse = ApiResponseDto.<PaymentResponse>builder()
                    .success(false)
                    .message("Payment verification failed: " + e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * PAYSTACK WEBHOOK HANDLER
     *
     * HTTP Method: POST
     * URL: /api/v1/payments/webhook/paystack
     * Content-Type: application/json
     *
     * What is a webhook?
     * - HTTP POST request from Paystack
     * - Sent when payment status changes
     * - Real-time notification
     * - More reliable than polling
     *
     * Webhook Events:
     * - charge.success: Payment successful
     * - charge.failed: Payment failed
     * - transfer.success: Transfer completed
     * - etc.
     *
     * Security:
     * - Paystack sends signature in header: x-paystack-signature
     * - We verify signature before processing
     * - Prevents fake webhooks
     *
     * Configuration in Paystack Dashboard:
     * - Webhook URL: https://yourapp.com/api/v1/payments/webhook/paystack
     * - Events: charge.success
     *
     * Example Webhook Body:
     * {
     *   "event": "charge.success",
     *   "data": {
     *     "reference": "PMT-1705318245000-AB12CD34",
     *     "amount": 1000000,
     *     "status": "success",
     *     "paid_at": "2025-01-15T10:35:00.000Z"
     *   }
     * }
     *
     * @param payload Webhook JSON body
     * @param signature Signature from x-paystack-signature header
     * @return HTTP 200 (always, even if processing fails)
     */
    @PostMapping("/webhook/paystack")
    @Operation(
            summary = "Paystack webhook",
            description = "Handle payment notifications from Paystack"
    )
    public ResponseEntity<String> paystackWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "x-paystack-signature", required = false) String signature) {

        log.info("POST /api/v1/payments/webhook/paystack - Received webhook");

        // Always return 200 to Paystack (even if processing fails)
        // This prevents webhook retries for processing errors
        try {
            if (signature == null) {
                log.error("Missing webhook signature");
                return ResponseEntity.ok("Signature missing");
            }

            // Process webhook (includes signature verification)
            paymentService.handleWebhook(payload, signature, "paystack");

            log.info("Paystack webhook processed successfully");
            return ResponseEntity.ok("Webhook processed");

        } catch (SecurityException e) {
            // Invalid signature - possible fraud
            log.error("Invalid webhook signature: {}", e.getMessage());
            return ResponseEntity.ok("Invalid signature");

        } catch (Exception e) {
            // Processing error - log but return 200
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.ok("Processing error");
        }
    }

    /**
     * FLUTTERWAVE WEBHOOK HANDLER
     *
     * HTTP Method: POST
     * URL: /api/v1/payments/webhook/flutterwave
     * Content-Type: application/json
     *
     * Similar to Paystack but different verification:
     * - Flutterwave sends: verif-hash header
     * - We compare with configured secret hash
     *
     * Configuration in Flutterwave Dashboard:
     * - Webhook URL: https://yourapp.com/api/v1/payments/webhook/flutterwave
     * - Secret Hash: (copy from dashboard)
     *
     * Example Webhook Body:
     * {
     *   "event": "charge.completed",
     *   "data": {
     *     "tx_ref": "FLW-1705318245000-AB12CD34",
     *     "amount": 10000,
     *     "status": "successful"
     *   }
     * }
     *
     * @param payload Webhook JSON body
     * @param verifHash Hash from verif-hash header
     * @return HTTP 200
     */
    @PostMapping("/webhook/flutterwave")
    @Operation(
            summary = "Flutterwave webhook",
            description = "Handle payment notifications from Flutterwave"
    )
    public ResponseEntity<String> flutterwaveWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "verif-hash", required = false) String verifHash) {

        log.info("POST /api/v1/payments/webhook/flutterwave - Received webhook");

        try {
            if (verifHash == null) {
                log.error("Missing webhook hash");
                return ResponseEntity.ok("Hash missing");
            }

            // Process webhook
            paymentService.handleWebhook(payload, verifHash, "flutterwave");

            log.info("Flutterwave webhook processed successfully");
            return ResponseEntity.ok("Webhook processed");

        } catch (SecurityException e) {
            log.error("Invalid webhook hash: {}", e.getMessage());
            return ResponseEntity.ok("Invalid hash");

        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.ok("Processing error");
        }
    }

    /**
     * PAYMENT CALLBACK HANDLER
     *
     * HTTP Method: GET
     * URL: /api/v1/payments/callback
     *
     * What is a callback?
     * - Where gateway redirects user after payment
     * - User's browser navigates here
     * - We verify payment and show result
     *
     * Query Parameters:
     * - reference: Our payment reference
     * - status: Payment status (from gateway)
     * - trxref: Gateway's transaction reference
     *
     * Example URL:
     * /api/v1/payments/callback?reference=PMT-123&status=success&trxref=ref_456
     *
     * Flow:
     * 1. User completes payment on gateway
     * 2. Gateway redirects to this URL
     * 3. We verify payment with gateway
     * 4. Redirect to success/failure page
     *
     * @param reference Our payment reference
     * @param status Status from gateway
     * @return Redirect to success/failure page
     */
    @GetMapping("/callback")
    @Operation(
            summary = "Payment callback",
            description = "Handle redirect after payment completion"
    )
    public ResponseEntity<ApiResponseDto<PaymentResponse>> paymentCallback(
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) String status) {

        log.info("GET /api/v1/payments/callback - reference: {}, status: {}", reference, status);

        if (reference == null) {
            ApiResponseDto<PaymentResponse> apiResponse = ApiResponseDto.<PaymentResponse>builder()
                    .success(false)
                    .message("Payment reference missing")
                    .build();

            return ResponseEntity.badRequest().body(apiResponse);
        }

        try {
            // Verify payment (don't trust status from URL)
            PaymentResponse response = paymentService.verifyPayment(reference);

            String message = response.isCompleted()
                    ? "Payment completed successfully!"
                    : "Payment status: " + response.getStatus();

            ApiResponseDto<PaymentResponse> apiResponse = ApiResponseDto.<PaymentResponse>builder()
                    .success(response.isCompleted())
                    .message(message)
                    .data(response)
                    .build();

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            log.error("Error in callback: {}", e.getMessage(), e);

            ApiResponseDto<PaymentResponse> apiResponse = ApiResponseDto.<PaymentResponse>builder()
                    .success(false)
                    .message("Error processing payment callback")
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
}
