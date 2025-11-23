package pension_management_system.pension.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import pension_management_system.pension.benefit.entity.Benefit;
import pension_management_system.pension.benefit.entity.BenefitStatus;
import pension_management_system.pension.benefit.repository.BenefitRepository;
import pension_management_system.pension.notification.service.EmailService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnBean(name = "emailService")
public class PendingBenefitReminderJob implements Job {

    private final BenefitRepository benefitRepository;
    private final EmailService emailService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Starting Pending Benefit Reminder Job");

        try {
            List<Benefit> pendingBenefits = benefitRepository.findByStatus(BenefitStatus.PENDING);
            int remindersSent = 0;

            for (Benefit benefit : pendingBenefits) {
                long daysPending = ChronoUnit.DAYS.between(benefit.getApplicationDate(), LocalDate.now());

                // Send reminder for benefits pending more than 7 days
                if (daysPending > 7) {
                    sendAdminReminder(benefit, daysPending);
                    remindersSent++;
                }
            }

            log.info("Pending Benefit Reminder Job completed. Sent {} reminders", remindersSent);
        } catch (Exception e) {
            log.error("Error executing Pending Benefit Reminder Job", e);
            throw new JobExecutionException(e);
        }
    }

    private void sendAdminReminder(Benefit benefit, long daysPending) {
        // In a real system, this would send to admin email
        log.warn("Benefit {} has been pending for {} days - Member: {}",
                benefit.getReferenceNumber(),
                daysPending,
                benefit.getMember().getMemberId());

        emailService.sendEmail(
                pension_management_system.pension.notification.dto.EmailDto.builder()
                        .to("admin@pensionsystem.com") // Admin email
                        .subject("Pending Benefit Alert - " + benefit.getReferenceNumber())
                        .template("PENDING_BENEFIT_REMINDER")
                        .templateData(Map.of(
                                "referenceNumber", benefit.getReferenceNumber(),
                                "memberName", benefit.getMember().getFirstName() + " " + benefit.getMember().getLastName(),
                                "daysPending", String.valueOf(daysPending),
                                "benefitType", benefit.getBenefitType().toString()
                        ))
                        .build()
        );
    }
}
