package pension_management_system.pension.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pension_management_system.pension.common.dto.ApiResponseDto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler - Centralized exception handling for all controllers
 *
 * Purpose:
 * Instead of handling exceptions in every controller method,
 * we handle them all in one place. This ensures:
 * 1. Consistent error response format across all endpoints
 * 2. Clean controller code (no try-catch blocks needed)
 * 3. Easy to maintain and update error handling
 *
 * Annotations explained:
 * @RestControllerAdvice - Applies to all @RestController classes in the application
 *                        Catches exceptions thrown by any controller
 * @ExceptionHandler - Defines which exception this method handles
 * @Slf4j - Provides logging capability
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle MemberNotFoundException
     *
     * When: Member is not found in database
     * Status Code: 404 NOT FOUND
     *
     * Example Response:
     * {
     *   "success": false,
     *   "message": "Member not found with ID: 1",
     *   "timestamp": "2023-11-15T10:30:00",
     *   "errors": null
     * }
     */
    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ApiResponseDto> handleMemberNotFoundException(MemberNotFoundException ex) {

        // Log the error (helps with debugging)
        log.error("Member not found: {}", ex.getMessage());

        // Create error response
        ApiResponseDto errorResponse = ApiResponseDto.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .errors(null)
                .build();

        // Return with 404 status
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    /**
     * Handle IllegalArgumentException
     *
     * When: Invalid input data (e.g., age out of range, invalid email)
     * Status Code: 400 BAD REQUEST
     *
     * Example Response:
     * {
     *   "success": false,
     *   "message": "Member must be at least 18 years old",
     *   "timestamp": "2023-11-15T10:30:00",
     *   "errors": null
     * }
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto> handleIllegalArgumentException(
            IllegalArgumentException ex) {

        log.error("Invalid argument: {}", ex.getMessage());

        ApiResponseDto errorResponse = ApiResponseDto.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .errors(null)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handle MethodArgumentNotValidException
     *
     * When: Validation annotations fail (e.g., @NotBlank, @Email, @Min, @Max)
     * Status Code: 400 BAD REQUEST
     *
     * This exception is thrown by Spring when @Valid annotation detects
     * validation errors in request body
     *
     * Example Response:
     * {
     *   "success": false,
     *   "message": "Validation failed",
     *   "timestamp": "2023-11-15T10:30:00",
     *   "errors": {
     *     "firstName": "First name is required",
     *     "email": "Invalid email format",
     *     "dateOfBirth": "Date of birth must be in the past"
     *   }
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto> handleValidationException(
            MethodArgumentNotValidException ex) {

        log.error("Validation failed: {}", ex.getMessage());

        // Extract all field errors into a map
        // Key = field name, Value = error message
        Map<String, String> errors = new HashMap<>();

        // Loop through all validation errors
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            // Get field name (e.g., "firstName", "email")
            String fieldName = ((FieldError) error).getField();

            // Get error message (e.g., "First name is required")
            String errorMessage = error.getDefaultMessage();

            // Add to map
            errors.put(fieldName, errorMessage);
        });

        ApiResponseDto errorResponse = ApiResponseDto.<Void>builder()
                .success(false)
                .message("Validation failed")
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handle IllegalStateException
     *
     * When: Operation not allowed in current state
     * Example: Trying to reactivate a terminated member
     * Status Code: 400 BAD REQUEST
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponseDto> handleIllegalStateException(
            IllegalStateException ex) {

        log.error("Illegal state: {}", ex.getMessage());

        ApiResponseDto errorResponse = ApiResponseDto.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .errors(null)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handle all other exceptions (catch-all)
     *
     * When: Any unexpected error occurs
     * Status Code: 500 INTERNAL SERVER ERROR
     *
     * This is a safety net to catch any exceptions we didn't specifically handle
     *
     * Example Response:
     * {
     *   "success": false,
     *   "message": "An unexpected error occurred. Please contact support.",
     *   "timestamp": "2023-11-15T10:30:00",
     *   "errors": null
     * }
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto> handleGenericException(Exception ex) {

        // Log full stack trace for debugging
        log.error("Unexpected error occurred", ex);

        ApiResponseDto errorResponse = ApiResponseDto.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .errors(null)
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    /**
     * Handle DuplicateMonthlyContributionException
     *
     * When: Member tries to make multiple monthly contributions in same month
     * Status Code: 409 CONFLICT
     */
    @ExceptionHandler(DuplicateMonthlyContributionException.class)
    public ResponseEntity<ApiResponseDto> handleDuplicateMonthlyContributionException(DuplicateMonthlyContributionException ex) {

        log.error("Duplicate monthly contribution: {}", ex.getMessage());

        ApiResponseDto errorResponse = ApiResponseDto.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .errors(null)
                .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    /**
     * Handle InvalidContributionException
     *
     * When: Contribution validation fails
     * Status Code: 400 BAD REQUEST
     */
    @ExceptionHandler(InvalidContributionException.class)
    public ResponseEntity<ApiResponseDto> handleInvalidContributionException(InvalidContributionException ex) {

        log.error("Invalid contribution: {}", ex.getMessage());

        ApiResponseDto errorResponse = ApiResponseDto.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .errors(null)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
}
