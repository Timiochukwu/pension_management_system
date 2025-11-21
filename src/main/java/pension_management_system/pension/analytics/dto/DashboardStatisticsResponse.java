package pension_management_system.pension.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for dashboard statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsResponse {
    private long totalMembers;
    private long activeMembers;
    private long inactiveMembers;
    private long totalContributions;
    private BigDecimal totalContributionAmount;
    private long totalPayments;
    private BigDecimal monthlyContributionAmount;
    private BigDecimal voluntaryContributionAmount;
    private long pendingContributions;
    private long completedContributions;
    private long failedContributions;
    private long pendingBenefits;
    private long approvedBenefits;
    private BigDecimal totalBenefitsPaid;
    private long totalEmployers;
}
