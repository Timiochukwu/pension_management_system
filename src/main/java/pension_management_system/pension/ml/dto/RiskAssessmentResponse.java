package pension_management_system.pension.ml.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RiskAssessmentResponse - Member risk profile
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessmentResponse {
    private Long memberId;
    private Integer riskScore; // 300-850 (like credit score)
    private String riskCategory; // EXCELLENT, GOOD, FAIR, POOR, VERY_POOR
    private Double defaultProbability; // Probability of default (0.0 to 1.0)
    private String recommendation; // APPROVE, MANUAL_REVIEW, REJECT
    private Integer contributionConsistencyScore; // 0-100
    private Integer paymentHistoryScore; // 0-100
    private Integer accountAgeScore; // 0-100
}
