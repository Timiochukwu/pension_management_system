package pension_management_system.pension.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a new member is registered
 */
@Getter
public class MemberRegisteredEvent extends ApplicationEvent {

    private final Long memberId;
    private final String memberName;
    private final String memberEmail;
    private final String accountType;

    public MemberRegisteredEvent(Object source, Long memberId, String memberName, String memberEmail, String accountType) {
        super(source);
        this.memberId = memberId;
        this.memberName = memberName;
        this.memberEmail = memberEmail;
        this.accountType = accountType;
    }
}
