package pension_management_system.pension.benefit.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pension_management_system.pension.benefit.entity.BenefitType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * BenefitRequest DTO - Data Transfer Object for creating/updating benefits
 *
 * What is a DTO?
 * - Data Transfer Object - carries data between processes
 * - Used to receive data from API requests (JSON → Java object)
 * - Separates API contract from database structure
 * - Allows validation before touching database
 *
 * Why use DTO instead of Entity?
 * - Entity has database-specific fields (id, createdAt, updatedAt)
 * - DTO has only fields user can provide
 * - Entity changes don't break API contracts
 * - Better security (hide internal database structure)
 *
 * Annotations Explained:
 * @Data - Lombok generates getters, setters, toString, equals, hashCode
 * @NotNull - Field must be provided (cannot be null)
 * @NotBlank - String must not be empty or whitespace
 * @DecimalMin - Number must be at least this value
 *
 * Example JSON Request:
 * {
 *   "memberId": 123,
 *   "benefitType": "RETIREMENT",
 *   "requestedAmount": 50000.00,
 *   "applicationDate": "2025-01-15",
 *   "notes": "Retirement after 30 years of service"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenefitRequest {

    /**
     * MEMBER ID
     * @NotNull - Must provide a member ID
     * @Min(1) - Must be positive number
     *
     * This is the database ID of the member, not the memberId field
     */
    @NotNull(message = "Member ID is required")
    @Min(value = 1, message = "Member ID must be positive")
    private Long memberId;

    /**
     * BENEFIT TYPE
     * @NotNull - Must specify type of benefit
     *
     * Values: RETIREMENT, DEATH, DISABILITY, WITHDRAWAL, TEMPORARY_WITHDRAWAL
     *
     * Front-end should provide dropdown with these options
     */
    @NotNull(message = "Benefit type is required")
    private BenefitType benefitType;

    /**
     * REQUESTED AMOUNT
     * @NotNull - Amount is required
     * @DecimalMin - Must be at least 0.01 (no zero or negative amounts)
     *
     * For withdrawals: Member specifies how much they want
     * For retirement/death: System calculates, but still required in request
     */
    @NotNull(message = "Requested amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal requestedAmount;

    /**
     * APPLICATION DATE
     * @NotNull - Must provide when claim was submitted
     * @PastOrPresent - Cannot be in future (can't claim benefit before applying!)
     *
     * Usually set to today's date by front-end
     */
    @NotNull(message = "Application date is required")
    @PastOrPresent(message = "Application date cannot be in the future")
    private LocalDate applicationDate;

    /**
     * NOTES/COMMENTS
     * Optional field for additional information
     * Example: "Member requesting early retirement due to health issues"
     *
     * No validation - can be empty or null
     */
    private String notes;
}
