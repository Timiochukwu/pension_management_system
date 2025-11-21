package pension_management_system.pension.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

/**
 * Event published when a payment is successfully processed
 */
@Getter
public class PaymentSuccessEvent extends ApplicationEvent {

    private final String paymentReference;
    private final String transactionId;
    private final String memberName;
    private final String memberEmail;
    private final BigDecimal amount;
    private final String gateway;
    private final String paymentMethod;

    public PaymentSuccessEvent(Object source, String paymentReference, String transactionId,
                               String memberName, String memberEmail, BigDecimal amount,
                               String gateway, String paymentMethod) {
        super(source);
        this.paymentReference = paymentReference;
        this.transactionId = transactionId;
        this.memberName = memberName;
        this.memberEmail = memberEmail;
        this.amount = amount;
        this.gateway = gateway;
        this.paymentMethod = paymentMethod;
    }
}
