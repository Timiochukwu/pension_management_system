package pension_management_system.pension.exception;

/**
 * Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException member(Long id) {
        return new ResourceNotFoundException("Member not found with ID: " + id);
    }

    public static ResourceNotFoundException contribution(Long id) {
        return new ResourceNotFoundException("Contribution not found with ID: " + id);
    }

    public static ResourceNotFoundException payment(Long id) {
        return new ResourceNotFoundException("Payment not found with ID: " + id);
    }

    public static ResourceNotFoundException benefit(Long id) {
        return new ResourceNotFoundException("Benefit not found with ID: " + id);
    }

    public static ResourceNotFoundException report(Long id) {
        return new ResourceNotFoundException("Report not found with ID: " + id);
    }
}
