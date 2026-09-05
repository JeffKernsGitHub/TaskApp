package info.jeffkerns.taskmanager.exception;

public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException(String message) {
        // JDK 25 Flexible Constructor Bodies: statements before super()
        var cleanMessage = (message != null) ? message.strip() : "A duplicate user already exists";
        super(cleanMessage);
    }
}
