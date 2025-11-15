package pension_management_system.pension.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import pension_management_system.pension.contribution.entity.ContributionType;
import pension_management_system.pension.contribution.repository.ContributionRepository;
import pension_management_system.pension.member.entity.Member;
import pension_management_system.pension.member.entity.MemberStatus;
import pension_management_system.pension.member.repository.MemberRepository;
import pension_management_system.pension.notification.service.EmailService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContributionReminderJob implements Job {

    private final MemberRepository memberRepository;
    private final ContributionRepository contributionRepository;
    private final EmailService emailService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Starting Contribution Reminder Job");

        try {
            List<Member> activeMembers = memberRepository.findByMemberStatus(MemberStatus.ACTIVE);
            int year = LocalDate.now().getYear();
            int month = LocalDate.now().getMonthValue();

            int remindersSent = 0;

            for (Member member : activeMembers) {
                // Check if member has made monthly contribution this month
                boolean hasContribution = contributionRepository
                        .findMonthlyContributionByMemberAndYearMonth(member, ContributionType.MONTHLY, year, month)
                        .isPresent();

                if (!hasContribution) {
                    sendContributionReminder(member);
                    remindersSent++;
                }
            }

            log.info("Contribution Reminder Job completed. Sent {} reminders", remindersSent);
        } catch (Exception e) {
            log.error("Error executing Contribution Reminder Job", e);
            throw new JobExecutionException(e);
        }
    }

    private void sendContributionReminder(Member member) {
        emailService.sendEmail(
                pension_management_system.pension.notification.dto.EmailDto.builder()
                        .to(member.getEmail())
                        .subject("Monthly Contribution Reminder")
                        .template("CONTRIBUTION_REMINDER")
                        .templateData(Map.of(
                                "memberName", member.getFirstName() + " " + member.getLastName(),
                                "memberId", member.getMemberId()
                        ))
                        .build()
        );
    }
}
