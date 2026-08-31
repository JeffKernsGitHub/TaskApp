package info.jeffkerns.taskmanager.mapper;

import info.jeffkerns.taskmanager.dto.response.TaskResponse;
import info.jeffkerns.taskmanager.dto.response.UserSummaryResponse;
import info.jeffkerns.taskmanager.entity.TaskEntity;

import java.util.Optional;

public final class TaskMapper {

  private TaskMapper() {}

  public static TaskResponse toResponse(TaskEntity entity) {
    var userSummary = Optional.ofNullable(entity.getUser())
        .map(u -> new UserSummaryResponse(u.getId(), u.getUsername(), u.getEmail()))
        .orElse(null);

    return new TaskResponse(
        entity.getId(),
        entity.getTitle(),
        entity.getDescription(),
        entity.getStatus(),
        entity.getPriority(),
        entity.getDueDate(),
        userSummary,
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
  }
}
