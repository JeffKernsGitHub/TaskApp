package info.jeffkerns.taskmanager.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ==============================================================================
 * Register Request (Public Registration DTO)
 * ==============================================================================
 * Received by the public {@code POST /api/v1/auth/register} endpoint when a new
 * visitor creates an account.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>Password Policy Validation</b>:
 *       {@code @Size(min = 8, max = 64)} enforces a minimum length of 8 characters
 *       to prevent easily guessable passwords.</li>
 *
 *   <li><b>Why no Role field?</b>:
 *       Public registration should never allow the user to choose their own role.
 *       New registrations are always hardcoded to {@code UserRole.USER} in the service layer,
 *       preventing privilege escalation attacks.</li>
 * </ul>
 *
 * @param username unique username
 * @param email    valid email address
 * @param password raw plain-text password to be encrypted via BCrypt
 * ==============================================================================
 */
public record RegisterRequest(
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    String username,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    String password
) {
    /**
     * Compact constructor: standardizes whitespace and converts email to lowercase.
     */
    public RegisterRequest {
        username = (username != null) ? username.strip() : null;
        email = (email != null) ? email.strip().toLowerCase() : null;
    }
}

