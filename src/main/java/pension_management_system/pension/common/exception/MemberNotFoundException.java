package pension_management_system.pension.common.exception;

/**
 * MemberNotFoundException
 *
 * Thrown when a member is not found in the database
 * This is a custom exception that extends RuntimeException
 *
 * RuntimeException = Unchecked exception (no need for try-catch)
 */
public class MemberNotFoundException extends RuntimeException{
    /**
     * Constructor with message
     * @param message Error message describing what wasn't found
     */
    public MemberNotFoundException(String message){
        super(message); // Pass message to parent class (RuntimeException)
    }
    /**
     * Constructor with message and cause
     * @param message Error message
     * @param cause Original exception that caused this
     */
    public MemberNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
}
