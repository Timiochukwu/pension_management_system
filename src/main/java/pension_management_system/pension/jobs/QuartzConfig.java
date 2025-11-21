package pension_management_system.pension.jobs;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail contributionReminderJobDetail() {
        return JobBuilder.newJob(ContributionReminderJob.class)
                .withIdentity("contributionReminderJob")
                .withDescription("Send contribution reminders to members")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger contributionReminderTrigger(JobDetail contributionReminderJobDetail) {
        // Run every month on the 1st at 9:00 AM
        return TriggerBuilder.newTrigger()
                .forJob(contributionReminderJobDetail)
                .withIdentity("contributionReminderTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 9 1 * ?"))
                .build();
    }

    @Bean
    public JobDetail retirementEligibilityJobDetail() {
        return JobBuilder.newJob(RetirementEligibilityCheckJob.class)
                .withIdentity("retirementEligibilityJob")
                .withDescription("Check members approaching retirement age")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger retirementEligibilityTrigger(JobDetail retirementEligibilityJobDetail) {
        // Run daily at 2:00 AM
        return TriggerBuilder.newTrigger()
                .forJob(retirementEligibilityJobDetail)
                .withIdentity("retirementEligibilityTrigger")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(2, 0))
                .build();
    }

    @Bean
    public JobDetail pendingBenefitReminderJobDetail() {
        return JobBuilder.newJob(PendingBenefitReminderJob.class)
                .withIdentity("pendingBenefitReminderJob")
                .withDescription("Send reminders for pending benefits")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger pendingBenefitReminderTrigger(JobDetail pendingBenefitReminderJobDetail) {
        // Run every week on Monday at 10:00 AM
        return TriggerBuilder.newTrigger()
                .forJob(pendingBenefitReminderJobDetail)
                .withIdentity("pendingBenefitReminderTrigger")
                .withSchedule(CronScheduleBuilder.weeklyOnDayAndHourAndMinute(DateBuilder.MONDAY, 10, 0))
                .build();
    }

    @Bean
    public JobDetail monthlyReportJobDetail() {
        return JobBuilder.newJob(MonthlyReportGenerationJob.class)
                .withIdentity("monthlyReportJob")
                .withDescription("Generate monthly system reports")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger monthlyReportTrigger(JobDetail monthlyReportJobDetail) {
        // Run on the last day of every month at 11:59 PM
        return TriggerBuilder.newTrigger()
                .forJob(monthlyReportJobDetail)
                .withIdentity("monthlyReportTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 59 23 L * ?"))
                .build();
    }
}
