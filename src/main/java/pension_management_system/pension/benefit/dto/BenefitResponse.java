package pension_management_system.pension.benefit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pension_management_system.pension.benefit.entity.BenefitStatus;
import pension_management_system.pension.benefit.entity.BenefitType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BenefitResponse {

    private Long id;
    private String referenceNumber;
    private Long memberId;
    private String memberName;
    private String memberIdNumber;
    private BenefitType benefitType;
    private BenefitStatus status;
    private BigDecimal totalContributions;
    private BigDecimal employerContributions;
    private BigDecimal investmentReturns;
    private BigDecimal calculatedBenefit;
    private BigDecimal taxDeductions;
    private BigDecimal administrativeFees;
    private BigDecimal netPayable;
    private LocalDate applicationDate;
    private LocalDate approvalDate;
    private LocalDate disbursementDate;
    private String approvedBy;
    private String disbursedBy;
    private String remarks;
    private String rejectionReason;
    private String paymentMethod;
    private String accountNumber;
    private String bankName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
