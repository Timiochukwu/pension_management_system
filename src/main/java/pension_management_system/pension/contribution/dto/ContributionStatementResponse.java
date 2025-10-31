package pension_management_system.pension.contribution.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContributionStatementResponse {
    private String memberId;
    private String memberName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate generatedDate;
    private List<ContributionResponse> contributions;
    private Integer numberOfContributions;
    private BigDecimal totalMonthlyContribution;
    private BigDecimal totalVoluntaryContribution;
    private BigDecimal grandTotal;

}
