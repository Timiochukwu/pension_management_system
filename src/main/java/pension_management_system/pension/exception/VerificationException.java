package pension_management_system.pension.exception;

/**
 * VerificationException - Thrown for BVN/verification errors
 */
public class VerificationException extends RuntimeException {

    public VerificationException(String message) {
        super(message);
    }

    public VerificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public static VerificationException alreadyExists() {
        return new VerificationException("Member already has a BVN verification record");
    }

    public static VerificationException apiFailed(String message) {
        return new VerificationException("BVN API call failed: " + message);
    }

    public static VerificationException requestBuildFailed(Throwable cause) {
        return new VerificationException("Failed to build API request", cause);
    }
}
