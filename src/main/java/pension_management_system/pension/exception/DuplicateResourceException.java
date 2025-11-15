package pension_management_system.pension.exception;

/**
 * DuplicateResourceException - Thrown when trying to create duplicate
 *
 * Usage:
 * throw DuplicateResourceException.email("user@example.com");
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public static DuplicateResourceException email(String email) {
        return new DuplicateResourceException("Email already exists: " + email);
    }

    public static DuplicateResourceException memberNumber(String memberNumber) {
        return new DuplicateResourceException("Member number already exists: " + memberNumber);
    }

    public static DuplicateResourceException paymentReference(String reference) {
        return new DuplicateResourceException("Payment reference already exists: " + reference);
    }
}
