package pension_management_system.pension.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

/**
 * PaymentSuccessEvent - Event fired when payment succeeds
 *
 * Purpose: Trigger email notification when payment processed
 */
@Getter
public class PaymentSuccessEvent extends ApplicationEvent {

    private final String memberEmail;
    private final String memberName;
    private final BigDecimal amount;
    private final String paymentReference;
    private final String transactionId;
    private final String gateway;
    private final String paymentMethod;

    public PaymentSuccessEvent(
            Object source,
            String memberEmail,
            String memberName,
            BigDecimal amount,
            String paymentReference,
            String transactionId,
            String gateway,
            String paymentMethod) {

        super(source);
        this.memberEmail = memberEmail;
        this.memberName = memberName;
        this.amount = amount;
        this.paymentReference = paymentReference;
        this.transactionId = transactionId;
        this.gateway = gateway;
        this.paymentMethod = paymentMethod;
    }
}
