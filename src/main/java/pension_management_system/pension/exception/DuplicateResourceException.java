package pension_management_system.pension.exception;

/**
 * Exception thrown when attempting to create a duplicate resource
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public static DuplicateResourceException email(String email) {
        return new DuplicateResourceException("Email already exists: " + email);
    }

    public static DuplicateResourceException reference(String reference) {
        return new DuplicateResourceException("Reference already exists: " + reference);
    }
}
