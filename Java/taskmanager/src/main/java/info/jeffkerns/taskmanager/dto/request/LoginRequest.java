package info.jeffkerns.taskmanager.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Username is required")
    String username,

    @NotBlank(message = "Password is required")
    String password
) {
    public LoginRequest {
        username = (username != null) ? username.strip() : null;
    }
}
