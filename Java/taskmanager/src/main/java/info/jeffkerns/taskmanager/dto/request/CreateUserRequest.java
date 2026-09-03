package info.jeffkerns.taskmanager.dto.request;

import info.jeffkerns.taskmanager.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    String username,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 250, message = "Email cannot exceed 250 characters")
    String email,

    String password,

    UserRole role
) {}
