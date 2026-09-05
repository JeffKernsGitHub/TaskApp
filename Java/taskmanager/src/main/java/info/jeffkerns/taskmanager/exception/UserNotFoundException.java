package info.jeffkerns.taskmanager.exception;

/**
 * ==============================================================================
 * User Not Found Exception
 * ==============================================================================
 * Thrown when an account lookup by ID fails.
 * Extends {@link ResourceNotFoundException} for unified HTTP 404 translation.
 * ==============================================================================
 */
public class UserNotFoundException extends ResourceNotFoundException {

    /**
     * Constructs the exception with pre-super ID validation.
     *
     * @param userId the ID of the missing user
     * @throws IllegalArgumentException if userId is null or not positive
     */
    public UserNotFoundException(Long userId) {
        // JDK 25 Flexible Constructor Bodies: statements and validation before super()
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be a positive, non-null number");
        }
        super("User with ID '" + userId + "' was not found");
    }
}

