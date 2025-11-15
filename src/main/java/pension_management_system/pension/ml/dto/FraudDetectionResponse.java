package pension_management_system.pension.ml.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * FraudDetectionResponse - Output from fraud detection ML model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudDetectionResponse {
    private Double fraudScore; // 0.0 to 1.0 (0 = safe, 1 = fraudulent)
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private Boolean isFraudulent; // true if score > threshold
    private List<String> riskFactors; // Reasons for high score
    private String recommendation; // APPROVE, REVIEW, REJECT, REQUIRE_2FA
    private Double confidence; // Model confidence (0.0 to 1.0)
}
