package pension_management_system.pension.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsResponse {
    private Long totalMembers;
    private Long activeMembers;
    private Long inactiveMembers;
    private Long totalEmployers;
    private Long totalContributions;
    private BigDecimal totalContributionAmount;
    private BigDecimal monthlyContributionAmount;
    private BigDecimal voluntaryContributionAmount;
    private Long pendingContributions;
    private Long completedContributions;
    private Long failedContributions;
}
