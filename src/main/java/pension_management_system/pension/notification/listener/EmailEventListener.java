package pension_management_system.pension.notification.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pension_management_system.pension.notification.event.ContributionCreatedEvent;
import pension_management_system.pension.notification.event.MemberRegisteredEvent;
import pension_management_system.pension.notification.event.PaymentSuccessEvent;
import pension_management_system.pension.notification.service.TemplateEmailService;

/**
 * EmailEventListener - Listens for application events and sends emails
 *
 * Purpose: Automatically send emails when important events occur
 *
 * What is an Event Listener?
 * - Component that reacts to application events
 * - Triggered automatically by Spring
 * - Decoupled from business logic
 * - Can be async for better performance
 *
 * How it works:
 * 1. Service publishes event (e.g., MemberRegisteredEvent)
 * 2. Spring finds all @EventListener methods
 * 3. Calls matching listener method
 * 4. Listener sends email
 *
 * Benefits:
 * - Services don't know about email logic
 * - Easy to add new notifications
 * - Can have multiple listeners for same event
 * - Non-blocking (async)
 *
 * Annotations:
 * @Component - Spring component (scanned and instantiated)
 * @RequiredArgsConstructor - Lombok constructor injection
 * @Slf4j - Logging support
 * @EventListener - Marks method as event listener
 * @Async - Runs in separate thread (non-blocking)
 *
 * Example Flow:
 *
 * MemberService          Spring          EmailEventListener
 *      |                    |                     |
 *      | publish event      |                     |
 *      |------------------->|                     |
 *      |                    | call listener       |
 *      |                    |-------------------->|
 *      |                    |                     | send email
 *      |                    |                     |----------->
 *      | return             |                     |
 *      |<-------------------|                     |
 *      |                    |                     |
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailEventListener {

    /**
     * DEPENDENCY
     *
     * Email service with beautiful HTML templates
     */
    private final TemplateEmailService templateEmailService;

    /**
     * HANDLE MEMBER REGISTRATION EVENT
     *
     * Triggered when: New member creates account
     * Action: Send welcome email
     *
     * @EventListener - Automatically called when event published
     * @Async - Runs in background thread
     *   - Registration API responds immediately
     *   - Email sends in background
     *   - User doesn't wait for email
     *
     * Event Flow:
     * 1. Member submits registration form
     * 2. MemberService creates account
     * 3. MemberService publishes MemberRegisteredEvent
     * 4. This method receives event (async)
     * 5. Sends welcome email
     * 6. User sees success message (email sending in background)
     *
     * Why async?
     * - Faster response to user
     * - Email failures don't block registration
     * - Better user experience
     * - Scalable (can queue many emails)
     *
     * @param event Member registration event with member details
     */
    @EventListener
    @Async
    public void handleMemberRegistered(MemberRegisteredEvent event) {
        log.info("Received MemberRegisteredEvent for member: {}", event.getMemberEmail());

        try {
            // Send professional welcome email with FinTech design
            templateEmailService.sendWelcomeEmail(
                    event.getMemberEmail(),
                    event.getMemberName(),
                    event.getMemberId(),
                    event.getAccountType()
            );

            log.info("Welcome email queued successfully for: {}", event.getMemberEmail());

        } catch (Exception e) {
            // Log error but don't throw
            // Email failure shouldn't break registration
            log.error("Failed to send welcome email to {}: {}",
                    event.getMemberEmail(), e.getMessage(), e);
        }
    }

    /**
     * HANDLE CONTRIBUTION CREATED EVENT
     *
     * Triggered when: New contribution recorded
     * Action: Send contribution confirmation email
     *
     * Email includes:
     * - Contribution amount
     * - Transaction details
     * - Previous and new balance
     * - Link to view statement
     *
     * @param event Contribution creation event with contribution details
     */
    @EventListener
    @Async
    public void handleContributionCreated(ContributionCreatedEvent event) {
        log.info("Received ContributionCreatedEvent for contribution: {}", event.getContributionId());

        try {
            templateEmailService.sendContributionConfirmation(
                    event.getMemberEmail(),
                    event.getMemberName(),
                    event.getMemberId(),
                    event.getContributionId(),
                    event.getAmount(),
                    event.getContributionType(),
                    event.getPreviousBalance(),
                    event.getNewBalance()
            );

            log.info("Contribution confirmation email queued for: {}", event.getMemberEmail());

        } catch (Exception e) {
            log.error("Failed to send contribution confirmation to {}: {}",
                    event.getMemberEmail(), e.getMessage(), e);
        }
    }

    /**
     * HANDLE PAYMENT SUCCESS EVENT
     *
     * Triggered when: Payment successfully processed
     * Action: Send payment success email
     *
     * Email includes:
     * - Payment amount
     * - Payment reference
     * - Transaction timeline
     * - PDF receipt download link
     *
     * This is different from contribution confirmation:
     * - Contribution = internal record created
     * - Payment = actual money transferred
     *
     * Flow:
     * 1. User initiates payment
     * 2. Payment gateway processes payment
     * 3. Webhook confirms payment
     * 4. PaymentService publishes PaymentSuccessEvent
     * 5. This method sends confirmation email
     *
     * @param event Payment success event with payment details
     */
    @EventListener
    @Async
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("Received PaymentSuccessEvent for reference: {}", event.getPaymentReference());

        try {
            templateEmailService.sendPaymentSuccessEmail(
                    event.getMemberEmail(),
                    event.getMemberName(),
                    event.getAmount(),
                    event.getPaymentReference(),
                    event.getTransactionId(),
                    event.getGateway(),
                    event.getPaymentMethod()
            );

            log.info("Payment success email queued for: {}", event.getMemberEmail());

        } catch (Exception e) {
            log.error("Failed to send payment success email to {}: {}",
                    event.getMemberEmail(), e.getMessage(), e);
        }
    }
}

/**
 * ADDING MORE EVENT LISTENERS
 *
 * To add new email notifications:
 *
 * 1. Create Event class:
 * ```java
 * public class BenefitApprovedEvent extends ApplicationEvent {
 *     private final String memberEmail;
 *     private final String benefitType;
 *     // ... constructor and getters
 * }
 * ```
 *
 * 2. Add listener method here:
 * ```java
 * @EventListener
 * @Async
 * public void handleBenefitApproved(BenefitApprovedEvent event) {
 *     templateEmailService.sendBenefitApprovalEmail(
 *         event.getMemberEmail(),
 *         event.getBenefitType()
 *     );
 * }
 * ```
 *
 * 3. Publish event in service:
 * ```java
 * eventPublisher.publishEvent(new BenefitApprovedEvent(
 *     this, member.getEmail(), benefit.getType()
 * ));
 * ```
 *
 * That's it! No changes to existing code needed.
 *
 * TESTING EVENT LISTENERS
 *
 * Unit test example:
 * ```java
 * @Test
 * void shouldSendWelcomeEmailOnMemberRegistered() {
 *     // Arrange
 *     MemberRegisteredEvent event = new MemberRegisteredEvent(
 *         this, 1L, "John Doe", "john@example.com", "Individual"
 *     );
 *
 *     // Act
 *     emailEventListener.handleMemberRegistered(event);
 *
 *     // Assert
 *     verify(templateEmailService).sendWelcomeEmail(
 *         eq("john@example.com"),
 *         eq("John Doe"),
 *         eq(1L),
 *         eq("Individual")
 *     );
 * }
 * ```
 *
 * Integration test:
 * ```java
 * @SpringBootTest
 * public class EmailIntegrationTest {
 *
 *     @Autowired
 *     private ApplicationEventPublisher eventPublisher;
 *
 *     @Autowired
 *     private JavaMailSender mailSender; // Mock this
 *
 *     @Test
 *     void shouldSendEmailWhenEventPublished() {
 *         // Publish event
 *         eventPublisher.publishEvent(new MemberRegisteredEvent(...));
 *
 *         // Wait for async processing
 *         await().atMost(5, SECONDS)
 *             .untilAsserted(() -> {
 *                 // Verify email was sent
 *                 verify(mailSender).send(any(MimeMessage.class));
 *             });
 *     }
 * }
 * ```
 *
 * ERROR HANDLING
 *
 * Notice we catch exceptions and log them but don't rethrow.
 * Why?
 *
 * - Email failure shouldn't break business logic
 * - If registration fails because email fails, that's bad UX
 * - Better to log error and retry later
 * - Can implement retry mechanism with DLQ (Dead Letter Queue)
 *
 * Retry mechanism example:
 * ```java
 * @Retryable(
 *     value = {MessagingException.class},
 *     maxAttempts = 3,
 *     backoff = @Backoff(delay = 2000)
 * )
 * public void handleMemberRegistered(MemberRegisteredEvent event) {
 *     // Will retry 3 times with 2-second delay
 *     templateEmailService.sendWelcomeEmail(...);
 * }
 * ```
 */
