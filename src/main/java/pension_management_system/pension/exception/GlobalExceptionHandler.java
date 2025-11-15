package pension_management_system.pension.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler - Centralized exception handling
 *
 * Purpose: Catch and handle all exceptions in one place
 *
 * What is @RestControllerAdvice?
 * - Global exception handler for all controllers
 * - Intercepts exceptions before they reach client
 * - Returns consistent error responses
 * - Applies to all @RestController classes
 *
 * Benefits:
 * - Consistent error format across all endpoints
 * - No try-catch in every controller
 * - Centralized error logging
 * - Better error messages for clients
 * - Clean controller code
 *
 * How it works:
 * 1. Exception thrown in controller/service
 * 2. Spring catches it
 * 3. Finds matching @ExceptionHandler method
 * 4. Calls handler method
 * 5. Returns error response to client
 *
 * Error Response Format:
 * {
 *   "timestamp": "2025-01-15T10:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Member not found with ID: 123",
 *   "path": "/api/members/123"
 * }
 *
 * Exception Hierarchy:
 * - Specific exceptions first (ResourceNotFoundException)
 * - General exceptions last (Exception.class)
 * - Spring handles in order of specificity
 *
 * @RestControllerAdvice - Global exception handler
 * @Slf4j - Logging support
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * HANDLE RESOURCE NOT FOUND
     *
     * Thrown when: Entity doesn't exist in database
     * Examples:
     * - Member not found
     * - Contribution not found
     * - Payment not found
     *
     * HTTP Status: 404 Not Found
     *
     * @param ex Exception thrown
     * @param request HTTP request context
     * @return Error response with 404 status
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            WebRequest request) {

        log.error("Resource not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(getPath(request))
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * HANDLE VALIDATION ERRORS
     *
     * Thrown when: @Valid fails on request DTO
     * Examples:
     * - Missing required field (@NotNull)
     * - Invalid email format (@Email)
     * - Amount less than minimum (@Min)
     *
     * HTTP Status: 400 Bad Request
     *
     * Response includes field-specific errors:
     * {
     *   "timestamp": "...",
     *   "status": 400,
     *   "error": "Validation Failed",
     *   "message": "Invalid request parameters",
     *   "errors": {
     *     "email": "must be a valid email address",
     *     "amount": "must be greater than 0"
     *   }
     * }
     *
     * @param ex Validation exception
     * @param request HTTP request
     * @return Error response with field errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        log.error("Validation error: {}", ex.getMessage());

        // Extract field errors
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Invalid request parameters")
                .path(getPath(request))
                .errors(fieldErrors)
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * HANDLE DUPLICATE RESOURCE
     *
     * Thrown when: Trying to create duplicate
     * Examples:
     * - Email already exists
     * - Duplicate payment reference
     * - Member ID already registered
     *
     * HTTP Status: 409 Conflict
     *
     * @param ex Exception thrown
     * @param request HTTP request
     * @return Error response with 409 status
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex,
            WebRequest request) {

        log.error("Duplicate resource: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(getPath(request))
                .build();

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * HANDLE BAD CREDENTIALS (LOGIN FAILURE)
     *
     * Thrown when: Invalid username/password
     *
     * HTTP Status: 401 Unauthorized
     *
     * Security note:
     * - Don't reveal if username or password is wrong
     * - Generic message prevents user enumeration
     *
     * @param ex Authentication exception
     * @param request HTTP request
     * @return Error response with 401 status
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            WebRequest request) {

        log.error("Authentication failed: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("Invalid username or password")
                .path(getPath(request))
                .build();

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * HANDLE ACCESS DENIED
     *
     * Thrown when: User lacks required permission
     * Examples:
     * - Member trying to access admin endpoint
     * - User without APPROVE_BENEFIT permission
     *
     * HTTP Status: 403 Forbidden
     *
     * @param ex Access denied exception
     * @param request HTTP request
     * @return Error response with 403 status
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            WebRequest request) {

        log.error("Access denied: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message("You don't have permission to access this resource")
                .path(getPath(request))
                .build();

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    /**
     * HANDLE BUSINESS LOGIC ERRORS
     *
     * Thrown when: Business rule violated
     * Examples:
     * - Insufficient balance for benefit claim
     * - Contribution amount exceeds limit
     * - Payment already processed
     *
     * HTTP Status: 422 Unprocessable Entity
     *
     * @param ex Business exception
     * @param request HTTP request
     * @return Error response with 422 status
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            WebRequest request) {

        log.error("Business rule violation: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .error("Business Rule Violation")
                .message(ex.getMessage())
                .path(getPath(request))
                .build();

        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * HANDLE ILLEGAL ARGUMENT
     *
     * Thrown when: Invalid method argument
     * Examples:
     * - Null when not allowed
     * - Invalid enum value
     * - Out of range value
     *
     * HTTP Status: 400 Bad Request
     *
     * @param ex Illegal argument exception
     * @param request HTTP request
     * @return Error response with 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            WebRequest request) {

        log.error("Illegal argument: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(getPath(request))
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * HANDLE ALL OTHER EXCEPTIONS
     *
     * Catch-all for unexpected errors
     *
     * HTTP Status: 500 Internal Server Error
     *
     * Security:
     * - Don't expose stack trace to client
     * - Log full error for debugging
     * - Return generic message
     *
     * @param ex Any unhandled exception
     * @param request HTTP request
     * @return Error response with 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            WebRequest request) {

        // Log full stack trace
        log.error("Unexpected error occurred", ex);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred. Please try again later.")
                .path(getPath(request))
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * EXTRACT REQUEST PATH
     *
     * Helper method to get request URI
     *
     * @param request Web request
     * @return Request path
     */
    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}

/**
 * CUSTOM EXCEPTIONS
 *
 * Create these exception classes for specific use cases:
 *
 * 1. ResourceNotFoundException:
 * ```java
 * public class ResourceNotFoundException extends RuntimeException {
 *     public ResourceNotFoundException(String message) {
 *         super(message);
 *     }
 *
 *     public static ResourceNotFoundException member(Long id) {
 *         return new ResourceNotFoundException("Member not found with ID: " + id);
 *     }
 *
 *     public static ResourceNotFoundException contribution(Long id) {
 *         return new ResourceNotFoundException("Contribution not found with ID: " + id);
 *     }
 * }
 * ```
 *
 * 2. DuplicateResourceException:
 * ```java
 * public class DuplicateResourceException extends RuntimeException {
 *     public DuplicateResourceException(String message) {
 *         super(message);
 *     }
 *
 *     public static DuplicateResourceException email(String email) {
 *         return new DuplicateResourceException("Email already exists: " + email);
 *     }
 * }
 * ```
 *
 * 3. BusinessException:
 * ```java
 * public class BusinessException extends RuntimeException {
 *     public BusinessException(String message) {
 *         super(message);
 *     }
 * }
 * ```
 *
 * USAGE IN SERVICES
 *
 * ```java
 * @Service
 * public class MemberService {
 *
 *     public Member getMember(Long id) {
 *         return memberRepository.findById(id)
 *             .orElseThrow(() -> ResourceNotFoundException.member(id));
 *     }
 *
 *     public Member createMember(MemberRequest request) {
 *         if (memberRepository.existsByEmail(request.getEmail())) {
 *             throw DuplicateResourceException.email(request.getEmail());
 *         }
 *         // create member
 *     }
 *
 *     public void approveBenefit(Long benefitId) {
 *         Benefit benefit = getBenefit(benefitId);
 *         if (benefit.getMember().getBalance() < benefit.getAmount()) {
 *             throw new BusinessException("Insufficient balance for benefit claim");
 *         }
 *         // approve benefit
 *     }
 * }
 * ```
 *
 * TESTING ERROR HANDLING
 *
 * ```java
 * @Test
 * void shouldReturn404WhenMemberNotFound() throws Exception {
 *     when(memberService.getMember(999L))
 *         .thenThrow(ResourceNotFoundException.member(999L));
 *
 *     mockMvc.perform(get("/api/members/999"))
 *         .andExpect(status().isNotFound())
 *         .andExpect(jsonPath("$.status").value(404))
 *         .andExpect(jsonPath("$.message").value("Member not found with ID: 999"));
 * }
 * ```
 *
 * ERROR RESPONSE CONSISTENCY
 *
 * All errors follow same format:
 * - timestamp: When error occurred
 * - status: HTTP status code
 * - error: HTTP status phrase
 * - message: Human-readable error message
 * - path: Request path that caused error
 * - errors: (Optional) Field-specific validation errors
 *
 * CLIENT HANDLING
 *
 * ```javascript
 * try {
 *     const response = await fetch('/api/members/999', {
 *         headers: {'Authorization': `Bearer ${token}`}
 *     });
 *
 *     if (!response.ok) {
 *         const error = await response.json();
 *         console.error(`Error ${error.status}: ${error.message}`);
 *
 *         // Handle specific errors
 *         if (error.status === 404) {
 *             showNotFoundMessage();
 *         } else if (error.status === 400 && error.errors) {
 *             // Validation errors
 *             Object.keys(error.errors).forEach(field => {
 *                 showFieldError(field, error.errors[field]);
 *             });
 *         }
 *     }
 * } catch (err) {
 *     console.error('Network error:', err);
 * }
 * ```
 */
