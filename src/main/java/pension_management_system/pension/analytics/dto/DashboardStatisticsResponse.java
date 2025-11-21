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
    private long totalContributions;
    private BigDecimal totalContributionAmount;
    private long pendingBenefits;
    private long approvedBenefits;
    private BigDecimal totalBenefitsPaid;
    private long totalEmployers;
}
