package info.jeffkerns.taskmanager.exception;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(Long userId) {
        super("User with ID '" + userId + "' was not found");
    }
}
