package pension_management_system.pension.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;


/**
 * ApiResponse - Generic wrapper for all API responses
 *
 * Provides consistent response format across all endpoints
 *
 * <T> - Generic type: Can wrap any data type (MemberResponse, List<Member>, etc.)
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Don't include null fields in JSON

public class ApiResponseDto<T> {
    /**
     * success - true for successful operations, false for errors
     */
    private Boolean success;
    /**
     * message - Human-readable message
     */
    private String message;

    /**
     * timestamp - When the error occurred
     */
    private LocalDateTime timestamp;

    /**
     * errors - Map of field-specific errors (for validation failures)
     * Key = field name, Value = error message
     *
     * Example:
     * {
     *   "firstName": "First name is required",
     *   "email": "Invalid email format"
     * }
     */
    private Map<String, String> errors;
    /**
     * data - The actual response data (generic type T)
     */
    private T data;


}
