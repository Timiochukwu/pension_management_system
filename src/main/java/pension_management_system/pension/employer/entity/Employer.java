package pension_management_system.pension.employer.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;
import pension_management_system.pension.member.entity.Member;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "employers", indexes = {
        @Index(name = "idx_registration_name", columnList = "registrationNumber"),
        @Index(name = "idx_company_name", columnList = "companyName")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE employers SET deleted = true, updated_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted = false")

public class Employer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String employerId;


    @Column(nullable = false, unique = true, length = 50)
    private String companyName;

    @Column(nullable = false, unique = true, length = 50)
    private String registrationNumber;

   @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 20)
    private String postalCode;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String industry;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @OneToMany(mappedBy = "employer", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Member> members = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = true)
    private LocalDateTime deletedAt;

    // Utility methods

    public int getMemberCount() {
        return members != null ? members.size() : 0;
    }
    public void addMember(Member member) {
        members.add(member);
        member.setEmployer(this);
    }
    public void removeMember(Member member) {
        members.remove(member);
        member.setEmployer(null);
    }
    public boolean canAcceptNewMember(Member member) {
        return active && !deleted;
    }
    public void deactivate() {
        this.active = false;
    }
    public void activate() {
        this.active = true;
    }
    public void softDelete() {
        this.active = false;
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
    @PrePersist
    public void prePersist() {
        if (employerId == null || employerId.isEmpty()) {
            employerId = "EMP" + System.currentTimeMillis();
        }
    }






}
