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
public class MemberAnalyticsDto {
    private String memberId;
    private String memberName;
    private Integer yearsOfService;
    private Long totalContributions;
    private BigDecimal totalMonthlyContributions;
    private BigDecimal totalVoluntaryContributions;
    private BigDecimal totalContributionAmount;
    private BigDecimal estimatedBenefit;
    private String memberStatus;
}
