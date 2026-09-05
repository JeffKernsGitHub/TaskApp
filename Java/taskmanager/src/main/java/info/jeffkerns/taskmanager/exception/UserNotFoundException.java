package info.jeffkerns.taskmanager.exception;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(Long userId) {
        // JDK 25 Flexible Constructor Bodies: statements and validation before super()
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be a positive, non-null number");
        }
        super("User with ID '" + userId + "' was not found");
    }
}
