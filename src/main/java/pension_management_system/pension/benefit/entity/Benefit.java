package pension_management_system.pension.benefit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pension_management_system.pension.member.entity.Member;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "benefits", indexes = {
        @Index(name = "idx_benefit_reference", columnList = "referenceNumber"),
        @Index(name = "idx_benefit_member", columnList = "member_id"),
        @Index(name = "idx_benefit_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Benefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String referenceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BenefitType benefitType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BenefitStatus status;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalContributions;

    @Column(precision = 15, scale = 2)
    private BigDecimal employerContributions;

    @Column(precision = 15, scale = 2)
    private BigDecimal investmentReturns;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal calculatedBenefit;

    @Column(precision = 15, scale = 2)
    private BigDecimal taxDeductions;

    @Column(precision = 15, scale = 2)
    private BigDecimal administrativeFees;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal netPayable;

    @Column(nullable = false)
    private LocalDate applicationDate;

    @Column
    private LocalDate approvalDate;

    @Column
    private LocalDate disbursementDate;

    @Column(length = 100)
    private String approvedBy;

    @Column(length = 100)
    private String disbursedBy;

    @Column(length = 500)
    private String remarks;

    @Column(length = 500)
    private String rejectionReason;

    @Column(length = 100)
    private String paymentMethod;

    @Column(length = 100)
    private String accountNumber;

    @Column(length = 100)
    private String bankName;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (referenceNumber == null) {
            referenceNumber = generateReferenceNumber();
        }
        if (status == null) {
            status = BenefitStatus.PENDING;
        }
        if (applicationDate == null) {
            applicationDate = LocalDate.now();
        }
    }

    private String generateReferenceNumber() {
        return "BEN" + System.currentTimeMillis();
    }

    public void approve(String approvedBy) {
        this.status = BenefitStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvalDate = LocalDate.now();
    }

    public void approve(BigDecimal amount, String approvedBy) {
        this.status = BenefitStatus.APPROVED;
        this.netPayable = amount;
        this.approvedBy = approvedBy;
        this.approvalDate = LocalDate.now();
    }

    public void disburse(String disbursedBy) {
        this.status = BenefitStatus.DISBURSED;
        this.disbursedBy = disbursedBy;
        this.disbursementDate = LocalDate.now();
    }

    public void reject(String reason) {
        this.status = BenefitStatus.REJECTED;
        this.rejectionReason = reason;
    }

    public void reject(String reason, String rejectedBy) {
        this.status = BenefitStatus.REJECTED;
        this.rejectionReason = reason;
        this.remarks = "Rejected by: " + rejectedBy;
    }

    public void cancel() {
        this.status = BenefitStatus.CANCELLED;
    }

    public void markAsPaid() {
        this.status = BenefitStatus.DISBURSED;
        this.disbursementDate = LocalDate.now();
    }

    public void startReview() {
        this.status = BenefitStatus.UNDER_REVIEW;
    }
}
