package info.jeffkerns.taskmanager.exception;

/**
 * ==============================================================================
 * Resource Not Found Exception (Base 404 Exception)
 * ==============================================================================
 * Unchecked exception thrown whenever a requested database entity or domain
 * resource cannot be located.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>Checked vs. Unchecked Exceptions</b>:
 *       By extending {@link RuntimeException}, this is an <i>unchecked exception</i>.
 *       You do not have to declare {@code throws ResourceNotFoundException} on every method
 *       signature, and calling code is not forced to write boilerplate {@code try/catch} blocks.</li>
 *
 *   <li><b>Flexible Constructor Bodies (Java 22+ / 25)</b>:
 *       Prior to modern Java, {@code super(...)} had to be the <i>very first</i> statement
 *       in a constructor. In Java 22+, you can execute validation, sanitization, and variable
 *       preparation <i>before</i> calling {@code super(...)}.</li>
 * </ul>
 * ==============================================================================
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new not-found exception with a sanitized message.
     *
     * @param message descriptive message explaining what resource was missing
     */
    public ResourceNotFoundException(String message) {
        // JDK 25 Flexible Constructor Bodies: pre-super preparation
        var cleanMessage = (message != null) ? message.strip() : "Requested resource was not found";
        super(cleanMessage);
    }
}

