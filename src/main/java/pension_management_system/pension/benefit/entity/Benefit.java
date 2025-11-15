package pension_management_system.pension.benefit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pension_management_system.pension.member.entity.Member;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Benefit Entity - Represents a pension benefit claim
 *
 * What is a Benefit?
 * - A claim made by a member to receive money from their pension fund
 * - Can be retirement payment, death benefit, disability benefit, or withdrawal
 * - Goes through approval process before payment
 *
 * Database Table: benefits
 * - Stores all benefit claims
 * - Links to members table
 * - Tracks claim status and payment details
 *
 * Annotations Explained:
 * @Entity - Tells JPA this is a database table
 * @Table - Specifies table name
 * @Data - Lombok generates getters, setters, toString, equals, hashCode
 * @NoArgsConstructor - Lombok generates empty constructor
 * @AllArgsConstructor - Lombok generates constructor with all fields
 */
@Entity
@Table(name = "benefits")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Benefit {

    /**
     * Primary Key - Auto-generated ID
     * @Id - Marks this as primary key
     * @GeneratedValue - Database auto-generates this value
     * IDENTITY strategy - Uses database auto-increment
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * MEMBER RELATIONSHIP
     * @ManyToOne - Many benefits can belong to one member
     * @JoinColumn - Creates foreign key column 'member_id'
     * LAZY loading - Member data loaded only when accessed (better performance)
     * nullable = false - Benefit must have a member
     *
     * Example: If member ID 5 has 3 benefits:
     * - Retirement benefit
     * - Temporary withdrawal
     * - Another temporary withdrawal
     * All have member_id = 5
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * BENEFIT TYPE
     * @Enumerated - Stores enum as string in database
     * @Column - Specifies column details
     * nullable = false - Type is required
     *
     * Values: RETIREMENT, DEATH, DISABILITY, WITHDRAWAL, TEMPORARY_WITHDRAWAL
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BenefitType benefitType;

    /**
     * BENEFIT STATUS
     * Tracks where the claim is in the approval process
     *
     * Workflow:
     * PENDING → UNDER_REVIEW → APPROVED → PAID
     *         ↘ REJECTED
     *         ↘ CANCELLED
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BenefitStatus status;

    /**
     * APPLICATION DATE
     * When member submitted the benefit claim
     * Cannot be null - every claim must have submission date
     */
    @Column(nullable = false)
    private LocalDate applicationDate;

    /**
     * REQUESTED AMOUNT
     * How much the member is requesting
     * For withdrawals, member specifies amount
     * For retirement/death/disability, calculated by system
     *
     * @Column - precision = total digits, scale = decimal places
     * Example: 99999999.99 (8 digits before decimal, 2 after)
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal requestedAmount;

    /**
     * APPROVED AMOUNT
     * Actual amount approved for payment
     * May differ from requested amount
     * Set when status changes to APPROVED
     * Null until approved
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal approvedAmount;

    /**
     * APPROVAL DATE
     * When the benefit was approved
     * Null until status = APPROVED
     */
    private LocalDate approvalDate;

    /**
     * PAYMENT DATE
     * When the money was actually paid to member
     * Null until status = PAID
     */
    private LocalDate paymentDate;

    /**
     * APPROVED BY
     * Name or ID of admin who approved the claim
     * For audit trail
     */
    private String approvedBy;

    /**
     * REJECTION REASON
     * Why the claim was rejected (if status = REJECTED)
     * Important for transparency and future reference
     * Examples: "Member not eligible", "Insufficient documents", etc.
     */
    @Column(length = 500)
    private String rejectionReason;

    /**
     * NOTES/COMMENTS
     * Additional information about the claim
     * Can be updated by admins during review
     */
    @Column(length = 1000)
    private String notes;

    /**
     * CREATED AT
     * @CreationTimestamp - Automatically set when record is created
     * updatable = false - Never changed after creation
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * UPDATED AT
     * @UpdateTimestamp - Automatically updated when record changes
     * Tracks last modification time
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * HELPER METHODS FOR STATUS MANAGEMENT
     *
     * These methods make it easy to update status with related fields
     * Ensures data consistency
     */

    /**
     * Approve the benefit claim
     *
     * @param approvedAmount The amount to pay
     * @param approvedBy Who approved it
     */
    public void approve(BigDecimal approvedAmount, String approvedBy) {
        this.status = BenefitStatus.APPROVED;
        this.approvedAmount = approvedAmount;
        this.approvalDate = LocalDate.now();
        this.approvedBy = approvedBy;
    }

    /**
     * Reject the benefit claim
     *
     * @param reason Why it was rejected
     * @param rejectedBy Who rejected it
     */
    public void reject(String reason, String rejectedBy) {
        this.status = BenefitStatus.REJECTED;
        this.rejectionReason = reason;
        this.approvedBy = rejectedBy; // Track who made decision
    }

    /**
     * Mark benefit as paid
     */
    public void markAsPaid() {
        if (this.status != BenefitStatus.APPROVED) {
            throw new IllegalStateException("Can only mark APPROVED benefits as PAID");
        }
        this.status = BenefitStatus.PAID;
        this.paymentDate = LocalDate.now();
    }

    /**
     * Move claim to under review
     */
    public void startReview() {
        if (this.status != BenefitStatus.PENDING) {
            throw new IllegalStateException("Can only review PENDING benefits");
        }
        this.status = BenefitStatus.UNDER_REVIEW;
    }

    /**
     * Cancel the benefit claim
     */
    public void cancel() {
        if (this.status == BenefitStatus.PAID) {
            throw new IllegalStateException("Cannot cancel PAID benefits");
        }
        this.status = BenefitStatus.CANCELLED;
    }
}
