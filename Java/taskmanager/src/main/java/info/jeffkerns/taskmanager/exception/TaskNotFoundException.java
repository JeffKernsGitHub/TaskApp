package info.jeffkerns.taskmanager.exception;

public class TaskNotFoundException extends ResourceNotFoundException {
    public TaskNotFoundException(Long taskId) {
        super("Task with ID '" + taskId + "' was not found");
    }
}
