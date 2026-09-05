package info.jeffkerns.taskmanager.exception;

/**
 * ==============================================================================
 * Duplicate User Exception (409 Conflict)
 * ==============================================================================
 * Thrown when attempting to register or update an account using a username or
 * email that is already registered.
 * ==============================================================================
 */
public class DuplicateUserException extends RuntimeException {

    /**
     * Constructs the duplicate user exception with sanitized error message.
     *
     * @param message descriptive explanation of the conflict
     */
    public DuplicateUserException(String message) {
        // JDK 25 Flexible Constructor Bodies: statements before super()
        var cleanMessage = (message != null) ? message.strip() : "A duplicate user already exists";
        super(cleanMessage);
    }
}

