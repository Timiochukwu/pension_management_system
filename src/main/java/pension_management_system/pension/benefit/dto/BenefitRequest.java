package pension_management_system.pension.benefit.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pension_management_system.pension.benefit.entity.BenefitType;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenefitRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Benefit type is required")
    private BenefitType benefitType;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    @NotBlank(message = "Payment method is required")
    @Size(max = 100, message = "Payment method cannot exceed 100 characters")
    private String paymentMethod;

    @NotBlank(message = "Account number is required")
    @Size(max = 100, message = "Account number cannot exceed 100 characters")
    private String accountNumber;

    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name cannot exceed 100 characters")
    private String bankName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate applicationDate;
}
