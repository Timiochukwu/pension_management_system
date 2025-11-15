package pension_management_system.pension.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContributionByPaymentMethod {
    private List<PaymentMethodData> paymentMethods;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethodData {
        private String paymentMethod;
        private Long count;
        private BigDecimal totalAmount;
        private Double percentage;
    }
}
