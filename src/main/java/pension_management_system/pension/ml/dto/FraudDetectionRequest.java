package pension_management_system.pension.ml.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * FraudDetectionRequest - Input data for fraud detection ML model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudDetectionRequest {
    private Long memberId;
    private BigDecimal amount;
    private String paymentMethod;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
    private String deviceFingerprint;
    private Double velocityScore; // Number of transactions in last hour
    private BigDecimal averageTransactionAmount; // Member's average transaction
    private Integer transactionCount24h; // Transactions in last 24 hours
    private Boolean isNewDevice;
    private Boolean isNewLocation;
    private Double amountDeviationFromAverage; // How different from normal
}
