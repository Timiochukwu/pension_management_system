package pension_management_system.pension.member.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import pension_management_system.pension.member.entity.MemberStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * MemberResponse - DTO for returning member data to client
 *
 * This is what the API returns when querying member information
 * Never expose the Entity directly - always use DTOs
 *
 * Why use DTOs instead of returning Entity?
 * 1. Security: Hide sensitive fields (passwords, internal IDs)
 * 2. Control: Choose exactly what data to expose
 * 3. Flexibility: Can combine data from multiple entities
 * 4. Versioning: Can have different DTOs for different API versions
 *
 * @JsonInclude(NON_NULL) - Don't include null fields in JSON response
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberResponse {
    /**
     * Database ID - Internal identifier
     */
    private Long id;

    /**
     * Member ID - Business identifier shown to users
     * Example: "MEM1699564800000"
     */
    private String memberId;

    /**
     * Personal Information
     */
    private String firstName;
    private String lastName;
    private String fullName; // Computed field: firstName + lastName
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private  Integer age; // Computed field: calculated from dateOfBirth

    /**
     * Address Information
     */
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    /**
     * Status Information
     */
    private MemberStatus memberStatus; // ACTIVE, INACTIVE, SUSPENDED, RETIRED, TERMINATED
    private Boolean active; // Quick check: true/false

    /**
     * Employer Information
     * We don't return the entire Employer object
     * Just the ID and name (most useful info)
     */
    private Long employerId;
    private String employerName;

    /**
     * Statistics
     * These would be calculated in the service layer
     */
    private Long totalContributions; // How many contributions made
    private String totalContributionAmount; // Total amount contributed
    private Boolean eligibleForBenefits; // Is member eligible?

    /**
     * Audit Information
     * Shows when record was created/updated
     */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;




}
