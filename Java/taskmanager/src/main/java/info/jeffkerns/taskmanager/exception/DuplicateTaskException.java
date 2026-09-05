package info.jeffkerns.taskmanager.exception;

public class DuplicateTaskException extends RuntimeException {
    public DuplicateTaskException(String title) {
        // JDK 25 Flexible Constructor Bodies: input sanitization & guard checks prior to super()
        var trimmedTitle = (title != null) ? title.strip() : "";
        if (trimmedTitle.isBlank()) {
            throw new IllegalArgumentException("Task title cannot be blank");
        }
        super("A task with title '" + trimmedTitle + "' already exists");
    }
}
