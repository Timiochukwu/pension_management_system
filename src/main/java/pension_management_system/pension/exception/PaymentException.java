package pension_management_system.pension.exception;

/**
 * PaymentException - Thrown for payment-related errors
 */
public class PaymentException extends RuntimeException {

    public PaymentException(String message) {
        super(message);
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }

    public static PaymentException alreadyPaid() {
        return new PaymentException("Contribution already paid");
    }

    public static PaymentException amountMismatch() {
        return new PaymentException("Payment amount does not match contribution amount");
    }

    public static PaymentException unsupportedGateway(String gateway) {
        return new PaymentException("Unsupported payment gateway: " + gateway);
    }

    public static PaymentException verificationFailed(Throwable cause) {
        return new PaymentException("Payment verification failed", cause);
    }

    public static PaymentException initializationFailed(String gateway, Throwable cause) {
        return new PaymentException("Failed to initialize " + gateway + " payment", cause);
    }
}
