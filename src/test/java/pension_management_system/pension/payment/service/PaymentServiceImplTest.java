package pension_management_system.pension.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import pension_management_system.pension.payment.service.gateway.FlutterwaveService;
import pension_management_system.pension.payment.service.gateway.PaystackService;
import pension_management_system.pension.payment.service.impl.PaymentServiceImpl;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * PaymentServiceImplTest - Unit tests for PaymentServiceImpl
 *
 * Purpose: Test payment processing logic in isolation
 *
 * What is Unit Testing?
 * - Testing individual components (units) in isolation
 * - Mock external dependencies
 * - Fast execution (no database, no network calls)
 * - Ensure code behaves correctly under different scenarios
 *
 * What is JUnit 5?
 * - Industry-standard testing framework for Java
 * - Provides annotations like @Test, @BeforeEach, @DisplayName
 * - Assertions: assertEquals, assertTrue, assertThrows, etc.
 * - Used by 90%+ of Java projects
 *
 * What is Mockito?
 * - Mocking framework for Java
 * - Creates "mock" objects that simulate real behavior
 * - Verify method calls and interactions
 * - Control return values for testing different scenarios
 *
 * Why Mock?
 * - Don't need real database for unit tests
 * - Don't make real API calls to payment gateways
 * - Test edge cases easily (errors, timeouts, etc.)
 * - Tests run fast (milliseconds vs seconds)
 *
 * Annotations Explained:
 * @ExtendWith(MockitoExtension.class) - Enables Mockito in JUnit 5
 * @Mock - Creates a mock object (fake implementation)
 * @InjectMocks - Creates real object, injects mocks into it
 * @BeforeEach - Runs before each test method
 * @Test - Marks method as a test case
 * @DisplayName - Human-readable test name
 *
 * Test Structure (AAA Pattern):
 * 1. ARRANGE: Set up test data and mocks
 * 2. ACT: Execute the method being tested
 * 3. ASSERT: Verify expected outcome
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Service Unit Tests")
class PaymentServiceImplTest {

    /**
     * MOCKED DEPENDENCIES
     *
     * @Mock creates fake implementations
     * These don't do real work - we control their behavior
     */
    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ContributionRepository contributionRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private PaystackService paystackService;

    @Mock
    private FlutterwaveService flutterwaveService;

    @Mock
    private ObjectMapper objectMapper;

    /**
     * SYSTEM UNDER TEST
     *
     * @InjectMocks creates real PaymentServiceImpl
     * Mockito automatically injects all @Mock objects into it
     */
    @InjectMocks
    private PaymentServiceImpl paymentService;

    // Test data
    private InitializePaymentRequest paymentRequest;
    private Contribution contribution;
    private Payment payment;
    private PaymentResponse paymentResponse;

    /**
     * SETUP METHOD
     *
     * Runs before each test
     * Initializes test data
     *
     * Why @BeforeEach instead of constructor?
     * - Ensures fresh data for each test
     * - Tests don't affect each other
     * - Each test is independent
     */
    @BeforeEach
    void setUp() {
        // ARRANGE: Create test data

        // 1. Create contribution (what's being paid for)
        contribution = Contribution.builder()
                .id(1L)
                .contributionAmount(BigDecimal.valueOf(50000))
                .status(ContributionStatus.PENDING)
                .build();

        // 2. Create payment request
        paymentRequest = InitializePaymentRequest.builder()
                .contributionId(1L)
                .amount(BigDecimal.valueOf(50000))
                .gateway(PaymentGateway.PAYSTACK)
                .email("member@example.com")
                .callbackUrl("https://pension.com/callback")
                .build();

        // 3. Create payment entity
        payment = Payment.builder()
                .id(1L)
                .contribution(contribution)
                .amount(BigDecimal.valueOf(50000))
                .gateway(PaymentGateway.PAYSTACK)
                .status(PaymentStatus.INITIATED)
                .reference("PMT-123456")
                .build();

        // 4. Create payment response
        paymentResponse = PaymentResponse.builder()
                .id(1L)
                .reference("PMT-123456")
                .amount(BigDecimal.valueOf(50000))
                .status(PaymentStatus.PENDING)
                .authorizationUrl("https://checkout.paystack.com/abc123")
                .build();
    }

    /**
     * TEST: Initialize Payment - Success Scenario
     *
     * Tests the happy path where everything works correctly
     *
     * Scenario:
     * - Contribution exists
     * - Amount matches
     * - Payment initialized successfully
     * - Returns authorization URL
     */
    @Test
    @DisplayName("Should initialize payment successfully with Paystack")
    void initializePayment_Success() {
        // ARRANGE: Set up mock behavior

        // 1. Mock contribution repository - return our test contribution
        when(contributionRepository.findById(1L))
                .thenReturn(Optional.of(contribution));

        // 2. Mock payment repository - return saved payment
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(payment);

        // 3. Mock Paystack service - return gateway response
        Map<String, Object> paystackResponse = Map.of(
                "status", true,
                "data", Map.of(
                        "authorization_url", "https://checkout.paystack.com/abc123",
                        "reference", "PMT-123456"
                )
        );
        when(paystackService.initializeTransaction(any()))
                .thenReturn(paystackResponse);
        when(paystackService.getAuthorizationUrl(any()))
                .thenReturn("https://checkout.paystack.com/abc123");

        // 4. Mock payment mapper
        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(paymentResponse);

        // ACT: Execute the method being tested
        PaymentResponse result = paymentService.initializePayment(paymentRequest);

        // ASSERT: Verify expected outcome

        // 1. Result is not null
        assertNotNull(result, "Payment response should not be null");

        // 2. Authorization URL is present
        assertNotNull(result.getAuthorizationUrl(),
                "Authorization URL should be present");

        // 3. Payment reference matches
        assertEquals("PMT-123456", result.getReference(),
                "Payment reference should match");

        // 4. Verify interactions
        // Ensure contribution was fetched
        verify(contributionRepository).findById(1L);

        // Ensure payment was saved twice (initial + updated)
        verify(paymentRepository, times(2)).save(any(Payment.class));

        // Ensure Paystack was called
        verify(paystackService).initializeTransaction(any());
    }

    /**
     * TEST: Initialize Payment - Contribution Not Found
     *
     * Tests error handling when contribution doesn't exist
     *
     * Expected: Should throw RuntimeException
     */
    @Test
    @DisplayName("Should throw exception when contribution not found")
    void initializePayment_ContributionNotFound() {
        // ARRANGE: Mock contribution repository to return empty
        when(contributionRepository.findById(1L))
                .thenReturn(Optional.empty());

        // ACT & ASSERT: Verify exception is thrown
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> paymentService.initializePayment(paymentRequest),
                "Should throw RuntimeException when contribution not found"
        );

        // Verify error message
        assertTrue(exception.getMessage().contains("Contribution not found"),
                "Error message should mention contribution not found");

        // Verify no payment was saved
        verify(paymentRepository, never()).save(any());
    }

    /**
     * TEST: Initialize Payment - Amount Mismatch
     *
     * Tests validation when payment amount doesn't match contribution
     *
     * Security: Prevents users from paying different amount than owed
     */
    @Test
    @DisplayName("Should throw exception when payment amount doesn't match")
    void initializePayment_AmountMismatch() {
        // ARRANGE: Set different amount
        paymentRequest.setAmount(BigDecimal.valueOf(30000)); // Contribution is 50000

        when(contributionRepository.findById(1L))
                .thenReturn(Optional.of(contribution));

        // ACT & ASSERT: Verify exception
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> paymentService.initializePayment(paymentRequest),
                "Should throw exception when amounts don't match"
        );

        assertTrue(exception.getMessage().contains("amount does not match"),
                "Error message should mention amount mismatch");
    }

    /**
     * TEST: Initialize Payment - Already Paid
     *
     * Tests prevention of duplicate payments
     */
    @Test
    @DisplayName("Should throw exception when contribution already paid")
    void initializePayment_AlreadyPaid() {
        // ARRANGE: Mark contribution as completed
        contribution.setStatus(ContributionStatus.COMPLETED);

        when(contributionRepository.findById(1L))
                .thenReturn(Optional.of(contribution));

        // ACT & ASSERT
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> paymentService.initializePayment(paymentRequest),
                "Should throw exception when contribution already paid"
        );

        assertTrue(exception.getMessage().contains("already paid"),
                "Error message should mention already paid");
    }

    /**
     * TEST: Verify Payment - Success
     *
     * Tests payment verification after user completes payment
     */
    @Test
    @DisplayName("Should verify payment successfully")
    void verifyPayment_Success() {
        // ARRANGE
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findByReference("PMT-123456"))
                .thenReturn(Optional.of(payment));

        // Mock Paystack verification response
        Map<String, Object> verificationResponse = Map.of(
                "status", true,
                "data", Map.of("status", "success")
        );
        when(paystackService.verifyTransaction("PMT-123456"))
                .thenReturn(verificationResponse);
        when(paystackService.isTransactionSuccessful(any()))
                .thenReturn(true);

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{\"status\":\"success\"}");

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(payment);

        paymentResponse.setStatus(PaymentStatus.SUCCESS);
        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(paymentResponse);

        // ACT
        PaymentResponse result = paymentService.verifyPayment("PMT-123456");

        // ASSERT
        assertNotNull(result);
        assertEquals(PaymentStatus.SUCCESS, result.getStatus());

        // Verify payment was marked as successful
        verify(paymentRepository, atLeast(1)).save(any(Payment.class));

        // Verify contribution status was updated
        verify(contributionRepository).save(any(Contribution.class));
    }

    /**
     * TEST: Verify Payment - Already Verified
     *
     * Tests idempotency (can call multiple times safely)
     */
    @Test
    @DisplayName("Should return existing result when payment already verified")
    void verifyPayment_AlreadyVerified() {
        // ARRANGE: Payment already successful
        payment.setStatus(PaymentStatus.SUCCESS);

        when(paymentRepository.findByReference("PMT-123456"))
                .thenReturn(Optional.of(payment));

        paymentResponse.setStatus(PaymentStatus.SUCCESS);
        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(paymentResponse);

        // ACT
        PaymentResponse result = paymentService.verifyPayment("PMT-123456");

        // ASSERT
        assertEquals(PaymentStatus.SUCCESS, result.getStatus());

        // Verify gateway was NOT called again
        verify(paystackService, never()).verifyTransaction(anyString());
    }

    /**
     * TEST: Get Payment By Reference
     *
     * Tests fetching payment details
     */
    @Test
    @DisplayName("Should get payment by reference")
    void getPaymentByReference_Success() {
        // ARRANGE
        when(paymentRepository.findByReference("PMT-123456"))
                .thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(paymentResponse);

        // ACT
        PaymentResponse result = paymentService.getPaymentByReference("PMT-123456");

        // ASSERT
        assertNotNull(result);
        assertEquals("PMT-123456", result.getReference());
        verify(paymentRepository).findByReference("PMT-123456");
    }

    /**
     * TEST: Get Payment - Not Found
     */
    @Test
    @DisplayName("Should throw exception when payment not found")
    void getPaymentByReference_NotFound() {
        // ARRANGE
        when(paymentRepository.findByReference("INVALID"))
                .thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(
                RuntimeException.class,
                () -> paymentService.getPaymentByReference("INVALID"),
                "Should throw exception when payment not found"
        );
    }
}

/**
 * RUNNING THESE TESTS
 *
 * From command line:
 * mvn test
 *
 * Run single test class:
 * mvn test -Dtest=PaymentServiceImplTest
 *
 * Run single test method:
 * mvn test -Dtest=PaymentServiceImplTest#initializePayment_Success
 *
 * From IDE:
 * - Right-click class → Run Tests
 * - Right-click method → Run Test
 * - Green = Passed, Red = Failed
 *
 * BENEFITS OF UNIT TESTS
 *
 * 1. Catch Bugs Early:
 *    - Find errors before production
 *    - Automated bug detection
 *
 * 2. Documentation:
 *    - Tests show how code should be used
 *    - Examples of valid inputs and expected outputs
 *
 * 3. Refactoring Safety:
 *    - Change code confidently
 *    - Tests ensure nothing breaks
 *
 * 4. Design Quality:
 *    - Hard-to-test code is often bad design
 *    - Tests force better architecture
 *
 * 5. Faster Development:
 *    - Less manual testing
 *    - Quick feedback loop
 *
 * 6. Regression Prevention:
 *    - Ensure fixed bugs stay fixed
 *    - Prevent old bugs from returning
 *
 * BEST PRACTICES
 *
 * 1. Test One Thing Per Test:
 *    - Each test has single purpose
 *    - Easy to understand what failed
 *
 * 2. Use Descriptive Names:
 *    - methodName_scenario_expectedBehavior
 *    - Should... format for @DisplayName
 *
 * 3. Follow AAA Pattern:
 *    - Arrange: Set up
 *    - Act: Execute
 *    - Assert: Verify
 *
 * 4. Don't Test Framework Code:
 *    - Don't test Spring, JPA, etc.
 *    - Test YOUR business logic
 *
 * 5. Keep Tests Fast:
 *    - Unit tests should run in milliseconds
 *    - Use mocks, not real database
 *
 * 6. Make Tests Independent:
 *    - Each test should run alone
 *    - No shared state between tests
 */
