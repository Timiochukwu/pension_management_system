package pension_management_system.pension.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

/**
 * Event published when a new contribution is created
 */
@Getter
public class ContributionCreatedEvent extends ApplicationEvent {

    private final Long contributionId;
    private final Long memberId;
    private final String memberName;
    private final String memberEmail;
    private final BigDecimal amount;
    private final String contributionType;
    private final BigDecimal previousBalance;
    private final BigDecimal newBalance;

    public ContributionCreatedEvent(Object source, Long contributionId, Long memberId, String memberName,
                                    String memberEmail, BigDecimal amount, String contributionType,
                                    BigDecimal previousBalance, BigDecimal newBalance) {
        super(source);
        this.contributionId = contributionId;
        this.memberId = memberId;
        this.memberName = memberName;
        this.memberEmail = memberEmail;
        this.amount = amount;
        this.contributionType = contributionType;
        this.previousBalance = previousBalance;
        this.newBalance = newBalance;
    }
}
