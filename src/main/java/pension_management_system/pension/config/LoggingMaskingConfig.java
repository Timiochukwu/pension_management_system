package pension_management_system.pension.config;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

/**
 * LoggingMaskingConfig - Mask sensitive data in logs
 *
 * Patterns masked:
 * - Email addresses (partially)
 * - Credit card numbers
 * - BVN numbers
 * - API keys/tokens
 * - Passwords
 */
public class LoggingMaskingConfig extends PatternLayout {

    private static final List<Pattern> PATTERNS = new ArrayList<>();

    static {
        // Email: show first 2 chars and domain
        PATTERNS.add(Pattern.compile("([a-zA-Z0-9._%+-]{2})[a-zA-Z0-9._%+-]*(@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})"));

        // Credit card: show last 4 digits
        PATTERNS.add(Pattern.compile("\\b\\d{12,16}\\b"));

        // BVN (11 digits): mask middle digits
        PATTERNS.add(Pattern.compile("\\b(\\d{3})\\d{5}(\\d{3})\\b"));

        // API keys/tokens: mask most of the value
        PATTERNS.add(Pattern.compile("(sk_[a-zA-Z]+_)[a-zA-Z0-9]{20,}"));
        PATTERNS.add(Pattern.compile("(FLWSECK_[A-Z]+-)[a-zA-Z0-9]{20,}"));

        // Password field in JSON
        PATTERNS.add(Pattern.compile("\"password\"\\s*:\\s*\"[^\"]+\""));

        // Authorization header
        PATTERNS.add(Pattern.compile("(Authorization:\\s*Bearer\\s+)[a-zA-Z0-9._-]+"));
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        String message = super.doLayout(event);
        return maskSensitiveData(message);
    }

    public static String maskSensitiveData(String message) {
        if (message == null) {
            return null;
        }

        String maskedMessage = message;

        // Email masking
        maskedMessage = maskedMessage.replaceAll(
            "([a-zA-Z0-9._%+-]{2})[a-zA-Z0-9._%+-]*(@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})",
            "$1***$2"
        );

        // Credit card masking
        maskedMessage = maskedMessage.replaceAll(
            "\\b(\\d{4})\\d{8,12}(\\d{4})\\b",
            "$1********$2"
        );

        // BVN masking (11 digits)
        maskedMessage = maskedMessage.replaceAll(
            "\\b(\\d{3})\\d{5}(\\d{3})\\b",
            "$1*****$2"
        );

        // API key masking
        maskedMessage = maskedMessage.replaceAll(
            "(sk_[a-zA-Z]+_)[a-zA-Z0-9]+",
            "$1****"
        );
        maskedMessage = maskedMessage.replaceAll(
            "(FLWSECK_[A-Z]+-)[a-zA-Z0-9]+",
            "$1****"
        );

        // Password in JSON
        maskedMessage = maskedMessage.replaceAll(
            "\"password\"\\s*:\\s*\"[^\"]+\"",
            "\"password\": \"****\""
        );

        // Authorization header
        maskedMessage = maskedMessage.replaceAll(
            "(Authorization:\\s*Bearer\\s+)[a-zA-Z0-9._-]+",
            "$1****"
        );

        return maskedMessage;
    }
}
