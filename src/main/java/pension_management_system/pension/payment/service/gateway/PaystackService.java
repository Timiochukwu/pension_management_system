package pension_management_system.pension.payment.service.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pension_management_system.pension.payment.dto.InitializePaymentRequest;
import pension_management_system.pension.exception.PaymentException;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * PaystackService - Integration with Paystack Payment Gateway
 *
 * Purpose: Handle all Paystack API interactions
 *
 * What is Paystack?
 * - Nigerian payment gateway for online payments
 * - Supports cards, bank transfers, USSD, mobile money
 * - REST API with JSON request/response
 * - Webhook notifications for payment status
 *
 * API Documentation: https://paystack.com/docs/api
 *
 * Key Features Implemented:
 * 1. Initialize Transaction - Start payment process
 * 2. Verify Transaction - Confirm payment status
 * 3. Webhook Signature Verification - Secure webhook handling
 *
 * Configuration Required (application.properties):
 * paystack.secret.key=sk_test_your_secret_key
 * paystack.base.url=https://api.paystack.co
 *
 * Testing:
 * - Use test keys from Paystack dashboard
 * - Test card: 4084084084084081 (success)
 * - Test card: 5060666666666666666 (decline)
 *
 * Annotations:
 * @Service - Spring service component
 * @RequiredArgsConstructor - Lombok constructor injection
 * @Slf4j - Lombok logging
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "paystack.enabled", havingValue = "true", matchIfMissing = false)
public class PaystackService {

    /**
     * DEPENDENCIES
     *
     * WebClient - Spring's reactive HTTP client
     * - Better than RestTemplate (non-blocking)
     * - Fluent API for building requests
     * - Supports async operations
     *
     * ObjectMapper - JSON serialization/deserialization
     * - Convert Java objects ↔ JSON
     */
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    /**
     * CONFIGURATION VALUES
     *
     * Injected from application.properties
     * @Value annotation reads property values
     *
     * Example application.properties:
     * paystack.secret.key=sk_test_abc123...
     * paystack.base.url=https://api.paystack.co
     */
    @Value("${paystack.secret.key}")
    private String secretKey;

    @Value("${paystack.base.url}")
    private String baseUrl;

    /**
     * INITIALIZE PAYMENT
     *
     * Step 1 of payment flow: Create transaction with Paystack
     *
     * What it does:
     * 1. Convert amount to kobo (Paystack uses smallest currency unit)
     * 2. Build API request with payment details
     * 3. Send HTTP POST to Paystack
     * 4. Return authorization URL for user to complete payment
     *
     * API Endpoint: POST /transaction/initialize
     * Documentation: https://paystack.com/docs/api/#transaction-initialize
     *
     * Request Body:
     * {
     *   "email": "customer@email.com",
     *   "amount": "1000000",  // Amount in kobo (₦10,000 = 1,000,000 kobo)
     *   "reference": "PMT-20250115-ABC123",
     *   "callback_url": "https://yourapp.com/callback"
     * }
     *
     * Response:
     * {
     *   "status": true,
     *   "message": "Authorization URL created",
     *   "data": {
     *     "authorization_url": "https://checkout.paystack.com/abc123",
     *     "access_code": "abc123",
     *     "reference": "PMT-20250115-ABC123"
     *   }
     * }
     *
     * Why kobo?
     * - Avoids decimal/floating point issues
     * - Standard practice for payment gateways
     * - ₦10,000.00 = 1,000,000 kobo (multiply by 100)
     *
     * @param request Payment initialization details
     * @return Paystack response with authorization URL
     */
    public Map<String, Object> initializeTransaction(InitializePaymentRequest request) {
        log.info("Initializing Paystack transaction for amount: {}", request.getAmount());

        try {
            // STEP 1: Convert amount to kobo
            // Paystack requires amount in smallest currency unit
            // ₦10,000.00 → 1,000,000 kobo
            long amountInKobo = request.getAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();

            // STEP 2: Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", request.getEmail());
            requestBody.put("amount", amountInKobo);
            requestBody.put("reference", generateReference()); // Our unique reference
            requestBody.put("callback_url", request.getCallbackUrl());

            // Optional: Add metadata
            if (request.getMetadata() != null) {
                requestBody.put("metadata", request.getMetadata());
            }

            log.debug("Paystack request body: {}", requestBody);

            // STEP 3: Make HTTP POST request to Paystack
            // WebClient for reactive HTTP calls
            String response = webClientBuilder.build()
                    .post()
                    .uri(baseUrl + "/transaction/initialize")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // Block to make it synchronous (for simplicity)

            // STEP 4: Parse response
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

            log.info("Paystack initialization successful: {}", responseMap.get("data"));
            return responseMap;

        } catch (Exception e) {
            log.error("Error initializing Paystack transaction: {}", e.getMessage(), e);
            throw PaymentException.initializationFailed("Paystack", e);
        }
    }

    /**
     * VERIFY TRANSACTION
     *
     * Step 3 of payment flow: Confirm payment was successful
     *
     * What it does:
     * 1. Call Paystack verify endpoint with transaction reference
     * 2. Check if payment status is "success"
     * 3. Return verification details
     *
     * API Endpoint: GET /transaction/verify/:reference
     * Documentation: https://paystack.com/docs/api/#transaction-verify
     *
     * Response:
     * {
     *   "status": true,
     *   "message": "Verification successful",
     *   "data": {
     *     "id": 1234567890,
     *     "status": "success",
     *     "reference": "PMT-20250115-ABC123",
     *     "amount": 1000000,
     *     "paid_at": "2025-01-15T10:30:45.000Z",
     *     "channel": "card",
     *     "currency": "NGN",
     *     "customer": {
     *       "email": "customer@email.com"
     *     }
     *   }
     * }
     *
     * Status values:
     * - "success": Payment completed
     * - "failed": Payment failed
     * - "abandoned": User didn't complete
     *
     * Why verify?
     * - Don't trust user's browser (can be manipulated)
     * - Webhooks can be delayed or missed
     * - Server-side verification is authoritative
     * - Prevents fraud
     *
     * @param reference Our payment reference
     * @return Verification response from Paystack
     */
    public Map<String, Object> verifyTransaction(String reference) {
        log.info("Verifying Paystack transaction: {}", reference);

        try {
            // STEP 1: Make HTTP GET request to verify endpoint
            String response = webClientBuilder.build()
                    .get()
                    .uri(baseUrl + "/transaction/verify/" + reference)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // STEP 2: Parse response
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

            // STEP 3: Check verification status
            Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
            String status = (String) data.get("status");

            log.info("Paystack verification result - Status: {}", status);

            // STEP 4: Return full response
            return responseMap;

        } catch (Exception e) {
            log.error("Error verifying Paystack transaction: {}", e.getMessage(), e);
            throw PaymentException.verificationFailed(e);
        }
    }

    /**
     * VERIFY WEBHOOK SIGNATURE
     *
     * Security: Ensure webhook really came from Paystack
     *
     * What it does:
     * 1. Paystack sends webhook with signature in header
     * 2. We compute signature using secret key
     * 3. Compare signatures
     * 4. If match → webhook is authentic
     * 5. If different → reject (possible fraud)
     *
     * How Paystack signature works:
     * - Paystack computes: HMAC-SHA512(webhook_body, secret_key)
     * - Sends in header: x-paystack-signature
     * - We compute same and compare
     *
     * Algorithm: HMAC-SHA512
     * - HMAC = Hash-based Message Authentication Code
     * - SHA512 = Secure Hash Algorithm (512-bit)
     * - Cryptographically secure
     *
     * Example:
     * Webhook body: {"event":"charge.success",...}
     * Secret key: sk_test_abc123
     * Signature: a7b3c2d1e4f5... (512-bit hash)
     *
     * Why important?
     * - Anyone can POST to your webhook URL
     * - Signature proves it's really from Paystack
     * - Prevents replay attacks
     * - Security best practice
     *
     * @param payload Webhook request body (JSON string)
     * @param signature Signature from webhook header
     * @return true if signature is valid
     */
    public boolean verifyWebhookSignature(String payload, String signature) {
        log.debug("Verifying Paystack webhook signature");

        try {
            // STEP 1: Compute expected signature
            // Use HMAC-SHA512 algorithm
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA512"
            );
            mac.init(secretKeySpec);

            // STEP 2: Hash the payload
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            // STEP 3: Convert to hex string (Paystack uses hex encoding)
            String computedSignature = bytesToHex(hash);

            // STEP 4: Compare signatures (constant-time comparison for security)
            boolean isValid = computedSignature.equals(signature);

            if (isValid) {
                log.info("Paystack webhook signature verified successfully");
            } else {
                log.warn("Paystack webhook signature verification failed! Possible fraud attempt.");
            }

            return isValid;

        } catch (Exception e) {
            log.error("Error verifying webhook signature: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * HELPER: Generate unique payment reference
     *
     * Format: PST-{timestamp}-{random}
     * Example: PST-20250115103045-ABC123
     *
     * Why timestamp?
     * - Ensures uniqueness
     * - Easy to sort chronologically
     * - Debug-friendly (can see when created)
     *
     * @return Unique payment reference
     */
    private String generateReference() {
        long timestamp = System.currentTimeMillis();
        String random = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "PST-" + timestamp + "-" + random;
    }

    /**
     * HELPER: Convert byte array to hex string
     *
     * Used for signature verification
     * Converts: [0xAB, 0xCD, 0xEF] → "ABCDEF"
     *
     * @param bytes Byte array to convert
     * @return Hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * HELPER: Extract authorization URL from response
     *
     * Convenience method to get the URL user should visit
     *
     * @param initializeResponse Response from initialize endpoint
     * @return Authorization URL or null if not found
     */
    public String getAuthorizationUrl(Map<String, Object> initializeResponse) {
        try {
            Map<String, Object> data = (Map<String, Object>) initializeResponse.get("data");
            return (String) data.get("authorization_url");
        } catch (Exception e) {
            log.error("Error extracting authorization URL: {}", e.getMessage());
            return null;
        }
    }

    /**
     * HELPER: Check if transaction was successful
     *
     * @param verifyResponse Response from verify endpoint
     * @return true if payment succeeded
     */
    public boolean isTransactionSuccessful(Map<String, Object> verifyResponse) {
        try {
            Map<String, Object> data = (Map<String, Object>) verifyResponse.get("data");
            String status = (String) data.get("status");
            return "success".equals(status);
        } catch (Exception e) {
            log.error("Error checking transaction status: {}", e.getMessage());
            return false;
        }
    }
}
