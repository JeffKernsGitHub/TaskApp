package info.jeffkerns.taskmanager.mapper;

import info.jeffkerns.taskmanager.dto.response.UserResponse;
import info.jeffkerns.taskmanager.entity.UserEntity;

public final class UserMapper {

    private UserMapper() {}

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
