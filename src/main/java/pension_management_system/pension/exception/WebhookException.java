package pension_management_system.pension.exception;

/**
 * WebhookException - Thrown for webhook-related errors
 */
public class WebhookException extends RuntimeException {

    public WebhookException(String message) {
        super(message);
    }

    public WebhookException(String message, Throwable cause) {
        super(message, cause);
    }

    public static WebhookException deliveryFailed(Throwable cause) {
        return new WebhookException("Webhook delivery failed", cause);
    }

    public static WebhookException signatureFailed(Throwable cause) {
        return new WebhookException("Failed to generate HMAC signature", cause);
    }
}
