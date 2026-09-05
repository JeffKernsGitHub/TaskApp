/**
 * ==============================================================================
 * Authentication Service Implementation (Security Business Logic)
 * ==============================================================================
 * Handles new account registration and credentials verification (login).
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>{@link AuthenticationManager#authenticate(org.springframework.security.core.Authentication)}</b>:
 *       Spring Security's standard entry point for validating credentials.
 *       Behind the scenes:
 *       <ol>
 *         <li>It calls our {@link org.springframework.security.authentication.dao.DaoAuthenticationProvider}.</li>
 *         <li>The provider uses {@link org.springframework.security.core.userdetails.UserDetailsService}
 *             to fetch the user's details and password hash from PostgreSQL.</li>
 *         <li>It compares the submitted plain password against the stored BCrypt hash.</li>
 *         <li>If passwords don't match, it throws a {@link org.springframework.security.core.AuthenticationException}
 *             (like {@code BadCredentialsException}), triggering an HTTP 401 response!</li>
 *       </ol>
 *   </li>
 * </ul>
 * ==============================================================================
 */
package info.jeffkerns.taskmanager.service.impl;

import info.jeffkerns.taskmanager.config.JwtProperties;
import info.jeffkerns.taskmanager.dto.request.LoginRequest;
import info.jeffkerns.taskmanager.dto.request.RegisterRequest;
import info.jeffkerns.taskmanager.dto.response.AuthResponse;
import info.jeffkerns.taskmanager.dto.response.UserSummaryResponse;
import info.jeffkerns.taskmanager.entity.UserEntity;
import info.jeffkerns.taskmanager.entity.UserRole;
import info.jeffkerns.taskmanager.exception.DuplicateUserException;
import info.jeffkerns.taskmanager.repository.UserRepository;
import info.jeffkerns.taskmanager.service.AuthService;
import info.jeffkerns.taskmanager.service.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    /**
     * Constructor injection of all authentication infrastructure beans.
     */
    public AuthServiceImpl(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        JwtProperties jwtProperties
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    /**
     * Registers a new user account.
     * Enforces username/email uniqueness and encrypts the password with BCrypt.
     */
    @Override
    @Transactional
    public UserSummaryResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUserException("Username already exists: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException("Email already exists: " + request.email());
        }

        // Encrypt the plain-text password before saving
        String hashedPassword = passwordEncoder.encode(request.password());
        UserEntity user = new UserEntity(
            request.username(),
            request.email(),
            hashedPassword,
            UserRole.USER
        );

        UserEntity saved = userRepository.save(user);
        return new UserSummaryResponse(saved.getId(), saved.getUsername(), saved.getEmail());
    }

    /**
     * Authenticates user credentials and returns a signed JWT token.
     *
     * @param request credentials payload (username, password)
     * @return {@link AuthResponse} containing the access token and expiry
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        // 1. Authenticate credentials against AuthenticationManager.
        // If password does not match, an AuthenticationException is thrown here.
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        // 2. Load the authenticated user entity to read roles and claims
        UserEntity user = userRepository.findByUsername(request.username())
            .orElseThrow();

        // 3. Generate a signed JWT token containing subject and role claims
        String token = jwtService.generateToken(user);

        // 4. Return formatted response with token and TTL
        return AuthResponse.of(
            token,
            jwtProperties.expirationMs(),
            user.getUsername(),
            user.getRole().getAuthority()
        );
    }
}

