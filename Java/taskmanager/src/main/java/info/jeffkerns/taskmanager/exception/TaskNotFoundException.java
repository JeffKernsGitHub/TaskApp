package info.jeffkerns.taskmanager.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(Long taskId) {
        super("Task with ID '" + taskId + "' was not found");
    }
}
