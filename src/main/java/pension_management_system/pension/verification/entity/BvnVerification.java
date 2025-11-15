package pension_management_system.pension.verification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pension_management_system.pension.member.entity.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * BvnVerification - BVN (Bank Verification Number) verification records
 *
 * Purpose: Verify member identity using BVN (Nigerian requirement)
 *
 * BVN is Nigeria's national identity system for banking
 * Required for:
 * - Regulatory compliance (PENCOM)
 * - Anti-fraud measures
 * - KYC (Know Your Customer)
 *
 * Verification process:
 * 1. Member provides BVN
 * 2. System calls verification API
 * 3. Matches name, DOB, phone number
 * 4. Stores verification result
 */
@Entity
@Table(name = "bvn_verifications", indexes = {
        @Index(name = "idx_bvn_member", columnList = "member_id"),
        @Index(name = "idx_bvn_number", columnList = "bvn_number"),
        @Index(name = "idx_bvn_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BvnVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(name = "bvn_number", nullable = false, length = 11)
    private String bvnNumber; // 11 digits

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationStatus status;

    @Column(name = "verified_first_name", length = 100)
    private String verifiedFirstName;

    @Column(name = "verified_last_name", length = 100)
    private String verifiedLastName;

    @Column(name = "verified_date_of_birth")
    private LocalDate verifiedDateOfBirth;

    @Column(name = "verified_phone_number", length = 20)
    private String verifiedPhoneNumber;

    @Column(name = "verified_gender", length = 10)
    private String verifiedGender;

    @Column(name = "match_score")
    private Integer matchScore; // 0-100 (how well data matches)

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @Column(name = "provider", length = 50)
    private String provider; // API provider (Smile ID, Youverify, etc.)

    @Column(name = "provider_reference", length = 100)
    private String providerReference;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum VerificationStatus {
        PENDING,        // Verification initiated
        VERIFIED,       // Successfully verified
        FAILED,         // Verification failed
        MISMATCH,       // BVN found but data doesn't match
        INVALID_BVN,    // BVN format invalid
        NOT_FOUND,      // BVN not found in database
        ERROR           // API error
    }
}
