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

    @Override
    @Transactional
    public UserSummaryResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUserException("Username already exists: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException("Email already exists: " + request.email());
        }

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

    @Override
    public AuthResponse login(LoginRequest request) {
        // Authenticate credentials against AuthenticationManager
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserEntity user = userRepository.findByUsername(request.username())
            .orElseThrow();

        String token = jwtService.generateToken(user);
        return AuthResponse.of(
            token,
            jwtProperties.expirationMs(),
            user.getUsername(),
            user.getRole().getAuthority()
        );
    }
}
