package pension_management_system.pension.member.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;
import pension_management_system.pension.employer.entity.Employer;

import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * Member Entity - Represents a pension scheme member
 * This is the main entity that stores information about members enrolled in the pension system
 */


@Entity // Tells Spring this is a database table
@Table(name = "members", indexes = { // Custom table name and database indexes for faster queries
        @Index(name = "idx_member_id", columnList = "memberId"), // Index on memberId for fast lookups
        @Index(name = "idx_email", columnList = "email"), // Index on email for fast searches
//        @Index(name = "idx_employer", columnList = "employer_id") // Index on employer relationship
})
@Getter // Lombok: Auto-generates getter methods for all fields
@Setter // Lombok: Auto-generates setter methods for all fields
@Builder // Lombok: Enables builder pattern for object creation (e.g., Member.builder().firstName("John").build())
@NoArgsConstructor // Lombok: Creates a constructor with no parameters (required by JPA)
@AllArgsConstructor // Lombok: Creates a constructor with all parameters

// Soft delete: When we "delete" a record, we just mark it as deleted instead of removing it from database
@SQLDelete(sql = "UPDATE members SET deleted = true, updated_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted = false") // Only fetch records that are not deleted

public class Member {
    // PRIMARY KEY - Unique identifier for each member record
    @Id //  Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID (1, 2, 3, etc.)
    private Long id;

    // MEMBER ID - Business identifier (like employee number)
    @Column(nullable = false, unique = true, length = 50) // Cannot be null, must be unique, max 50 characters
    private String memberId; // Example: "MEM123456789"

    // FIRST NAME - Member's first name
    @NotBlank(message = "First name is required") // Validation: Cannot be empty or just whitespace
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String firstName;


    // LAST NAME - Member's last name
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String lastName;

    // Email - Member's Email Address
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format") // Validates email format (must contain @, proper domain, etc.)
    @Column(nullable = false, unique = true, length = 100) // Must be unique - no two members can have same email
    private String email;

    // PHONE NUMBER - Member's contact number
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    // Validates phone format: optional +, starts with 1-9, followed by 1-14 digits
    @Column(nullable = false, unique = true, length = 20)
    private String phoneNumber;

    // DATE OF BIRTH - Required for age validation (must be 18-70 years old)
    @NotNull(message = "Date of Birth is required")
    @Past(message = "Date of birth must be in the past") // Cannot be today or future date
    @Column(nullable = false)
    private LocalDate dateOfBirth; // Stores date without time (e.g., 1990-05-15)

    // ADDRESS FIELDS - Member's residential address
    @Column(length = 255)
    private String address; // Street address

    @Column(length = 100)
    private String city; // City name

    @Column(length = 100)
    private String state; // State/Province

    @Column(length = 20)
    private String postalCode; // ZIP/Postal code

    @Column(length = 100)
    private String country; // Country name

    // MEMBER STATUS - Enum to track member's status in the system
    @Enumerated(EnumType.STRING) // Store enum as text (ACTIVE, INACTIVE, SUSPENDED) instead of numbers
    @Column(nullable = false, length = 20)
    @Builder.Default // Sets default value when using builder pattern
    private MemberStatus memberStatus = MemberStatus.ACTIVE;  // Default status is ACTIVE

    // ACTIVE FLAG - Quick check if member is active
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true; // true = active, false = inactive

    // SOFT DELETE FLAG - For soft delete functionality
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false; // false = not deleted, true = deleted (but still in database)

    // EMPLOYER RELATIONSHIP - Links member to their employer
    @ManyToOne(fetch = FetchType.LAZY) // Many members can belong to one employer
//     LAZY loading: Employer data is only loaded when explicitly accessed (saves memory)
    @JoinColumn(name = "employer_id") // Foreign key column name in members table
    private Employer employer;

    // CONTRIBUTIONS RELATIONSHIP - All contributions made by this member
//    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    // One member can have many contributions
    // mappedBy = "member": The Contribution entity has a "member" field that manages this relationship
    // cascade = ALL: When we save/delete a member, their contributions are also saved/deleted
    // orphanRemoval = true: If a contribution is removed from the list, delete it from database
//    @Builder.Default
//    private List<Contribution> contributions = new ArrayList<>();

    // AUDIT TIMESTAMPS - Automatically tracked dates
    @CreationTimestamp // Automatically set when record is created
    @Column(nullable = false, updatable = false) // Cannot be updated after creation
    private LocalDateTime createdAt;

    @UpdateTimestamp  // Automatically updated every time record is modified
    @Column(nullable = false)
    private LocalDateTime updatedAt;  // When this member was last updated

    @Column
    private LocalDateTime deletedAt; // When this member was soft-deleted (null if not deleted)

    // ==================== HELPER METHODS ====================

    /**
     * Calculate member's age based on date of birth
     * Used for validation (must be 18-70 years old)
     *
     * @return age in years
     */

    public int getAge() {
        if (this.dateOfBirth == null) {
            return 0;
        }
        // Simple age calculation: current year - birth year
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    /**
     * Get member's full name
     *
     * @return "FirstName LastName"
     */
    public String getfullName() {
        return firstName + " " + lastName;
    }

    /**
     * Add a contribution to this member
     * This properly maintains the bidirectional relationship
     */
//    public void addContribution(Contribution contribution){

//}

/**
 * Remove a contribution from this member
 */
//    public void removeContribution(Contribution contribution){

//    }

    /**
     * Lifecycle callback - runs before saving to database
     * Generates memberId if not already set
     */
    @PrePersist
    //// JPA annotation: Execute this method before persisting (saving) to database
    public void prePersist() {
        if (memberId == null || memberId.isEmpty()) {
            memberId = generateMemberId();
        }
    }

    /**
     * Generate a unique member ID
     * Format: MEM + timestamp (e.g., MEM1699564800000)
     */
    private String generateMemberId() {
        return "MEM" + System.currentTimeMillis(); // Current time in milliseconds since 1970
    }

    /**
     * Soft delete this member
     * Instead of deleting from database, we mark it as deleted
     * This preserves historical data and relationships
     */
    public void softDelete() {
        this.active = false;  // Mark as inactive
        this.deleted = true;  // Mark as deleted
        this.deletedAt = LocalDateTime.now(); // Record when deleted
    }
    public void activate() {
        this.active = true;
        this.deleted = false;
        this.updatedAt = LocalDateTime.now();
    }
    public void deactivate() {
        this.setMemberStatus( MemberStatus.INACTIVE);
        this.active = false;
        this.deleted = false;
        this.updatedAt = LocalDateTime.now();
    }
}






















