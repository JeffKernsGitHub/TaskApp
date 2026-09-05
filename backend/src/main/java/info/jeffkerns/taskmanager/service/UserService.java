package info.jeffkerns.taskmanager.service;

import info.jeffkerns.taskmanager.dto.request.CreateUserRequest;
import info.jeffkerns.taskmanager.dto.request.UpdateUserRequest;
import info.jeffkerns.taskmanager.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * ==============================================================================
 * User Service (Business Logic Interface)
 * ==============================================================================
 * Defines the contract for user account management, lookup, and administrative operations.
 *
 * <p>Controllers interact exclusively through this interface, keeping the presentation
 * layer decoupled from the persistence and hashing logic.</p>
 * ==============================================================================
 */
public interface UserService {

    /**
     * Lists users in paginated form.
     *
     * @param pageable pagination parameters
     * @return a page of user responses
     */
    Page<UserResponse> getUsers(Pageable pageable);

    /**
     * Retrieves a user by their database primary key ID.
     *
     * @param id the user ID
     * @return the user response DTO
     * @throws info.jeffkerns.taskmanager.exception.UserNotFoundException if user does not exist
     */
    UserResponse getUserById(Long id);

    /**
     * Creates and persists a new user account.
     *
     * @param request input payload containing username, email, password, and role
     * @return the newly created user response DTO
     * @throws info.jeffkerns.taskmanager.exception.DuplicateUserException if username or email already exists
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * Updates an existing user's profile information.
     *
     * @param id      the ID of the user to update
     * @param request the updated profile fields
     * @return the updated user response DTO
     * @throws info.jeffkerns.taskmanager.exception.UserNotFoundException if user does not exist
     * @throws info.jeffkerns.taskmanager.exception.DuplicateUserException if new username or email conflicts
     */
    UserResponse updateUser(Long id, UpdateUserRequest request);

    /**
     * Removes a user from the system by ID.
     *
     * @param id the ID of the user to delete
     * @throws info.jeffkerns.taskmanager.exception.UserNotFoundException if user does not exist
     */
    void deleteUser(Long id);
}

