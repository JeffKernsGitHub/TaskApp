package info.jeffkerns.taskmanager.service;

import info.jeffkerns.taskmanager.dto.request.LoginRequest;
import info.jeffkerns.taskmanager.dto.request.RegisterRequest;
import info.jeffkerns.taskmanager.dto.response.AuthResponse;
import info.jeffkerns.taskmanager.dto.response.UserSummaryResponse;

/**
 * ==============================================================================
 * Authentication Service (Interface)
 * ==============================================================================
 * Defines operations for user onboarding (registration) and authentication (login).
 * ==============================================================================
 */
public interface AuthService {

    /**
     * Registers a new user account with default USER role.
     *
     * @param request registration details (username, email, plain password)
     * @return summary response of the newly registered user
     * @throws info.jeffkerns.taskmanager.exception.DuplicateUserException if username or email already exists
     */
    UserSummaryResponse register(RegisterRequest request);

    /**
     * Authenticates a user's credentials and issues a signed JWT access token.
     *
     * @param request login credentials (username and password)
     * @return authentication response containing the signed JWT and expiry time
     * @throws org.springframework.security.core.AuthenticationException if credentials are invalid
     */
    AuthResponse login(LoginRequest request);
}

