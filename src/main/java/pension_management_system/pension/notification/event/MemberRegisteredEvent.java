package pension_management_system.pension.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * MemberRegisteredEvent - Event fired when new member registers
 *
 * Purpose: Trigger email notification when account created
 *
 * What is an Application Event?
 * - Spring's way of communication between components
 * - Publisher fires event (e.g., MemberService)
 * - Listener receives event (e.g., EmailService)
 * - Decoupled communication (publisher doesn't know about listeners)
 *
 * Why use events?
 * - Separation of concerns
 * - One action can trigger multiple reactions
 * - Easy to add new listeners without changing publisher
 * - Async processing support
 *
 * Example Flow:
 * 1. Member registers → MemberService.register()
 * 2. Service publishes MemberRegisteredEvent
 * 3. EmailListener receives event
 * 4. Sends welcome email
 * 5. (Other listeners can also react: Analytics, Audit, etc.)
 *
 * Benefits:
 * - MemberService doesn't know about emails
 * - Can add SMS notification later without changing MemberService
 * - Clean architecture
 *
 * @Getter - Lombok generates getters for all fields
 */
@Getter
public class MemberRegisteredEvent extends ApplicationEvent {

    /**
     * EVENT DATA
     *
     * Information needed to send welcome email
     */
    private final Long memberId;
    private final String memberName;
    private final String memberEmail;
    private final String accountType;

    /**
     * CONSTRUCTOR
     *
     * @param source Event source (usually the service that fired it)
     * @param memberId Member's ID
     * @param memberName Member's full name
     * @param memberEmail Member's email address
     * @param accountType Type of account (Individual, Corporate, etc.)
     */
    public MemberRegisteredEvent(
            Object source,
            Long memberId,
            String memberName,
            String memberEmail,
            String accountType) {

        super(source);
        this.memberId = memberId;
        this.memberName = memberName;
        this.memberEmail = memberEmail;
        this.accountType = accountType;
    }
}

/**
 * USAGE IN SERVICE
 *
 * When member registers:
 *
 * ```java
 * @Service
 * @RequiredArgsConstructor
 * public class MemberService {
 *
 *     private final MemberRepository memberRepository;
 *     private final ApplicationEventPublisher eventPublisher;
 *
 *     public Member register(RegisterRequest request) {
 *         // Create member
 *         Member member = new Member();
 *         member.setName(request.getName());
 *         member.setEmail(request.getEmail());
 *         member = memberRepository.save(member);
 *
 *         // Publish event (triggers welcome email)
 *         eventPublisher.publishEvent(new MemberRegisteredEvent(
 *             this,
 *             member.getId(),
 *             member.getName(),
 *             member.getEmail(),
 *             "Individual"
 *         ));
 *
 *         return member;
 *     }
 * }
 * ```
 */
