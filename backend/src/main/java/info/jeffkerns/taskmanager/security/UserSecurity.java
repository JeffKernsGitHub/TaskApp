package info.jeffkerns.taskmanager.security;

import info.jeffkerns.taskmanager.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ==============================================================================
 * User Security (Account Authorization Evaluator)
 * ==============================================================================
 * This class provides authorization checks for user management operations.
 *
 * It is invoked in Spring Security {@code @PreAuthorize} expressions to ensure
 * that non-admin users can only inspect or modify their own account data.
 *
 * <p>Example usage in a controller:
 * <pre>{@code @PreAuthorize("hasRole('ADMIN') or @userSecurity.isUserOwner(#id, authentication.name)")}</pre>
 * </p>
 * ==============================================================================
 */
@Component("userSecurity")
public class UserSecurity {

    private final UserRepository userRepository;

    /**
     * Constructor injection of {@link UserRepository}.
     *
     * @param userRepository repository for querying user entities
     */
    public UserSecurity(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Checks if the user entity with the given database ID has the specified username.
     *
     * @param userId   the primary key ID of the user record
     * @param username the username of the authenticated caller
     * @return {@code true} if the database user's username matches the caller's username,
     *         {@code false} otherwise
     */
    @Transactional(readOnly = true)
    public boolean isUserOwner(Long userId, String username) {
        return userRepository.findById(userId)
            .map(user -> username.equals(user.getUsername()))
            .orElse(false);
    }
}
