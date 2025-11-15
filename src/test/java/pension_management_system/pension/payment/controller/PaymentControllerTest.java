package pension_management_system.pension.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pension_management_system.pension.payment.dto.InitializePaymentRequest;
import pension_management_system.pension.payment.dto.PaymentResponse;
import pension_management_system.pension.payment.entity.PaymentGateway;
import pension_management_system.pension.payment.entity.PaymentStatus;
import pension_management_system.pension.payment.service.PaymentService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PaymentControllerTest - Controller/API layer tests
 *
 * Purpose: Test REST API endpoints without starting full application
 *
 * What is @WebMvcTest?
 * - Spring Boot test annotation for controllers
 * - Tests only web layer (controllers)
 * - Doesn't load full application context
 * - Faster than full integration tests
 * - Automatically configures MockMvc
 *
 * What is MockMvc?
 * - Spring's framework for testing controllers
 * - Simulates HTTP requests without real HTTP
 * - Tests request mapping, validation, response format
 * - No need to start server
 *
 * What are we testing?
 * - HTTP endpoints work correctly
 * - Request validation
 * - Response format (JSON)
 * - HTTP status codes
 * - Error handling
 *
 * Difference from Unit Tests:
 * - Unit tests: Test business logic in isolation
 * - Controller tests: Test HTTP layer (requests/responses)
 * - Still mock service layer (not full integration)
 *
 * Annotations:
 * @WebMvcTest - Configure Spring MVC test
 * @MockBean - Mock beans in Spring context
 * @Autowired - Inject MockMvc and ObjectMapper
 */
@WebMvcTest(PaymentController.class)
@DisplayName("Payment Controller API Tests")
class PaymentControllerTest {

    /**
     * MOCKMVC
     *
     * Simulates HTTP requests
     * Automatically injected by @WebMvcTest
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * OBJECT MAPPER
     *
     * Converts objects to/from JSON
     * Used to create request bodies
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * MOCKED SERVICE
     *
     * @MockBean replaces real PaymentService with mock
     * We control its behavior in tests
     */
    @MockBean
    private PaymentService paymentService;

    // Test data
    private InitializePaymentRequest paymentRequest;
    private PaymentResponse paymentResponse;

    @BeforeEach
    void setUp() {
        // Create test payment request
        paymentRequest = InitializePaymentRequest.builder()
                .contributionId(1L)
                .amount(BigDecimal.valueOf(50000))
                .gateway(PaymentGateway.PAYSTACK)
                .email("member@example.com")
                .callbackUrl("https://pension.com/callback")
                .build();

        // Create test payment response
        paymentResponse = PaymentResponse.builder()
                .id(1L)
                .reference("PMT-123456")
                .contributionId(1L)
                .amount(BigDecimal.valueOf(50000))
                .gateway(PaymentGateway.PAYSTACK)
                .status(PaymentStatus.PENDING)
                .authorizationUrl("https://checkout.paystack.com/abc123")
                .build();
    }

    /**
     * TEST: Initialize Payment Endpoint
     *
     * Tests POST /api/v1/payments/initialize
     *
     * What we're testing:
     * - Endpoint is accessible
     * - Accepts POST requests
     * - Validates request body
     * - Returns correct status code (201 Created)
     * - Returns JSON response
     * - Response contains expected fields
     */
    @Test
    @DisplayName("POST /api/v1/payments/initialize - Should initialize payment")
    void initializePayment_Success() throws Exception {
        // ARRANGE: Mock service response
        when(paymentService.initializePayment(any(InitializePaymentRequest.class)))
                .thenReturn(paymentResponse);

        // ACT & ASSERT: Perform HTTP request and verify response
        mockMvc.perform(post("/api/v1/payments/initialize")
                        .contentType(MediaType.APPLICATION_JSON) // Request is JSON
                        .content(objectMapper.writeValueAsString(paymentRequest))) // Convert to JSON
                .andDo(print()) // Print request/response (helpful for debugging)
                .andExpect(status().isCreated()) // Expect 201 Created
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Response is JSON
                .andExpect(jsonPath("$.success").value(true)) // Check success field
                .andExpect(jsonPath("$.data.reference").value("PMT-123456")) // Check reference
                .andExpect(jsonPath("$.data.authorizationUrl").exists()) // Check URL exists
                .andExpect(jsonPath("$.data.status").value("PENDING")); // Check status

        // Verify service was called
        verify(paymentService, times(1)).initializePayment(any());
    }

    /**
     * TEST: Validation - Missing Required Fields
     *
     * Tests that validation works for invalid requests
     *
     * What happens when required fields are missing?
     * - @Valid annotation triggers validation
     * - @NotNull, @Positive constraints checked
     * - Spring returns 400 Bad Request
     * - Error message describes problem
     */
    @Test
    @DisplayName("POST /api/v1/payments/initialize - Should reject invalid request")
    void initializePayment_InvalidRequest() throws Exception {
        // ARRANGE: Create invalid request (missing amount)
        paymentRequest.setAmount(null);

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/payments/initialize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest()); // Expect 400 Bad Request

        // Verify service was NOT called (validation failed before service)
        verify(paymentService, never()).initializePayment(any());
    }

    /**
     * TEST: Verify Payment Endpoint
     *
     * Tests GET /api/v1/payments/verify/{reference}
     *
     * What we're testing:
     * - Path variable extraction
     * - GET request handling
     * - Service integration
     * - Response format
     */
    @Test
    @DisplayName("GET /api/v1/payments/verify/{reference} - Should verify payment")
    void verifyPayment_Success() throws Exception {
        // ARRANGE
        paymentResponse.setStatus(PaymentStatus.SUCCESS);
        when(paymentService.verifyPayment(anyString()))
                .thenReturn(paymentResponse);

        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/payments/verify/PMT-123456")
                        .accept(MediaType.APPLICATION_JSON)) // Client accepts JSON
                .andDo(print())
                .andExpect(status().isOk()) // Expect 200 OK
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.reference").value("PMT-123456"));

        // Verify correct reference was passed to service
        verify(paymentService).verifyPayment("PMT-123456");
    }

    /**
     * TEST: Get Payment By Reference
     *
     * Tests GET /api/v1/payments/{reference}
     */
    @Test
    @DisplayName("GET /api/v1/payments/{reference} - Should get payment details")
    void getPaymentByReference_Success() throws Exception {
        // ARRANGE
        when(paymentService.getPaymentByReference(anyString()))
                .thenReturn(paymentResponse);

        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/payments/PMT-123456")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reference").value("PMT-123456"));

        verify(paymentService).getPaymentByReference("PMT-123456");
    }

    /**
     * TEST: Paystack Webhook Endpoint
     *
     * Tests POST /api/v1/payments/webhook/paystack
     *
     * What we're testing:
     * - Webhook endpoint is accessible
     * - Header extraction works
     * - Service handles webhook processing
     */
    @Test
    @DisplayName("POST /api/v1/payments/webhook/paystack - Should process webhook")
    void paystackWebhook_Success() throws Exception {
        // ARRANGE
        String webhookPayload = "{\"event\":\"charge.success\",\"data\":{\"reference\":\"PMT-123456\"}}";
        String signature = "test-signature-hash";

        // Mock service (void method, just verify it's called)
        doNothing().when(paymentService)
                .handleWebhook(anyString(), anyString(), anyString());

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/payments/webhook/paystack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-paystack-signature", signature) // Paystack signature header
                        .content(webhookPayload))
                .andDo(print())
                .andExpect(status().isOk()); // Webhooks return 200 OK

        // Verify webhook was processed
        verify(paymentService).handleWebhook(
                eq(webhookPayload),
                eq(signature),
                eq("paystack")
        );
    }

    /**
     * TEST: Flutterwave Webhook Endpoint
     *
     * Tests POST /api/v1/payments/webhook/flutterwave
     */
    @Test
    @DisplayName("POST /api/v1/payments/webhook/flutterwave - Should process webhook")
    void flutterwaveWebhook_Success() throws Exception {
        // ARRANGE
        String webhookPayload = "{\"event\":\"charge.completed\",\"data\":{\"tx_ref\":\"PMT-123456\"}}";
        String verifHash = "test-hash";

        doNothing().when(paymentService)
                .handleWebhook(anyString(), anyString(), anyString());

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/payments/webhook/flutterwave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("verif-hash", verifHash) // Flutterwave hash header
                        .content(webhookPayload))
                .andExpect(status().isOk());

        verify(paymentService).handleWebhook(
                eq(webhookPayload),
                eq(verifHash),
                eq("flutterwave")
        );
    }

    /**
     * TEST: Payment Callback Endpoint
     *
     * Tests GET /api/v1/payments/callback
     *
     * This is where payment gateway redirects user after payment
     */
    @Test
    @DisplayName("GET /api/v1/payments/callback - Should handle payment callback")
    void paymentCallback_Success() throws Exception {
        // ARRANGE
        when(paymentService.verifyPayment(anyString()))
                .thenReturn(paymentResponse);

        // ACT & ASSERT
        // Callback includes reference as query parameter
        mockMvc.perform(get("/api/v1/payments/callback")
                        .param("reference", "PMT-123456")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data.reference").value("PMT-123456"));

        verify(paymentService).verifyPayment("PMT-123456");
    }

    /**
     * TEST: Error Handling
     *
     * Tests that controller handles service exceptions properly
     */
    @Test
    @DisplayName("Should handle service exceptions and return error response")
    void handleServiceException() throws Exception {
        // ARRANGE: Make service throw exception
        when(paymentService.getPaymentByReference(anyString()))
                .thenThrow(new RuntimeException("Payment not found"));

        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/payments/INVALID-REF")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()) // Or whatever error handling returns
                .andDo(print());

        verify(paymentService).getPaymentByReference("INVALID-REF");
    }
}

/**
 * MOCKMVC CHEAT SHEET
 *
 * Making Requests:
 * - get("/path") → GET request
 * - post("/path") → POST request
 * - put("/path") → PUT request
 * - delete("/path") → DELETE request
 *
 * Request Customization:
 * - .param("key", "value") → Query parameter
 * - .header("name", "value") → HTTP header
 * - .contentType(MediaType.APPLICATION_JSON) → Content-Type header
 * - .content(json) → Request body
 * - .accept(MediaType.APPLICATION_JSON) → Accept header
 *
 * Assertions:
 * - .andExpect(status().isOk()) → 200
 * - .andExpect(status().isCreated()) → 201
 * - .andExpect(status().isBadRequest()) → 400
 * - .andExpect(status().isNotFound()) → 404
 * - .andExpect(status().isInternalServerError()) → 500
 *
 * JSON Assertions (using JsonPath):
 * - .andExpect(jsonPath("$.field").value("expected"))
 * - .andExpect(jsonPath("$.field").exists())
 * - .andExpect(jsonPath("$.field").isString())
 * - .andExpect(jsonPath("$.array").isArray())
 * - .andExpect(jsonPath("$.array[0].field").value("expected"))
 *
 * Debugging:
 * - .andDo(print()) → Print request/response to console
 *
 * INTEGRATION VS UNIT VS CONTROLLER TESTS
 *
 * Unit Tests (e.g., PaymentServiceImplTest):
 * - Test single class in isolation
 * - Mock all dependencies
 * - Fast (milliseconds)
 * - Test business logic
 *
 * Controller Tests (this file):
 * - Test HTTP layer
 * - Mock service layer
 * - Test request/response format
 * - Faster than full integration
 *
 * Integration Tests:
 * - Test multiple layers together
 * - Use real database (H2, Testcontainers)
 * - Slower but more realistic
 * - Test end-to-end flow
 *
 * BEST PRACTICES
 *
 * 1. Test Happy Path First:
 *    - Success scenarios
 *    - Then add error cases
 *
 * 2. Test All HTTP Methods:
 *    - GET, POST, PUT, DELETE
 *    - Each has different behavior
 *
 * 3. Test Validation:
 *    - Invalid requests
 *    - Missing fields
 *    - Wrong types
 *
 * 4. Test Error Handling:
 *    - Service exceptions
 *    - Proper status codes
 *    - Error messages
 *
 * 5. Use Descriptive Names:
 *    - endpoint_scenario_expectedBehavior
 *    - Clear @DisplayName
 *
 * 6. Verify Service Calls:
 *    - Use Mockito verify()
 *    - Ensure correct parameters
 *    - Check call counts
 */
