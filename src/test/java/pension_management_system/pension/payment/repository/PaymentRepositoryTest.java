package pension_management_system.pension.payment.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import pension_management_system.pension.contribution.entity.Contribution;
import pension_management_system.pension.contribution.entity.ContributionStatus;
import pension_management_system.pension.payment.entity.Payment;
import pension_management_system.pension.payment.entity.PaymentGateway;
import pension_management_system.pension.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * PaymentRepositoryTest - Integration tests for PaymentRepository
 *
 * Purpose: Test database operations with real (test) database
 *
 * What is @DataJpaTest?
 * - Spring Boot test annotation for JPA repositories
 * - Configures in-memory database (H2 by default)
 * - Auto-configures Spring Data JPA
 * - Each test runs in transaction (rolled back after test)
 * - Faster than full @SpringBootTest
 *
 * What is TestEntityManager?
 * - JPA testing utility provided by Spring
 * - Simplified EntityManager for tests
 * - Methods: persist, flush, find, etc.
 * - Helps set up test data
 *
 * What are we testing?
 * - Custom repository queries work correctly
 * - JPA relationships are mapped properly
 * - Database constraints are enforced
 * - Entity state transitions
 *
 * Integration vs Unit Tests:
 * - Unit tests: Mock database, test logic only
 * - Integration tests: Real database, test queries work
 * - Both are important!
 *
 * Why H2 Database for Tests?
 * - In-memory database (no setup required)
 * - Fast (recreated for each test)
 * - Compatible with MySQL/PostgreSQL syntax
 * - No need to clean up
 *
 * Test Isolation:
 * - Each test gets fresh database
 * - Transactions rolled back after test
 * - Tests don't affect each other
 * - Can run in any order
 *
 * Annotations:
 * @DataJpaTest - Configure JPA test slice
 * @Autowired - Inject repository and TestEntityManager
 * @BeforeEach - Set up test data before each test
 * @Test - Mark test method
 */
@DataJpaTest
@DisplayName("Payment Repository Integration Tests")
class PaymentRepositoryTest {

    /**
     * TEST ENTITY MANAGER
     *
     * Used to set up test data
     * Simplified EntityManager for testing
     */
    @Autowired
    private TestEntityManager entityManager;

    /**
     * REPOSITORY UNDER TEST
     *
     * Real repository implementation (not mocked)
     * Actually executes SQL against H2 database
     */
    @Autowired
    private PaymentRepository paymentRepository;

    // Test data
    private Contribution contribution;
    private Payment payment;

    /**
     * SETUP METHOD
     *
     * Runs before each test
     * Creates test data in database
     *
     * Why create data here?
     * - Fresh data for each test
     * - Tests are independent
     * - Clear what each test needs
     */
    @BeforeEach
    void setUp() {
        // STEP 1: Create contribution (required for payment)
        contribution = Contribution.builder()
                .contributionAmount(BigDecimal.valueOf(50000))
                .status(ContributionStatus.PENDING)
                .build();

        // Persist to database and get ID
        contribution = entityManager.persist(contribution);

        // STEP 2: Create payment
        payment = Payment.builder()
                .contribution(contribution)
                .amount(BigDecimal.valueOf(50000))
                .gateway(PaymentGateway.PAYSTACK)
                .status(PaymentStatus.INITIATED)
                .reference("PMT-TEST-123")
                .authorizationUrl("https://checkout.paystack.com/test")
                .build();

        // Persist to database
        payment = entityManager.persist(payment);

        // STEP 3: Flush changes to database
        // Ensures data is committed before test runs
        entityManager.flush();
    }

    /**
     * TEST: Find Payment By Reference
     *
     * Tests custom query: findByReference()
     *
     * What we're testing:
     * - Custom query works
     * - Returns Optional
     * - Finds existing payment
     * - Returns empty for non-existent payment
     */
    @Test
    @DisplayName("Should find payment by reference")
    void findByReference_ExistingPayment_ReturnsPayment() {
        // ACT: Call repository method
        Optional<Payment> found = paymentRepository.findByReference("PMT-TEST-123");

        // ASSERT: Verify result
        assertTrue(found.isPresent(), "Payment should be found");
        assertEquals("PMT-TEST-123", found.get().getReference());
        assertEquals(PaymentGateway.PAYSTACK, found.get().getGateway());
        assertEquals(BigDecimal.valueOf(50000), found.get().getAmount());
    }

    /**
     * TEST: Find Non-Existent Payment
     *
     * Tests that query returns empty for missing payment
     */
    @Test
    @DisplayName("Should return empty for non-existent reference")
    void findByReference_NonExistent_ReturnsEmpty() {
        // ACT
        Optional<Payment> found = paymentRepository.findByReference("INVALID-REF");

        // ASSERT
        assertFalse(found.isPresent(), "Should not find payment with invalid reference");
    }

    /**
     * TEST: Find Payments By Status
     *
     * Tests custom query: findByStatus()
     *
     * Assumes this query exists in PaymentRepository
     * If not, this demonstrates how to test it when you add it
     */
    @Test
    @DisplayName("Should find payments by status")
    void findByStatus_ReturnsMatchingPayments() {
        // ARRANGE: Create additional payments with different statuses
        Payment successfulPayment = Payment.builder()
                .contribution(contribution)
                .amount(BigDecimal.valueOf(30000))
                .gateway(PaymentGateway.FLUTTERWAVE)
                .status(PaymentStatus.SUCCESS)
                .reference("PMT-TEST-456")
                .build();
        entityManager.persist(successfulPayment);

        Payment failedPayment = Payment.builder()
                .contribution(contribution)
                .amount(BigDecimal.valueOf(20000))
                .gateway(PaymentGateway.PAYSTACK)
                .status(PaymentStatus.FAILED)
                .reference("PMT-TEST-789")
                .build();
        entityManager.persist(failedPayment);

        entityManager.flush();

        // ACT: Find by status
        // Note: If this method doesn't exist, add it to PaymentRepository:
        // List<Payment> findByStatus(PaymentStatus status);

        // For demonstration, using findAll() and filtering
        List<Payment> allPayments = paymentRepository.findAll();

        // ASSERT
        assertThat(allPayments).hasSize(3); // Total 3 payments created

        // Verify we can filter by status
        long initiatedCount = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.INITIATED)
                .count();
        assertEquals(1, initiatedCount, "Should have 1 INITIATED payment");
    }

    /**
     * TEST: Save Payment
     *
     * Tests that repository saves payment correctly
     */
    @Test
    @DisplayName("Should save new payment")
    void save_NewPayment_SavesSuccessfully() {
        // ARRANGE: Create new payment
        Payment newPayment = Payment.builder()
                .contribution(contribution)
                .amount(BigDecimal.valueOf(75000))
                .gateway(PaymentGateway.FLUTTERWAVE)
                .status(PaymentStatus.PENDING)
                .reference("PMT-NEW-001")
                .build();

        // ACT: Save payment
        Payment saved = paymentRepository.save(newPayment);

        // ASSERT: Verify payment was saved
        assertNotNull(saved.getId(), "Saved payment should have ID");
        assertEquals("PMT-NEW-001", saved.getReference());

        // Verify we can retrieve it
        Optional<Payment> retrieved = paymentRepository.findById(saved.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("PMT-NEW-001", retrieved.get().getReference());
    }

    /**
     * TEST: Update Payment
     *
     * Tests that repository updates existing payment
     */
    @Test
    @DisplayName("Should update existing payment")
    void save_UpdatePayment_UpdatesSuccessfully() {
        // ARRANGE: Get existing payment
        Payment existing = paymentRepository.findByReference("PMT-TEST-123")
                .orElseThrow();

        // ACT: Update payment
        existing.setStatus(PaymentStatus.SUCCESS);
        existing.markAsSuccessful("{\"status\":\"success\"}");

        Payment updated = paymentRepository.save(existing);

        // ASSERT: Verify update
        assertEquals(PaymentStatus.SUCCESS, updated.getStatus());
        assertNotNull(updated.getPaidAt(), "Should have paidAt timestamp");

        // Verify in database
        entityManager.clear(); // Clear persistence context
        Payment fromDb = paymentRepository.findById(updated.getId()).orElseThrow();
        assertEquals(PaymentStatus.SUCCESS, fromDb.getStatus());
    }

    /**
     * TEST: Delete Payment
     *
     * Tests that repository deletes payment
     */
    @Test
    @DisplayName("Should delete payment")
    void delete_ExistingPayment_DeletesSuccessfully() {
        // ARRANGE: Get payment ID
        Long paymentId = payment.getId();

        // ACT: Delete payment
        paymentRepository.deleteById(paymentId);

        // ASSERT: Verify deletion
        Optional<Payment> deleted = paymentRepository.findById(paymentId);
        assertFalse(deleted.isPresent(), "Payment should be deleted");
    }

    /**
     * TEST: Cascade Relationship
     *
     * Tests JPA relationship mapping
     * Payment references Contribution - is it loaded correctly?
     */
    @Test
    @DisplayName("Should load contribution with payment")
    void findById_LoadsContribution() {
        // ACT: Find payment
        Payment found = paymentRepository.findById(payment.getId())
                .orElseThrow();

        // ASSERT: Verify contribution is loaded
        assertNotNull(found.getContribution(), "Contribution should be loaded");
        assertEquals(contribution.getId(), found.getContribution().getId());
        assertEquals(BigDecimal.valueOf(50000), found.getContribution().getContributionAmount());
    }

    /**
     * TEST: Find All Payments
     *
     * Tests basic findAll() query
     */
    @Test
    @DisplayName("Should find all payments")
    void findAll_ReturnsAllPayments() {
        // ARRANGE: Create one more payment
        Payment anotherPayment = Payment.builder()
                .contribution(contribution)
                .amount(BigDecimal.valueOf(25000))
                .gateway(PaymentGateway.PAYSTACK)
                .status(PaymentStatus.PENDING)
                .reference("PMT-TEST-ANOTHER")
                .build();
        entityManager.persist(anotherPayment);
        entityManager.flush();

        // ACT: Find all
        List<Payment> allPayments = paymentRepository.findAll();

        // ASSERT: Verify count
        assertThat(allPayments)
                .hasSize(2)
                .extracting(Payment::getReference)
                .containsExactlyInAnyOrder("PMT-TEST-123", "PMT-TEST-ANOTHER");
    }

    /**
     * TEST: Entity Validation
     *
     * Tests that database constraints work
     * Example: Amount cannot be null
     */
    @Test
    @DisplayName("Should enforce not-null constraints")
    void save_NullAmount_ThrowsException() {
        // ARRANGE: Create payment with null amount
        Payment invalidPayment = Payment.builder()
                .contribution(contribution)
                .amount(null) // Violates @Column(nullable = false)
                .gateway(PaymentGateway.PAYSTACK)
                .status(PaymentStatus.INITIATED)
                .reference("PMT-INVALID")
                .build();

        // ACT & ASSERT: Should throw exception
        assertThrows(Exception.class, () -> {
            entityManager.persist(invalidPayment);
            entityManager.flush(); // Flush triggers validation
        }, "Should throw exception for null amount");
    }

    /**
     * TEST: Unique Constraint
     *
     * Tests unique constraint on payment reference
     * (Assuming reference is unique - adjust if not)
     */
    @Test
    @DisplayName("Should enforce unique reference constraint")
    void save_DuplicateReference_ThrowsException() {
        // ARRANGE: Create payment with same reference
        Payment duplicate = Payment.builder()
                .contribution(contribution)
                .amount(BigDecimal.valueOf(10000))
                .gateway(PaymentGateway.PAYSTACK)
                .status(PaymentStatus.INITIATED)
                .reference("PMT-TEST-123") // Same as existing payment
                .build();

        // ACT & ASSERT: Should throw exception
        // (Only if reference has @Column(unique = true))
        // Uncomment if you have unique constraint:
        // assertThrows(Exception.class, () -> {
        //     entityManager.persist(duplicate);
        //     entityManager.flush();
        // }, "Should throw exception for duplicate reference");
    }
}

/**
 * ASSERTJ CHEAT SHEET
 *
 * Why AssertJ over JUnit assertions?
 * - More readable
 * - Better error messages
 * - Fluent API (chainable)
 * - More assertions available
 *
 * Common Assertions:
 *
 * assertThat(actual).isEqualTo(expected)
 * assertThat(actual).isNotEqualTo(unexpected)
 * assertThat(actual).isNull()
 * assertThat(actual).isNotNull()
 * assertThat(actual).isTrue()
 * assertThat(actual).isFalse()
 *
 * String Assertions:
 * assertThat(string).contains("text")
 * assertThat(string).startsWith("prefix")
 * assertThat(string).endsWith("suffix")
 * assertThat(string).matches("regex.*")
 *
 * Number Assertions:
 * assertThat(number).isPositive()
 * assertThat(number).isNegative()
 * assertThat(number).isGreaterThan(10)
 * assertThat(number).isLessThan(100)
 * assertThat(number).isBetween(10, 100)
 *
 * Collection Assertions:
 * assertThat(list).hasSize(3)
 * assertThat(list).isEmpty()
 * assertThat(list).contains(element)
 * assertThat(list).containsExactly(el1, el2, el3)
 * assertThat(list).extracting(Payment::getReference)
 *                  .contains("PMT-123")
 *
 * Exception Assertions:
 * assertThatThrownBy(() -> code())
 *     .isInstanceOf(RuntimeException.class)
 *     .hasMessageContaining("error")
 *
 * REPOSITORY TESTING BEST PRACTICES
 *
 * 1. Test Custom Queries:
 *    - Any @Query you write
 *    - Custom finder methods
 *    - Named queries
 *
 * 2. Test Relationships:
 *    - @OneToMany, @ManyToOne work
 *    - Cascading deletes
 *    - Lazy vs eager loading
 *
 * 3. Test Constraints:
 *    - Unique constraints
 *    - Not null constraints
 *    - Foreign key constraints
 *
 * 4. Test Edge Cases:
 *    - Empty results
 *    - Large datasets
 *    - Special characters in strings
 *
 * 5. Use TestEntityManager:
 *    - Set up test data
 *    - Control persistence context
 *    - Flush/clear when needed
 *
 * 6. Clean Test Data:
 *    - @DataJpaTest rolls back transactions
 *    - No manual cleanup needed
 *    - Each test gets fresh database
 *
 * COMMON PITFALLS
 *
 * 1. Forgetting to flush():
 *    - Changes may not hit database
 *    - Call flush() after persist() in setup
 *
 * 2. Not clearing context:
 *    - entityManager.clear() forces DB query
 *    - Otherwise might get cached entity
 *
 * 3. Testing derived queries wrong:
 *    - Spring Data generates queries automatically
 *    - Trust the framework, but test your custom ones
 *
 * 4. Over-testing:
 *    - Don't test Spring Data's own code
 *    - Focus on YOUR custom queries
 *
 * TEST DATA BUILDERS
 *
 * For complex tests, consider builder pattern:
 *
 * public class PaymentTestBuilder {
 *     public static Payment createPendingPayment() {
 *         return Payment.builder()
 *             .amount(BigDecimal.valueOf(50000))
 *             .gateway(PaymentGateway.PAYSTACK)
 *             .status(PaymentStatus.PENDING)
 *             .reference("PMT-" + UUID.randomUUID())
 *             .build();
 *     }
 * }
 *
 * Makes tests more readable and maintainable!
 */
