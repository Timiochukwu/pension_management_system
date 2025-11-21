package pension_management_system.pension.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import pension_management_system.pension.member.entity.Member;
import pension_management_system.pension.member.repository.MemberRepository;
import pension_management_system.pension.notification.service.EmailService;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RetirementEligibilityCheckJob implements Job {

    private final MemberRepository memberRepository;
    private final EmailService emailService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Starting Retirement Eligibility Check Job");

        try {
            List<Member> eligibleMembers = memberRepository.findMembersEligibleForRetirement();
            int notificationsSent = 0;

            for (Member member : eligibleMembers) {
                int age = Period.between(member.getDateOfBirth(), LocalDate.now()).getYears();

                // Notify members who are exactly 59 (one year before retirement)
                if (age == 59) {
                    sendRetirementNotification(member, age);
                    notificationsSent++;
                }

                // Also notify those who just turned 60
                if (age == 60) {
                    sendRetirementEligibilityNotification(member);
                    notificationsSent++;
                }
            }

            log.info("Retirement Eligibility Check Job completed. Sent {} notifications", notificationsSent);
        } catch (Exception e) {
            log.error("Error executing Retirement Eligibility Check Job", e);
            throw new JobExecutionException(e);
        }
    }

    private void sendRetirementNotification(Member member, int age) {
        emailService.sendEmail(
                pension_management_system.pension.notification.dto.EmailDto.builder()
                        .to(member.getEmail())
                        .subject("Approaching Retirement Age")
                        .template("RETIREMENT_APPROACHING")
                        .templateData(Map.of(
                                "memberName", member.getFirstName() + " " + member.getLastName(),
                                "age", String.valueOf(age),
                                "yearsToRetirement", "1"
                        ))
                        .build()
        );
    }

    private void sendRetirementEligibilityNotification(Member member) {
        emailService.sendEmail(
                pension_management_system.pension.notification.dto.EmailDto.builder()
                        .to(member.getEmail())
                        .subject("You Are Now Eligible for Retirement Benefits")
                        .template("RETIREMENT_ELIGIBLE")
                        .templateData(Map.of(
                                "memberName", member.getFirstName() + " " + member.getLastName(),
                                "memberId", member.getMemberId()
                        ))
                        .build()
        );
    }
}
