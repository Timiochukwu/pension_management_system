package pension_management_system.pension.exception;

/**
 * ReportException - Thrown for report generation errors
 */
public class ReportException extends RuntimeException {

    public ReportException(String message) {
        super(message);
    }

    public ReportException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ReportException quotaExceeded() {
        return new ReportException("Storage quota exceeded. Please delete old reports.");
    }

    public static ReportException generationFailed(Throwable cause) {
        return new ReportException("Report generation failed", cause);
    }

    public static ReportException notReady(String status) {
        return new ReportException("Report is not ready for download. Status: " + status);
    }

    public static ReportException fileNotFound(String filePath) {
        return new ReportException("Report file not found or not readable: " + filePath);
    }

    public static ReportException loadFailed(Throwable cause) {
        return new ReportException("Failed to load report file", cause);
    }
}
