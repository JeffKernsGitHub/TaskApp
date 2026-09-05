/**
 * ==============================================================================
 * Authentication Controller (Public Security Endpoints)
 * ==============================================================================
 * Exposes open endpoints for user onboarding (registration) and authentication (login).
 *
 * <p>These endpoints are publicly accessible without an existing JWT because
 * they are whitelisted in {@link info.jeffkerns.taskmanager.config.SecurityConfig}:
 * <pre>{@code .requestMatchers("/api/v1/auth/**").permitAll()}</pre>
 * </p>
 * ==============================================================================
 */
package info.jeffkerns.taskmanager.controller;

import info.jeffkerns.taskmanager.dto.request.LoginRequest;
import info.jeffkerns.taskmanager.dto.request.RegisterRequest;
import info.jeffkerns.taskmanager.dto.response.AuthResponse;
import info.jeffkerns.taskmanager.dto.response.UserSummaryResponse;
import info.jeffkerns.taskmanager.service.AuthService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Constructor injection of {@link AuthService}.
     *
     * @param authService service handling registration and login
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user account.
     *
     * <p><b>Example HTTP Request:</b>
     * <pre>POST /api/v1/auth/register
     * Content-Type: application/json
     *
     * { "username": "alice", "email": "alice@example.com", "password": "SecurePassword123!" }
     * </pre></p>
     *
     * @param request validated registration payload
     * @return 201 Created with Location header and user summary
     */
    @PostMapping("/register")
    public ResponseEntity<UserSummaryResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserSummaryResponse response = authService.register(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + response.id())).body(response);
    }

    /**
     * Authenticates user credentials and issues a signed JWT access token.
     *
     * <p><b>Example HTTP Request:</b>
     * <pre>POST /api/v1/auth/login
     * Content-Type: application/json
     *
     * { "username": "alice", "password": "SecurePassword123!" }
     * </pre></p>
     *
     * @param request validated login credentials
     * @return 200 OK with the JWT token and expiration metadata
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}

