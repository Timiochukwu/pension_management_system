package pension_management_system.pension.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import pension_management_system.pension.analytics.dto.SystemStatisticsDto;
import pension_management_system.pension.analytics.service.AnalyticsService;
import pension_management_system.pension.notification.service.EmailService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnBean(name = "emailService")
public class MonthlyReportGenerationJob implements Job {

    private final AnalyticsService analyticsService;
    private final EmailService emailService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Starting Monthly Report Generation Job");

        try {
            SystemStatisticsDto statistics = analyticsService.getSystemStatistics();

            String reportPeriod = LocalDate.now().minusMonths(1)
                    .format(DateTimeFormatter.ofPattern("MMMM yyyy"));

            // Generate and send monthly report
            sendMonthlyReport(statistics, reportPeriod);

            log.info("Monthly Report Generation Job completed for period: {}", reportPeriod);
        } catch (Exception e) {
            log.error("Error executing Monthly Report Generation Job", e);
            throw new JobExecutionException(e);
        }
    }

    private void sendMonthlyReport(SystemStatisticsDto stats, String period) {
        emailService.sendEmail(
                pension_management_system.pension.notification.dto.EmailDto.builder()
                        .to("admin@pensionsystem.com")
                        .subject("Monthly System Report - " + period)
                        .template("MONTHLY_REPORT")
                        .templateData(Map.of(
                                "period", period,
                                "totalMembers", stats.getTotalMembers().toString(),
                                "activeMembers", stats.getActiveMembers().toString(),
                                "totalContributions", stats.getTotalContributions().toString(),
                                "totalContributionAmount", stats.getTotalContributionAmount().toString(),
                                "pendingBenefits", stats.getPendingBenefits().toString(),
                                "disbursedBenefits", stats.getDisbursedBenefits().toString()
                        ))
                        .build()
        );
    }
}
