package pension_management_system.pension.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pension_management_system.pension.notification.dto.EmailDto;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendEmail(EmailDto emailDto) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailDto.getTo());
            message.setSubject(emailDto.getSubject());
            message.setText(buildEmailBody(emailDto.getTemplate(), emailDto.getTemplateData()));

            mailSender.send(message);
            log.info("Email sent successfully to: {}", emailDto.getTo());
        } catch (Exception e) {
            log.error("Failed to send email to: {}, error: {}", emailDto.getTo(), e.getMessage());
        }
    }

    @Async
    public void sendMemberRegistrationEmail(String email, String memberName, String memberId) {
        EmailDto emailDto = EmailDto.builder()
                .to(email)
                .subject("Welcome to Pension Management System")
                .template("MEMBER_REGISTRATION")
                .templateData(Map.of(
                        "memberName", memberName,
                        "memberId", memberId
                ))
                .build();

        sendEmail(emailDto);
    }

    @Async
    public void sendContributionConfirmationEmail(String email, String memberName,
                                                    String referenceNumber, String amount) {
        EmailDto emailDto = EmailDto.builder()
                .to(email)
                .subject("Contribution Confirmation")
                .template("CONTRIBUTION_CONFIRMATION")
                .templateData(Map.of(
                        "memberName", memberName,
                        "referenceNumber", referenceNumber,
                        "amount", amount
                ))
                .build();

        sendEmail(emailDto);
    }

    @Async
    public void sendBenefitApplicationEmail(String email, String memberName,
                                             String referenceNumber, String benefitType) {
        EmailDto emailDto = EmailDto.builder()
                .to(email)
                .subject("Benefit Application Received")
                .template("BENEFIT_APPLICATION")
                .templateData(Map.of(
                        "memberName", memberName,
                        "referenceNumber", referenceNumber,
                        "benefitType", benefitType
                ))
                .build();

        sendEmail(emailDto);
    }

    @Async
    public void sendBenefitApprovalEmail(String email, String memberName,
                                          String referenceNumber, String amount) {
        EmailDto emailDto = EmailDto.builder()
                .to(email)
                .subject("Benefit Application Approved")
                .template("BENEFIT_APPROVAL")
                .templateData(Map.of(
                        "memberName", memberName,
                        "referenceNumber", referenceNumber,
                        "amount", amount
                ))
                .build();

        sendEmail(emailDto);
    }

    private String buildEmailBody(String template, Map<String, Object> data) {
        // In a real system, you would use a templating engine like Thymeleaf or FreeMarker
        // For now, we'll use simple string formatting

        return switch (template) {
            case "MEMBER_REGISTRATION" ->
                    String.format("""
                    Dear %s,

                    Welcome to the Pension Management System!

                    Your registration has been completed successfully.
                    Your Member ID is: %s

                    You can now start making contributions to your pension account.

                    Best regards,
                    Pension Management System
                    """, data.get("memberName"), data.get("memberId"));

            case "CONTRIBUTION_CONFIRMATION" ->
                    String.format("""
                    Dear %s,

                    Your contribution has been received and processed successfully.

                    Reference Number: %s
                    Amount: %s

                    Thank you for your contribution!

                    Best regards,
                    Pension Management System
                    """, data.get("memberName"), data.get("referenceNumber"), data.get("amount"));

            case "BENEFIT_APPLICATION" ->
                    String.format("""
                    Dear %s,

                    We have received your benefit application.

                    Reference Number: %s
                    Benefit Type: %s

                    Your application is now being processed. We will notify you once it has been reviewed.

                    Best regards,
                    Pension Management System
                    """, data.get("memberName"), data.get("referenceNumber"), data.get("benefitType"));

            case "BENEFIT_APPROVAL" ->
                    String.format("""
                    Dear %s,

                    Great news! Your benefit application has been approved.

                    Reference Number: %s
                    Approved Amount: %s

                    The disbursement will be processed shortly.

                    Best regards,
                    Pension Management System
                    """, data.get("memberName"), data.get("referenceNumber"), data.get("amount"));

            default -> "Email notification";
        };
    }
}
