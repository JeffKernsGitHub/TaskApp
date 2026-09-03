package info.jeffkerns.taskmanager.dto.response;

import info.jeffkerns.taskmanager.entity.UserRole;
import java.time.Instant;

public record UserResponse(
    Long id,
    String username,
    String email,
    UserRole role,
    Instant createdAt,
    Instant updatedAt
) {}
