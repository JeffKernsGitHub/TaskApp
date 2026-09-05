package info.jeffkerns.taskmanager.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * ==============================================================================
 * Login Request (Authentication DTO)
 * ==============================================================================
 * Carries username and password credentials supplied by a client attempting to log in
 * via {@code POST /api/v1/auth/login}.
 *
 * @param username the user's login username (must not be blank)
 * @param password the user's plain-text password to be verified against the BCrypt hash
 * ==============================================================================
 */
public record LoginRequest(
    @NotBlank(message = "Username is required")
    String username,

    @NotBlank(message = "Password is required")
    String password
) {
    /**
     * Compact constructor: removes surrounding whitespace from username.
     */
    public LoginRequest {
        username = (username != null) ? username.strip() : null;
    }
}

