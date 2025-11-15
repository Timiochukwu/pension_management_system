package pension_management_system.pension.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ErrorResponse - Standardized error response format
 *
 * Purpose: Consistent error structure for all API errors
 *
 * Benefits:
 * - Clients know what to expect
 * - Easy to parse and display
 * - Includes all necessary information
 * - Professional error messages
 *
 * @JsonInclude(JsonInclude.Include.NON_NULL)
 * - Only include non-null fields in JSON
 * - Errors map only included for validation errors
 * - Keeps response clean
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * When error occurred
     * Example: "2025-01-15T10:30:00"
     */
    private LocalDateTime timestamp;

    /**
     * HTTP status code
     * Examples: 400, 404, 500
     */
    private int status;

    /**
     * HTTP status phrase
     * Examples: "Bad Request", "Not Found", "Internal Server Error"
     */
    private String error;

    /**
     * Human-readable error message
     * Examples:
     * - "Member not found with ID: 123"
     * - "Invalid username or password"
     * - "Insufficient balance for benefit claim"
     */
    private String message;

    /**
     * Request path that caused error
     * Example: "/api/members/123"
     */
    private String path;

    /**
     * Field-specific validation errors
     * Only present for validation failures
     *
     * Example:
     * {
     *   "email": "must be a valid email address",
     *   "amount": "must be greater than 0"
     * }
     */
    private Map<String, String> errors;
}
