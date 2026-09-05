package info.jeffkerns.taskmanager.exception;

/**
 * ==============================================================================
 * Duplicate Task Exception (409 Conflict)
 * ==============================================================================
 * Thrown when attempting to create or rename a task with a title that is already
 * in use by another task in the database.
 * ==============================================================================
 */
public class DuplicateTaskException extends RuntimeException {

    /**
     * Constructs the conflict exception with title sanitization.
     *
     * @param title the conflicting task title
     * @throws IllegalArgumentException if title is blank after trimming
     */
    public DuplicateTaskException(String title) {
        // JDK 25 Flexible Constructor Bodies: input sanitization & guard checks prior to super()
        var trimmedTitle = (title != null) ? title.strip() : "";
        if (trimmedTitle.isBlank()) {
            throw new IllegalArgumentException("Task title cannot be blank");
        }
        super("A task with title '" + trimmedTitle + "' already exists");
    }
}

