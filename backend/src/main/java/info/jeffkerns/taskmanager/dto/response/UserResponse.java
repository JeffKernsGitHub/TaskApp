package info.jeffkerns.taskmanager.dto.response;

import info.jeffkerns.taskmanager.entity.UserRole;
import java.time.Instant;

/**
 * ==============================================================================
 * User Response (Output DTO)
 * ==============================================================================
 * Represents the public user representation returned in API responses.
 *
 * <p><b>Security Tip for Beginners:</b>
 * Notice what is <i>missing</i> from this record: {@code passwordHash}.
 * By returning a dedicated {@code UserResponse} DTO rather than {@link info.jeffkerns.taskmanager.entity.UserEntity},
 * there is zero risk of leaking the hashed password to clients.</p>
 *
 * @param id        user database primary key ID
 * @param username  user's unique username
 * @param email     user's email address
 * @param role      assigned system role (USER, ADMIN)
 * @param createdAt account creation timestamp
 * @param updatedAt account last update timestamp
 * ==============================================================================
 */
public record UserResponse(
    Long id,
    String username,
    String email,
    UserRole role,
    Instant createdAt,
    Instant updatedAt
) {}

