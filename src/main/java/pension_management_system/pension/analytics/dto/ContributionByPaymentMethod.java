package pension_management_system.pension.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Response DTO for contributions grouped by payment method
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContributionByPaymentMethod {
    private Map<String, BigDecimal> amountByMethod;
    private Map<String, Long> countByMethod;
}
