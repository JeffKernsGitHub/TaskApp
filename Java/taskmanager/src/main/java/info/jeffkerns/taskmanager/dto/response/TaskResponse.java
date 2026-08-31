package info.jeffkerns.taskmanager.dto.response;

import info.jeffkerns.taskmanager.entity.TaskPriority;
import java.time.Instant;
import java.time.LocalDate;
import info.jeffkerns.taskmanager.entity.TaskStatus;

public record TaskResponse(
    Long id,
    String title,
    String description,
    TaskStatus status,
    TaskPriority priority,
    LocalDate dueDate,
    UserSummaryResponse assignedUser,
    Instant createdAt,
    Instant updatedAt
) {}