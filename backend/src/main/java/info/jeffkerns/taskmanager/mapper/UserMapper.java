package info.jeffkerns.taskmanager.mapper;

import info.jeffkerns.taskmanager.dto.response.UserResponse;
import info.jeffkerns.taskmanager.entity.UserEntity;

/**
 * ==============================================================================
 * User Mapper (Entity to DTO Transformation Utility)
 * ==============================================================================
 * Utility class that converts {@link UserEntity} database objects into client-facing
 * {@link UserResponse} DTOs.
 *
 * <p>Separating the conversion logic here keeps service and controller classes
 * clean and adheres to the Single Responsibility Principle (SRP).</p>
 * ==============================================================================
 */
public final class UserMapper {

    /** Private constructor prevents creating instances of this utility class. */
    private UserMapper() {}

    /**
     * Converts a {@link UserEntity} into a {@link UserResponse} DTO.
     *
     * <p><b>Beginner Tip:</b> Notice the defensive null check at the start.
     * If a caller passes {@code null}, it returns {@code null} instead of throwing
     * a {@link NullPointerException}.</p>
     *
     * @param entity the user database entity to transform
     * @return the resulting DTO, or {@code null} if the entity is null
     */
    public static UserResponse toResponse(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return new UserResponse(
            entity.getId(),
            entity.getUsername(),
            entity.getEmail(),
            entity.getRole(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}

