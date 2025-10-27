package pension_management_system.pension.common.exception;

public class DuplicateMonthlyContributionException extends RuntimeException {
    public DuplicateMonthlyContributionException(String message) {
        super(message);
    }
}
