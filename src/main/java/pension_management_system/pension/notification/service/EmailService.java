package pension_management_system.pension.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * EmailService - Send email notifications
 *
 * Purpose: Handle all email communications in the system
 *
 * What emails do we send?
 * - Payment confirmations
 * - Benefit approval notifications
 * - Monthly contribution reminders
 * - Report generation alerts
 * - Password reset emails
 *
 * Configuration required (application.properties):
 * spring.mail.host=smtp.gmail.com
 * spring.mail.port=587
 * spring.mail.username=your-email@gmail.com
 * spring.mail.password=your-app-password
 * spring.mail.properties.mail.smtp.auth=true
 * spring.mail.properties.mail.smtp.starttls.enable=true
 *
 * app.email.from=noreply@pension.com
 *
 * For Gmail:
 * 1. Enable 2-factor authentication
 * 2. Generate "App Password"
 * 3. Use app password in configuration
 *
 * Annotations:
 * @Service - Spring service component
 * @RequiredArgsConstructor - Lombok constructor injection
 * @Slf4j - Logging
 * @Async - Makes methods run asynchronously (don't block)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    /**
     * DEPENDENCIES
     *
     * JavaMailSender - Spring's email sending abstraction
     * Handles SMTP connection and email sending
     */
    private final JavaMailSender mailSender;

    /**
     * CONFIGURATION
     *
     * From email address (configured in properties)
     */
    @Value("${app.email.from:noreply@pension.com}")
    private String fromEmail;

    /**
     * SEND PAYMENT CONFIRMATION EMAIL
     *
     * Sent when payment is successful
     *
     * @Async - Runs in separate thread (non-blocking)
     * - User doesn't wait for email to send
     * - Better user experience
     * - Email failures don't block payment processing
     *
     * Email content:
     * Subject: Payment Confirmation - ₦10,000.00
     * Body:
     *   Dear Member,
     *
     *   Your payment of ₦10,000.00 has been received successfully.
     *
     *   Payment Reference: PMT-123456-ABC
     *   Date: 2025-01-15
     *   Amount: ₦10,000.00
     *   Status: Completed
     *
     *   Thank you for your contribution!
     *
     * @param toEmail Recipient email
     * @param memberName Member's name
     * @param amount Payment amount
     * @param reference Payment reference
     */
    @Async
    public void sendPaymentConfirmation(String toEmail, String memberName, String amount, String reference) {
        log.info("Sending payment confirmation email to: {}", toEmail);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Payment Confirmation - " + amount);
            message.setText(
                    String.format(
                            "Dear %s,\n\n" +
                                    "Your payment has been received successfully.\n\n" +
                                    "Payment Reference: %s\n" +
                                    "Amount: %s\n" +
                                    "Status: Completed\n\n" +
                                    "Thank you for your contribution!\n\n" +
                                    "Best regards,\n" +
                                    "Pension Management System",
                            memberName, reference, amount
                    )
            );

            mailSender.send(message);
            log.info("Payment confirmation email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send payment confirmation email: {}", e.getMessage(), e);
            // Don't throw - email failures shouldn't break payment flow
        }
    }

    /**
     * SEND BENEFIT APPROVAL EMAIL
     *
     * Sent when benefit claim is approved
     *
     * @param toEmail Recipient email
     * @param memberName Member's name
     * @param benefitType Type of benefit (RETIREMENT, DEATH, etc.)
     * @param amount Approved amount
     */
    @Async
    public void sendBenefitApprovalNotification(
            String toEmail, String memberName, String benefitType, String amount) {

        log.info("Sending benefit approval email to: {}", toEmail);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Benefit Claim Approved - " + benefitType);
            message.setText(
                    String.format(
                            "Dear %s,\n\n" +
                                    "Your %s benefit claim has been approved.\n\n" +
                                    "Approved Amount: %s\n\n" +
                                    "The payment will be processed shortly.\n\n" +
                                    "Best regards,\n" +
                                    "Pension Management System",
                            memberName, benefitType, amount
                    )
            );

            mailSender.send(message);
            log.info("Benefit approval email sent successfully");

        } catch (Exception e) {
            log.error("Failed to send benefit approval email: {}", e.getMessage(), e);
        }
    }

    /**
     * SEND REPORT READY EMAIL
     *
     * Sent when requested report is ready for download
     *
     * @param toEmail Recipient email
     * @param reportTitle Report title
     * @param downloadUrl URL to download report
     */
    @Async
    public void sendReportReadyNotification(String toEmail, String reportTitle, String downloadUrl) {
        log.info("Sending report ready email to: {}", toEmail);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Report Ready - " + reportTitle);
            message.setText(
                    String.format(
                            "Your requested report is ready for download.\n\n" +
                                    "Report: %s\n\n" +
                                    "Download: %s\n\n" +
                                    "This link will expire in 7 days.\n\n" +
                                    "Best regards,\n" +
                                    "Pension Management System",
                            reportTitle, downloadUrl
                    )
            );

            mailSender.send(message);
            log.info("Report ready email sent successfully");

        } catch (Exception e) {
            log.error("Failed to send report ready email: {}", e.getMessage(), e);
        }
    }

    /**
     * SEND HTML EMAIL
     *
     * For richer formatting with HTML
     *
     * @param toEmail Recipient
     * @param subject Email subject
     * @param htmlContent HTML content
     */
    @Async
    public void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        log.info("Sending HTML email to: {}", toEmail);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(message);
            log.info("HTML email sent successfully");

        } catch (Exception e) {
            log.error("Failed to send HTML email: {}", e.getMessage(), e);
        }
    }

    /**
     * SEND MONTHLY REMINDER
     *
     * Reminder for monthly contribution
     *
     * @param toEmail Recipient
     * @param memberName Member name
     * @param dueAmount Amount due
     */
    @Async
    public void sendMonthlyContributionReminder(String toEmail, String memberName, String dueAmount) {
        log.info("Sending monthly reminder to: {}", toEmail);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Monthly Contribution Reminder");
            message.setText(
                    String.format(
                            "Dear %s,\n\n" +
                                    "This is a reminder that your monthly contribution is due.\n\n" +
                                    "Amount Due: %s\n\n" +
                                    "Please make payment at your earliest convenience.\n\n" +
                                    "Best regards,\n" +
                                    "Pension Management System",
                            memberName, dueAmount
                    )
            );

            mailSender.send(message);
            log.info("Monthly reminder sent successfully");

        } catch (Exception e) {
            log.error("Failed to send monthly reminder: {}", e.getMessage(), e);
        }
    }
}

/**
 * USAGE EXAMPLES:
 *
 * 1. In PaymentService after successful payment:
 *
 * @Autowired
 * private EmailService emailService;
 *
 * public void processPayment(Payment payment) {
 *     // Process payment...
 *
 *     if (payment.isSuccessful()) {
 *         emailService.sendPaymentConfirmation(
 *             member.getEmail(),
 *             member.getFullName(),
 *             "₦" + payment.getAmount(),
 *             payment.getReference()
 *         );
 *     }
 * }
 *
 * 2. In BenefitService after approval:
 *
 * public void approveBenefit(Long benefitId) {
 *     Benefit benefit = benefitRepository.findById(benefitId);
 *     benefit.approve();
 *
 *     emailService.sendBenefitApprovalNotification(
 *         benefit.getMember().getEmail(),
 *         benefit.getMember().getFullName(),
 *         benefit.getBenefitType().name(),
 *         "₦" + benefit.getApprovedAmount()
 *     );
 * }
 *
 * EMAIL TEMPLATES:
 *
 * For production, use HTML templates with Thymeleaf:
 *
 * 1. Create template: resources/templates/email/payment-confirmation.html
 *
 * <!DOCTYPE html>
 * <html xmlns:th="http://www.thymeleaf.org">
 * <body>
 *     <h1>Payment Confirmation</h1>
 *     <p>Dear <span th:text="${memberName}"></span>,</p>
 *     <p>Amount: <span th:text="${amount}"></span></p>
 * </body>
 * </html>
 *
 * 2. Use TemplateEngine to render:
 *
 * @Autowired
 * private TemplateEngine templateEngine;
 *
 * Context context = new Context();
 * context.setVariable("memberName", "John Doe");
 * context.setVariable("amount", "₦10,000");
 * String htmlContent = templateEngine.process("email/payment-confirmation", context);
 * sendHtmlEmail(email, subject, htmlContent);
 *
 * TESTING:
 *
 * For development, use Mailtrap (https://mailtrap.io):
 * - Catches all emails (doesn't send to real recipients)
 * - View emails in web interface
 * - Test email layout and content
 *
 * Configuration for Mailtrap:
 * spring.mail.host=smtp.mailtrap.io
 * spring.mail.port=2525
 * spring.mail.username=your_mailtrap_username
 * spring.mail.password=your_mailtrap_password
 */
