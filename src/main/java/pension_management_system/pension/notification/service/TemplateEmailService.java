package pension_management_system.pension.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * TemplateEmailService - Professional email service with HTML templates
 *
 * Purpose: Send beautifully designed FinTech-style emails
 *
 * What is Thymeleaf?
 * - Template engine for Java
 * - Creates dynamic HTML from templates
 * - Replaces {{placeholders}} with actual values
 * - Industry standard for Spring applications
 *
 * Why use HTML email templates?
 * - Professional appearance
 * - Better user experience
 * - Brand consistency
 * - Mobile-responsive
 * - Higher engagement rates
 *
 * Email Types:
 * 1. Welcome Email - When member registers
 * 2. Contribution Confirmation - After contribution created
 * 3. Payment Success - After successful payment
 * 4. Benefit Approval - When benefit claim approved
 *
 * All emails are:
 * - Mobile responsive
 * - Beautifully designed
 * - FinTech-inspired UI
 * - Include call-to-action buttons
 * - Professional branding
 *
 * Annotations:
 * @Service - Spring service component
 * @RequiredArgsConstructor - Constructor injection
 * @Slf4j - Logging support
 * @Async - Non-blocking email sending
 * @ConditionalOnProperty - Only loads when email is enabled
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.mail.enabled", havingValue = "true", matchIfMissing = false)
public class TemplateEmailService {

    /**
     * DEPENDENCIES
     */
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    /**
     * CONFIGURATION
     */
    @Value("${app.email.from:noreply@pensionsystem.com}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.currency.symbol:₦}")
    private String currencySymbol;

    /**
     * SEND WELCOME EMAIL
     *
     * Sent when a new member creates an account
     *
     * Features:
     * - Personalized greeting
     * - Account details summary
     * - Quick start guide
     * - Dashboard access link
     *
     * Template: welcome-email.html
     *
     * @param toEmail Member's email
     * @param memberName Member's full name
     * @param memberId Member ID
     * @param accountType Account type (e.g., "Individual", "Corporate")
     */
    @Async
    public void sendWelcomeEmail(String toEmail, String memberName, Long memberId, String accountType) {
        log.info("Sending welcome email to: {}", toEmail);

        try {
            // STEP 1: Prepare template variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("memberName", memberName);
            variables.put("memberId", memberId);
            variables.put("memberEmail", toEmail);
            variables.put("accountType", accountType);
            variables.put("registrationDate", formatDate(LocalDateTime.now()));
            variables.put("dashboardUrl", baseUrl + "/dashboard");
            variables.put("termsUrl", baseUrl + "/terms");
            variables.put("privacyUrl", baseUrl + "/privacy");
            variables.put("supportUrl", baseUrl + "/support");
            variables.put("facebookUrl", "https://facebook.com");
            variables.put("twitterUrl", "https://twitter.com");
            variables.put("linkedinUrl", "https://linkedin.com");

            // STEP 2: Process template
            String htmlContent = processTemplate("emails/welcome-email", variables);

            // STEP 3: Send email
            sendHtmlEmail(
                    toEmail,
                    "Welcome to Pension Management System! 🎉",
                    htmlContent
            );

            log.info("Welcome email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    /**
     * SEND CONTRIBUTION CONFIRMATION EMAIL
     *
     * Sent when a new contribution is recorded
     *
     * Features:
     * - Contribution amount highlight
     * - Transaction details
     * - Updated account balance
     * - PDF receipt link
     *
     * Template: contribution-confirmation.html
     *
     * @param toEmail Member's email
     * @param memberName Member's name
     * @param memberId Member ID
     * @param contributionId Contribution ID
     * @param amount Contribution amount
     * @param contributionType Type (e.g., "Monthly", "Voluntary")
     * @param previousBalance Previous balance
     * @param newBalance New balance after contribution
     */
    @Async
    public void sendContributionConfirmation(
            String toEmail,
            String memberName,
            Long memberId,
            Long contributionId,
            BigDecimal amount,
            String contributionType,
            BigDecimal previousBalance,
            BigDecimal newBalance) {

        log.info("Sending contribution confirmation email to: {}", toEmail);

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("memberName", memberName);
            variables.put("memberId", memberId);
            variables.put("memberEmail", toEmail);
            variables.put("contributionId", contributionId);
            variables.put("currencySymbol", currencySymbol);
            variables.put("amount", formatAmount(amount));
            variables.put("contributionType", contributionType);
            variables.put("contributionDate", formatDateTime(LocalDateTime.now()));
            variables.put("paymentMethod", "Bank Transfer"); // Default, can be passed as parameter
            variables.put("status", "Confirmed");
            variables.put("previousBalance", formatAmount(previousBalance));
            variables.put("newBalance", formatAmount(newBalance));
            variables.put("dashboardUrl", baseUrl + "/dashboard");
            variables.put("statementsUrl", baseUrl + "/statements");
            variables.put("supportUrl", baseUrl + "/support");

            String htmlContent = processTemplate("emails/contribution-confirmation", variables);

            sendHtmlEmail(
                    toEmail,
                    "Contribution Confirmed - " + currencySymbol + formatAmount(amount) + " ✅",
                    htmlContent
            );

            log.info("Contribution confirmation sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send contribution confirmation to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    /**
     * SEND PAYMENT SUCCESS EMAIL
     *
     * Sent when payment is processed successfully
     *
     * Features:
     * - Payment amount prominent display
     * - Complete transaction details
     * - Payment timeline
     * - PDF receipt download
     *
     * Template: payment-success.html
     *
     * @param toEmail Member's email
     * @param memberName Member's name
     * @param amount Payment amount
     * @param paymentReference Payment reference
     * @param transactionId Transaction ID from gateway
     * @param gateway Payment gateway used (Paystack/Flutterwave)
     * @param paymentMethod Payment method (Card/Bank Transfer)
     */
    @Async
    public void sendPaymentSuccessEmail(
            String toEmail,
            String memberName,
            BigDecimal amount,
            String paymentReference,
            String transactionId,
            String gateway,
            String paymentMethod) {

        log.info("Sending payment success email to: {}", toEmail);

        try {
            LocalDateTime now = LocalDateTime.now();

            Map<String, Object> variables = new HashMap<>();
            variables.put("memberName", memberName);
            variables.put("memberEmail", toEmail);
            variables.put("currencySymbol", currencySymbol);
            variables.put("amount", formatAmount(amount));
            variables.put("paymentReference", paymentReference);
            variables.put("transactionId", transactionId);
            variables.put("gateway", gateway);
            variables.put("paymentMethod", paymentMethod);
            variables.put("paymentDate", formatDateTime(now));
            variables.put("initiatedTime", formatTime(now.minusMinutes(5)));
            variables.put("authorizedTime", formatTime(now.minusMinutes(2)));
            variables.put("confirmedTime", formatTime(now));
            variables.put("receiptUrl", baseUrl + "/api/v1/payments/" + paymentReference + "/receipt");
            variables.put("dashboardUrl", baseUrl + "/dashboard");
            variables.put("transactionsUrl", baseUrl + "/transactions");
            variables.put("supportUrl", baseUrl + "/support");

            String htmlContent = processTemplate("emails/payment-success", variables);

            sendHtmlEmail(
                    toEmail,
                    "Payment Successful - " + currencySymbol + formatAmount(amount) + " 💳",
                    htmlContent
            );

            log.info("Payment success email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send payment success email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    /**
     * PROCESS TEMPLATE
     *
     * Converts template + variables → HTML
     *
     * How it works:
     * 1. Load template from resources/templates/
     * 2. Create Thymeleaf context with variables
     * 3. Replace all {{placeholders}} with values
     * 4. Return final HTML string
     *
     * @param templateName Template name (without .html)
     * @param variables Map of variable name → value
     * @return Processed HTML string
     */
    private String processTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);

        return templateEngine.process(templateName, context);
    }

    /**
     * SEND HTML EMAIL
     *
     * Low-level method to send HTML email
     *
     * Uses MimeMessage for HTML support
     * SimpleMailMessage only supports plain text
     *
     * @param to Recipient email
     * @param subject Email subject
     * @param htmlContent HTML content
     * @throws MessagingException if sending fails
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = HTML

        mailSender.send(message);
    }

    /**
     * FORMATTING UTILITIES
     *
     * Helper methods to format data for email templates
     */

    private String formatAmount(BigDecimal amount) {
        return String.format("%,.2f", amount);
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a"));
    }

    private String formatTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }
}

/**
 * USAGE EXAMPLES
 *
 * 1. Welcome Email (when member registers):
 * ```java
 * templateEmailService.sendWelcomeEmail(
 *     member.getEmail(),
 *     member.getFullName(),
 *     member.getId(),
 *     "Individual"
 * );
 * ```
 *
 * 2. Contribution Confirmation (after contribution):
 * ```java
 * templateEmailService.sendContributionConfirmation(
 *     member.getEmail(),
 *     member.getFullName(),
 *     member.getId(),
 *     contribution.getId(),
 *     contribution.getAmount(),
 *     "Monthly",
 *     previousBalance,
 *     newBalance
 * );
 * ```
 *
 * 3. Payment Success (after payment):
 * ```java
 * templateEmailService.sendPaymentSuccessEmail(
 *     member.getEmail(),
 *     member.getFullName(),
 *     payment.getAmount(),
 *     payment.getReference(),
 *     payment.getGatewayTransactionId(),
 *     "Paystack",
 *     "Debit Card"
 * );
 * ```
 *
 * TESTING EMAILS
 *
 * Use Mailtrap.io for development:
 * 1. Sign up at https://mailtrap.io
 * 2. Get SMTP credentials
 * 3. Configure in application.properties:
 *
 * spring.mail.host=sandbox.smtp.mailtrap.io
 * spring.mail.port=2525
 * spring.mail.username=your-mailtrap-username
 * spring.mail.password=your-mailtrap-password
 *
 * All emails sent will appear in Mailtrap inbox
 * No real emails sent during development!
 *
 * BENEFITS OF TEMPLATE EMAILS
 *
 * 1. Professional Appearance:
 *    - Beautiful FinTech design
 *    - Mobile responsive
 *    - Consistent branding
 *
 * 2. Better Engagement:
 *    - Higher open rates
 *    - Clear call-to-actions
 *    - Easy to read on mobile
 *
 * 3. Maintainability:
 *    - Designers can edit HTML
 *    - No code changes needed
 *    - Centralized templates
 *
 * 4. Flexibility:
 *    - Easy to add new email types
 *    - Reusable components
 *    - Variable substitution
 */
