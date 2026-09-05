package info.jeffkerns.taskmanager.exception;

/**
 * ==============================================================================
 * Task Not Found Exception
 * ==============================================================================
 * Thrown when a task lookup by ID yields no result.
 * Inherits from {@link ResourceNotFoundException} so it is automatically handled
 * as an HTTP 404 by {@link GlobalExceptionHandler#handleResourceNotFound}.
 * ==============================================================================
 */
public class TaskNotFoundException extends ResourceNotFoundException {

    /**
     * Constructs the exception with pre-super ID validation.
     *
     * @param taskId the ID of the missing task
     * @throws IllegalArgumentException if taskId is null or not positive
     */
    public TaskNotFoundException(Long taskId) {
        // JDK 25 Flexible Constructor Bodies: statements and validation before super()
        if (taskId == null || taskId <= 0) {
            throw new IllegalArgumentException("Task ID must be a positive, non-null number");
        }
        super("Task with ID '" + taskId + "' was not found");
    }
}

