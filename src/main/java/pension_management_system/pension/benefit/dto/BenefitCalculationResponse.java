package pension_management_system.pension.benefit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenefitCalculationResponse {

    private Long memberId;
    private String memberName;
    private Integer yearsOfService;
    private Integer monthsOfService;
    private BigDecimal totalMonthlyContributions;
    private BigDecimal totalVoluntaryContributions;
    private BigDecimal totalContributions;
    private BigDecimal estimatedEmployerContributions;
    private BigDecimal estimatedInvestmentReturns;
    private BigDecimal grossBenefit;
    private BigDecimal estimatedTax;
    private BigDecimal estimatedAdminFees;
    private BigDecimal estimatedNetBenefit;
    private String eligibilityStatus;
    private String eligibilityMessage;
}
