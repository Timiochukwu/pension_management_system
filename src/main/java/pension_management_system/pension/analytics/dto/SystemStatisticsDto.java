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
public class SystemStatisticsDto {
    private Long totalMembers;
    private Long activeMembers;
    private Long retiredMembers;
    private Long totalContributions;
    private BigDecimal totalContributionAmount;
    private Long totalBenefits;
    private Long pendingBenefits;
    private Long approvedBenefits;
    private Long disbursedBenefits;
    private BigDecimal totalBenefitsAmount;
    private Long totalEmployers;
    private Long activeEmployers;
}
