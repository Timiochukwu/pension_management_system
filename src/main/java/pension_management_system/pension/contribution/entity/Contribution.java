package pension_management_system.pension.contribution.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pension_management_system.pension.member.entity.Member;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(name = "contributions", indexes = {
        @Index(name = "idx_member_contribution", columnList = "member_id, contribution_date"),
        @Index(name = "idx_reference_number", columnList = "referenceNumber"),
        @Index(name = "idx_reference_type", columnList = "contributionType"),
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true, length = 50)
    private String referenceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ContributionType contributionType;

    @NotNull(message = "Contribution amount is required")
    @DecimalMin(value = "100", message = "Contribution amount must be greater or equals than 100")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal contributionAmount;

    @NotNull(message = "Contribution date is required")
    @Column(nullable = false)
    private LocalDateTime contributionDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentMethod paymentMethod;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContributionStatus status =  ContributionStatus.PENDING;

    @Column
    private LocalDateTime processedAt;

    @Column(length = 100)
    private String processedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public YearMonth getContributionYearMonth() {
        if (contributionDate == null) {
            return null;
        }
        return YearMonth.from(contributionDate);
    }

    public boolean isMonthlyContribution() {
        return contributionType == ContributionType.MONTHLY;
    }
    public boolean isVoluntaryContribution() {
        return contributionType == ContributionType.VOLUNTARY;
    }
    public void markAsProcessed(String processedByUser) {
        this.status = ContributionStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
        this.processedBy = processedByUser;
    }
    public void markedAsFailed(String reason) {
        this.status = ContributionStatus.FAILED;
        this.description = (this.description != null ? this.description + " | " : "") + "Failed: " + reason;
    }
    @PrePersist
    public void prePersist() {
        if (referenceNumber == null || referenceNumber.isEmpty()) {
            referenceNumber = generateReferenceNumber();
        }
    }
    private String generateReferenceNumber() {
        String dateStr = LocalDateTime.now().toString().replaceAll("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        return STR."CON\{dateStr}-\{timestamp.substring(timestamp.length() - 12)}";
    }

}
