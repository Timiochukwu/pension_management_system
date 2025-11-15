package pension_management_system.pension.exception;

/**
 * ResourceNotFoundException - Thrown when entity not found
 *
 * Usage:
 * throw ResourceNotFoundException.member(123L);
 * throw ResourceNotFoundException.contribution(456L);
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    // Factory methods for different resources
    public static ResourceNotFoundException member(Long id) {
        return new ResourceNotFoundException("Member not found with ID: " + id);
    }

    public static ResourceNotFoundException contribution(Long id) {
        return new ResourceNotFoundException("Contribution not found with ID: " + id);
    }

    public static ResourceNotFoundException payment(String reference) {
        return new ResourceNotFoundException("Payment not found with reference: " + reference);
    }

    public static ResourceNotFoundException benefit(Long id) {
        return new ResourceNotFoundException("Benefit not found with ID: " + id);
    }

    public static ResourceNotFoundException report(Long id) {
        return new ResourceNotFoundException("Report not found with ID: " + id);
    }

    public static ResourceNotFoundException employer(Long id) {
        return new ResourceNotFoundException("Employer not found with ID: " + id);
    }
}
