/**
 * ==============================================================================
 * User Service Implementation (Business Logic Layer)
 * ==============================================================================
 * Implements business operations for managing user accounts, enforcing uniqueness
 * constraints, and securely hashing credentials.
 * ==============================================================================
 */
package info.jeffkerns.taskmanager.service.impl;

import info.jeffkerns.taskmanager.dto.request.CreateUserRequest;
import info.jeffkerns.taskmanager.dto.request.UpdateUserRequest;
import info.jeffkerns.taskmanager.dto.response.UserResponse;
import info.jeffkerns.taskmanager.entity.UserEntity;
import info.jeffkerns.taskmanager.entity.UserRole;
import info.jeffkerns.taskmanager.exception.DuplicateUserException;
import info.jeffkerns.taskmanager.exception.UserNotFoundException;
import info.jeffkerns.taskmanager.mapper.UserMapper;
import info.jeffkerns.taskmanager.repository.UserRepository;
import info.jeffkerns.taskmanager.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor injection: Spring supplies the UserRepository and the BCrypt PasswordEncoder.
     *
     * @param userRepository  data access repository for users
     * @param passwordEncoder cryptographic password encoder
     */
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retrieves all users in paginated format.
     */
    @Override
    public Page<UserResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserMapper::toResponse);
    }

    /**
     * Retrieves a user by their database ID, converting to DTO if present or throwing an exception.
     */
    @Override
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
            .map(UserMapper::toResponse)
            .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * Creates a new user account with duplicate validation and BCrypt password encryption.
     *
     * <p><b>Security Rule:</b> Never store user passwords directly as plain text.
     * {@code passwordEncoder.encode()} computes a secure one-way cryptographic hash with random salt.</p>
     */
    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // Enforce uniqueness constraints before attempting insertion
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUserException("A user with username '" + request.username() + "' already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException("A user with email '" + request.email() + "' already exists");
        }

        // Hash password with BCrypt (avoiding plain-text storage)
        String rawPassword = (request.password() != null && !request.password().isBlank())
            ? request.password()
            : "DefaultTemporaryPass123!";
        String hashedPassword = passwordEncoder.encode(rawPassword);

        UserEntity user = new UserEntity(
            request.username(),
            request.email(),
            hashedPassword,
            request.role() != null ? request.role() : UserRole.USER
        );

        UserEntity saved = userRepository.save(user);
        return UserMapper.toResponse(saved);
    }

    /**
     * Updates an existing user's details, checking for name and email collisions with other users.
     */
    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        UserEntity user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));

        // If username changed, verify the new username is not already taken by someone else
        if (!user.getUsername().equals(request.username()) && userRepository.existsByUsername(request.username())) {
            throw new DuplicateUserException("A user with username '" + request.username() + "' already exists");
        }

        // If email changed, verify the new email is not already taken by someone else
        if (!user.getEmail().equalsIgnoreCase(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException("A user with email '" + request.email() + "' already exists");
        }

        user.setUsername(request.username());
        user.setEmail(request.email());
        if (request.role() != null) {
            user.setRole(request.role());
        }

        // Changes automatically flushed on commit via Hibernate dirty checking
        return UserMapper.toResponse(user);
    }

    /**
     * Deletes a user by ID after verifying the record exists.
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }
}

