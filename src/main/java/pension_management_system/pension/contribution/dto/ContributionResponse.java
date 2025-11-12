package pension_management_system.pension.contribution.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pension_management_system.pension.contribution.entity.ContributionStatus;
import pension_management_system.pension.contribution.entity.ContributionType;
import pension_management_system.pension.contribution.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContributionResponse {
    private Long id;
    private String referenceNumber;
    private Long memberId;
    private String memberName;
    private String memberBusinessId;
    private ContributionType contributionType;
    private BigDecimal contributionAmount;
    private LocalDate contributionDate;
    private PaymentMethod paymentMethod;
    private String description;
    private ContributionStatus status;
    private LocalDateTime processedAt;
    private String processedBy;
    private LocalDateTime createdAt;
}
