package pension_management_system.pension.exception;

/**
 * BusinessException - Thrown when business rule violated
 *
 * Usage:
 * throw new BusinessException("Insufficient balance for benefit claim");
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
