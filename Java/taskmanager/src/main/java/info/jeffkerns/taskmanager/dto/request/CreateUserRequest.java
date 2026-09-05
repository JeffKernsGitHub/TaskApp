package info.jeffkerns.taskmanager.dto.request;

import info.jeffkerns.taskmanager.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ==============================================================================
 * Create User Request (Input DTO)
 * ==============================================================================
 * Payload accepted when creating a new user through administrative endpoints.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>Normalization in Compact Constructor</b>:
 *       User emails are trimmed and converted to lowercase so that lookups like
 *       {@code test@example.com} and {@code Test@Example.com} match reliably.</li>
 *
 *   <li><b>{@code @Email} Annotation</b>:
 *       Ensures that the string provided by the client follows standard RFC email formatting
 *       (contains {@code @}, a domain name, and valid TLD).</li>
 * </ul>
 *
 * @param username desired unique username (3-30 chars)
 * @param email    user's contact email address
 * @param password initial password (optional for admin-created users, minimum 8 chars if provided)
 * @param role     user's initial role (defaults to USER if null)
 * ==============================================================================
 */
public record CreateUserRequest(
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    String username,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 250, message = "Email cannot exceed 250 characters")
    String email,

    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters if provided")
    String password,

    UserRole role
) {
    /**
     * Compact constructor: cleans up whitespace and standardizes email casing.
     */
    public CreateUserRequest {
        username = (username != null) ? username.strip() : null;
        email = (email != null) ? email.strip().toLowerCase() : null;
    }
}

