/**
 * ==============================================================================
 * User Service Unit Tests (JUnit 5 + Mockito)
 * ==============================================================================
 * Tests the business rules and security operations in {@link UserServiceImpl}.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>Testing Password Hashing in Isolation</b>:
 *       Rather than computing actual computationally-intensive BCrypt hashes,
 *       we mock {@link PasswordEncoder} to verify that passwords are <i>always</i>
 *       encoded before entities are saved to the database.</li>
 *
 *   <li><b>Testing Business Constraint Violations</b>:
 *       We verify that attempting to register an existing username or email address
 *       is caught by the service layer and throws {@link DuplicateUserException}
 *       before calling the database.</li>
 * </ul>
 * ==============================================================================
 */
package info.jeffkerns.taskmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import info.jeffkerns.taskmanager.dto.request.CreateUserRequest;
import info.jeffkerns.taskmanager.dto.response.UserResponse;
import info.jeffkerns.taskmanager.entity.UserEntity;
import info.jeffkerns.taskmanager.entity.UserRole;
import info.jeffkerns.taskmanager.exception.DuplicateUserException;
import info.jeffkerns.taskmanager.exception.UserNotFoundException;
import info.jeffkerns.taskmanager.repository.UserRepository;
import info.jeffkerns.taskmanager.service.impl.UserServiceImpl;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests (Mockito)")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    // =========================================================================
    // 1. createUser Tests
    // =========================================================================

    @Test
    @DisplayName("createUser: Successfully hashes password and saves new user")
    void createUser_ValidRequest_EncodesPasswordAndPersistsUser() {
        // Arrange
        var request = new CreateUserRequest(
            "john_doe",
            "john@example.com",
            "PlainPassword123!",
            UserRole.USER
        );

        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("PlainPassword123!")).thenReturn("$2a$10$hashedPasswordHere...");

        var savedEntity = new UserEntity(
            "john_doe",
            "john@example.com",
            "$2a$10$hashedPasswordHere...",
            UserRole.USER
        );
        ReflectionTestUtils.setField(savedEntity, "id", 10L);

        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        // Act
        var response = userService.createUser(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.username()).isEqualTo("john_doe");
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.role()).isEqualTo(UserRole.USER);

        // Verify passwordEncoder.encode was called with the raw password
        verify(passwordEncoder, times(1)).encode("PlainPassword123!");
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("createUser: Throws DuplicateUserException when username already exists")
    void createUser_DuplicateUsername_ThrowsException() {
        // Arrange
        var request = new CreateUserRequest(
            "existing_user",
            "test@example.com",
            "Password123!",
            UserRole.USER
        );
        when(userRepository.existsByUsername("existing_user")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(DuplicateUserException.class)
            .hasMessageContaining("existing_user");

        // Database save and password encoding should NEVER occur on collision
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser: Throws DuplicateUserException when email already exists")
    void createUser_DuplicateEmail_ThrowsException() {
        // Arrange
        var request = new CreateUserRequest(
            "unique_user",
            "taken@example.com",
            "Password123!",
            UserRole.USER
        );
        when(userRepository.existsByUsername("unique_user")).thenReturn(false);
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(DuplicateUserException.class)
            .hasMessageContaining("taken@example.com");

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    // =========================================================================
    // 2. getUserById Tests
    // =========================================================================

    @Test
    @DisplayName("getUserById: Returns UserResponse when user exists")
    void getUserById_ExistingId_ReturnsUserResponse() {
        // Arrange
        var userId = 1L;
        UserEntity existing = new UserEntity("bob", "bob@example.com", "hash", UserRole.USER);
        ReflectionTestUtils.setField(existing, "id", userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));

        // Act
        var response = userService.getUserById(userId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.username()).isEqualTo("bob");
        assertThat(response.email()).isEqualTo("bob@example.com");
    }

    @Test
    @DisplayName("getUserById: Throws UserNotFoundException when user is missing")
    void getUserById_MissingId_ThrowsUserNotFoundException() {
        // Arrange
        var missingId = 404L;
        when(userRepository.findById(missingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(missingId))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining(String.valueOf(missingId));
    }

    // =========================================================================
    // 3. deleteUser Tests
    // =========================================================================

    @Test
    @DisplayName("deleteUser: Deletes user when ID exists")
    void deleteUser_ExistingId_CallsDeleteById() {
        // Arrange
        var userId = 5L;
        when(userRepository.existsById(userId)).thenReturn(true);

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("deleteUser: Throws UserNotFoundException when user ID does not exist")
    void deleteUser_MissingId_ThrowsException() {
        // Arrange
        var missingId = 99L;
        when(userRepository.existsById(missingId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(missingId))
            .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).deleteById(anyLong());
    }
}
