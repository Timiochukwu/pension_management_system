package pension_management_system.pension.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
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
    private List<PaymentMethodData> paymentMethods;

    /**
     * Inner class representing payment method data with count, total amount and percentage
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethodData {
        private String paymentMethod;
        private long count;
        private BigDecimal totalAmount;
        private BigDecimal percentage;
    }
}
