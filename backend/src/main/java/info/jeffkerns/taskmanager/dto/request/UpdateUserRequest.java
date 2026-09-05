package info.jeffkerns.taskmanager.dto.request;

import info.jeffkerns.taskmanager.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ==============================================================================
 * Update User Request (Input DTO)
 * ==============================================================================
 * Payload accepted when updating an existing user's profile or account details.
 *
 * <p>Notice that password is not included here; password modifications should always
 * be handled through a dedicated change-password endpoint with current password verification.</p>
 *
 * @param username updated username (3-30 chars)
 * @param email    updated email address (valid format, max 250 chars)
 * @param role     updated role (optional; ignored for regular users)
 * ==============================================================================
 */
public record UpdateUserRequest(
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    String username,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 250, message = "Email cannot exceed 250 characters")
    String email,

    UserRole role
) {
    /**
     * Compact constructor: standardizes whitespace and converts email to lowercase.
     */
    public UpdateUserRequest {
        username = (username != null) ? username.strip() : null;
        email = (email != null) ? email.strip().toLowerCase() : null;
    }
}

