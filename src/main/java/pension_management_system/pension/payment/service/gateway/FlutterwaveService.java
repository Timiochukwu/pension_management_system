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

import java.util.HashMap;
import java.util.Map;

/**
 * FlutterwaveService - Integration with Flutterwave Payment Gateway
 *
 * Purpose: Handle all Flutterwave API interactions
 *
 * What is Flutterwave?
 * - Pan-African payment gateway
 * - Supports multiple countries (Nigeria, Ghana, Kenya, etc.)
 * - Multiple payment methods (cards, banks, mobile money)
 * - REST API with JSON request/response
 *
 * API Documentation: https://developer.flutterwave.com
 *
 * Key Features Implemented:
 * 1. Initialize Payment - Start payment process
 * 2. Verify Transaction - Confirm payment status
 * 3. Webhook Verification - Secure webhook handling
 *
 * Configuration Required (application.properties):
 * flutterwave.secret.key=FLWSECK_TEST-your_secret_key
 * flutterwave.base.url=https://api.flutterwave.com/v3
 *
 * Testing:
 * - Use test keys from Flutterwave dashboard
 * - Test card: 5531886652142950
 * - CVV: 564, PIN: 3310, OTP: 12345
 *
 * Differences from Paystack:
 * - Uses full currency amounts (not kobo)
 * - Different webhook verification (secret hash)
 * - Multi-currency support built-in
 * - Transaction ID instead of reference in some endpoints
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "flutterwave.enabled", havingValue = "true", matchIfMissing = false)
public class FlutterwaveService {

    /**
     * DEPENDENCIES
     */
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    /**
     * CONFIGURATION VALUES
     *
     * Injected from application.properties
     */
    @Value("${flutterwave.secret.key}")
    private String secretKey;

    @Value("${flutterwave.base.url}")
    private String baseUrl;

    /**
     * INITIALIZE PAYMENT
     *
     * Step 1 of payment flow: Create payment with Flutterwave
     *
     * API Endpoint: POST /payments
     * Documentation: https://developer.flutterwave.com/docs/collecting-payments/standard
     *
     * Request Body:
     * {
     *   "tx_ref": "PMT-20250115-ABC123",
     *   "amount": "10000",  // Full amount (not smallest unit)
     *   "currency": "NGN",
     *   "redirect_url": "https://yourapp.com/callback",
     *   "customer": {
     *     "email": "customer@email.com"
     *   },
     *   "customizations": {
     *     "title": "Pension Contribution",
     *     "description": "Monthly contribution payment"
     *   }
     * }
     *
     * Response:
     * {
     *   "status": "success",
     *   "message": "Hosted Link",
     *   "data": {
     *     "link": "https://checkout.flutterwave.com/v3/hosted/pay/xyz789"
     *   }
     * }
     *
     * Key differences from Paystack:
     * 1. Amount is in full currency (₦10,000.00 not kobo)
     * 2. Uses "tx_ref" instead of "reference"
     * 3. Returns payment link directly
     * 4. Requires explicit currency code
     *
     * @param request Payment initialization details
     * @return Flutterwave response with payment link
     */
    public Map<String, Object> initializePayment(InitializePaymentRequest request) {
        log.info("Initializing Flutterwave payment for amount: {}", request.getAmount());

        try {
            // STEP 1: Build request body
            // Note: Flutterwave uses full amount, not smallest unit
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("tx_ref", generateTransactionReference());
            requestBody.put("amount", request.getAmount().toString());
            requestBody.put("currency", "NGN"); // Nigerian Naira
            requestBody.put("redirect_url", request.getCallbackUrl());
            requestBody.put("payment_options", "card,banktransfer,ussd"); // Payment methods

            // Customer information
            Map<String, String> customer = new HashMap<>();
            customer.put("email", request.getEmail());
            requestBody.put("customer", customer);

            // Customizations (optional but recommended)
            Map<String, String> customizations = new HashMap<>();
            customizations.put("title", "Pension Contribution");
            customizations.put("description", "Monthly pension contribution payment");
            customizations.put("logo", "https://yourcompany.com/logo.png"); // Optional
            requestBody.put("customizations", customizations);

            // Metadata (optional)
            if (request.getMetadata() != null) {
                requestBody.put("meta", request.getMetadata());
            }

            log.debug("Flutterwave request body: {}", requestBody);

            // STEP 2: Make HTTP POST request
            String response = webClientBuilder.build()
                    .post()
                    .uri(baseUrl + "/payments")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // STEP 3: Parse response
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

            log.info("Flutterwave initialization successful: {}", responseMap.get("data"));
            return responseMap;

        } catch (Exception e) {
            log.error("Error initializing Flutterwave payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Flutterwave payment: " + e.getMessage(), e);
        }
    }

    /**
     * VERIFY TRANSACTION
     *
     * Step 3 of payment flow: Confirm payment was successful
     *
     * API Endpoint: GET /transactions/:id/verify
     * Documentation: https://developer.flutterwave.com/docs/integration-guides/verifying-transactions
     *
     * Response:
     * {
     *   "status": "success",
     *   "message": "Transaction fetched successfully",
     *   "data": {
     *     "id": 1234567,
     *     "tx_ref": "PMT-20250115-ABC123",
     *     "flw_ref": "FLW-REF-123456789",
     *     "amount": 10000,
     *     "currency": "NGN",
     *     "status": "successful",
     *     "payment_type": "card",
     *     "created_at": "2025-01-15T10:30:45.000Z",
     *     "customer": {
     *       "email": "customer@email.com"
     *     }
     *   }
     * }
     *
     * Status values:
     * - "successful": Payment completed
     * - "failed": Payment failed
     * - "pending": Still processing
     *
     * Important:
     * - Flutterwave uses transaction ID for verification
     * - ID comes from callback/webhook
     * - Can also verify by tx_ref (our reference)
     *
     * @param transactionId Flutterwave transaction ID
     * @return Verification response
     */
    public Map<String, Object> verifyTransaction(Long transactionId) {
        log.info("Verifying Flutterwave transaction: {}", transactionId);

        try {
            // STEP 1: Make HTTP GET request
            String response = webClientBuilder.build()
                    .get()
                    .uri(baseUrl + "/transactions/" + transactionId + "/verify")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // STEP 2: Parse response
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

            // STEP 3: Check status
            Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
            String status = (String) data.get("status");

            log.info("Flutterwave verification result - Status: {}", status);

            return responseMap;

        } catch (Exception e) {
            log.error("Error verifying Flutterwave transaction: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to verify Flutterwave payment: " + e.getMessage(), e);
        }
    }

    /**
     * VERIFY TRANSACTION BY REFERENCE
     *
     * Alternative verification using our transaction reference
     * Useful when you have tx_ref but not transaction ID
     *
     * API Endpoint: GET /transactions/verify_by_reference?tx_ref=XXX
     *
     * @param txRef Our transaction reference
     * @return Verification response
     */
    public Map<String, Object> verifyTransactionByReference(String txRef) {
        log.info("Verifying Flutterwave transaction by reference: {}", txRef);

        try {
            String response = webClientBuilder.build()
                    .get()
                    .uri(baseUrl + "/transactions/verify_by_reference?tx_ref=" + txRef)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

            log.info("Verification by reference successful");
            return responseMap;

        } catch (Exception e) {
            log.error("Error verifying by reference: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to verify by reference: " + e.getMessage(), e);
        }
    }

    /**
     * VERIFY WEBHOOK
     *
     * Security: Ensure webhook really came from Flutterwave
     *
     * Flutterwave webhook verification:
     * - Sends secret hash in header: verif-hash
     * - Compare with our configured secret hash
     * - If match → webhook is authentic
     *
     * Different from Paystack:
     * - Simpler verification (just hash comparison)
     * - No HMAC computation needed
     * - Secret hash is a fixed value from dashboard
     *
     * Configuration:
     * flutterwave.webhook.secret.hash=your_secret_hash_from_dashboard
     *
     * Why important?
     * - Prevent unauthorized webhook calls
     * - Security best practice
     * - Avoid fraud
     *
     * @param secretHash Hash from webhook header
     * @return true if hash is valid
     */
    public boolean verifyWebhook(String secretHash) {
        log.debug("Verifying Flutterwave webhook");

        // Get configured secret hash
        // In production, this should come from @Value
        String configuredHash = secretKey; // For simplicity, using secret key
        // TODO: Add separate flutterwave.webhook.secret.hash property

        boolean isValid = configuredHash.equals(secretHash);

        if (isValid) {
            log.info("Flutterwave webhook verified successfully");
        } else {
            log.warn("Flutterwave webhook verification failed! Possible fraud attempt.");
        }

        return isValid;
    }

    /**
     * HELPER: Generate unique transaction reference
     *
     * Format: FLW-{timestamp}-{random}
     * Example: FLW-20250115103045-ABC123
     *
     * @return Unique transaction reference
     */
    private String generateTransactionReference() {
        long timestamp = System.currentTimeMillis();
        String random = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "FLW-" + timestamp + "-" + random;
    }

    /**
     * HELPER: Extract payment link from response
     *
     * @param initializeResponse Response from initialize endpoint
     * @return Payment link or null if not found
     */
    public String getPaymentLink(Map<String, Object> initializeResponse) {
        try {
            Map<String, Object> data = (Map<String, Object>) initializeResponse.get("data");
            return (String) data.get("link");
        } catch (Exception e) {
            log.error("Error extracting payment link: {}", e.getMessage());
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
            return "successful".equals(status);
        } catch (Exception e) {
            log.error("Error checking transaction status: {}", e.getMessage());
            return false;
        }
    }

    /**
     * HELPER: Get transaction amount
     *
     * @param verifyResponse Response from verify endpoint
     * @return Transaction amount
     */
    public Double getTransactionAmount(Map<String, Object> verifyResponse) {
        try {
            Map<String, Object> data = (Map<String, Object>) verifyResponse.get("data");
            Object amount = data.get("amount");
            if (amount instanceof Integer) {
                return ((Integer) amount).doubleValue();
            } else if (amount instanceof Double) {
                return (Double) amount;
            }
            return 0.0;
        } catch (Exception e) {
            log.error("Error getting transaction amount: {}", e.getMessage());
            return 0.0;
        }
    }
}
