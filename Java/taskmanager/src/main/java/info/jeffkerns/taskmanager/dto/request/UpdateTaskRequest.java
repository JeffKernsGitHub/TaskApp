package info.jeffkerns.taskmanager.dto.request;

import info.jeffkerns.taskmanager.entity.TaskPriority;
import info.jeffkerns.taskmanager.entity.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTaskRequest(
        @NotBlank(message = "Task title is required")
        @Size(min = 3, max = 120)
        String title,

        @Size(max = 256)
        String description,

        @NotNull(message = "Task status is required")
        TaskStatus status,

        @NotNull(message = "Task priority is required")
        TaskPriority priority,

        @FutureOrPresent
        LocalDate dueDate,

        Long assignedUserId
) {}