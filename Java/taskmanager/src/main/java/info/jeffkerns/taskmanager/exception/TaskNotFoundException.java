package info.jeffkerns.taskmanager.exception;

public class TaskNotFoundException extends ResourceNotFoundException {
    public TaskNotFoundException(Long taskId) {
        // JDK 25 Flexible Constructor Bodies: statements and validation before super()
        if (taskId == null || taskId <= 0) {
            throw new IllegalArgumentException("Task ID must be a positive, non-null number");
        }
        super("Task with ID '" + taskId + "' was not found");
    }
}
