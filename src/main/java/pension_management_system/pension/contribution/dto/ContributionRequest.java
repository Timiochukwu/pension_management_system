package pension_management_system.pension.contribution.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pension_management_system.pension.contribution.entity.ContributionType;
import pension_management_system.pension.contribution.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContributionRequest {
    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Contribution type is required")
    private ContributionType contributionType;

    @NotNull(message = "Contribution amount is requires")
    @DecimalMin(value = "100", message = "Amount must be greater or equals to 100")
    private BigDecimal contributionAmount;

    @NotNull(message = "Contribution date is required")
    @PastOrPresent(message = "Contribution date cannot be in the future")
    private LocalDate contributionDate;

    private PaymentMethod paymentMethod;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
