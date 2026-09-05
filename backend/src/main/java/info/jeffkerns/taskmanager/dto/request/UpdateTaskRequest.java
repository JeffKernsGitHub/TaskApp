package info.jeffkerns.taskmanager.dto.request;

import info.jeffkerns.taskmanager.entity.TaskPriority;
import info.jeffkerns.taskmanager.entity.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * ==============================================================================
 * Update Task Request (Input DTO)
 * ==============================================================================
 * Represents the incoming JSON payload when an HTTP client updates an existing task
 * via {@code PUT /api/v1/tasks/{id}}.
 *
 * <p>Unlike creation, updates require both status and priority to be specified,
 * ensuring the task's full state is explicitly communicated.</p>
 *
 * @param title          updated task title (required, 3-120 chars)
 * @param description    updated task description (max 256 chars)
 * @param status         updated workflow status (required)
 * @param priority       updated priority level (required)
 * @param dueDate        updated due date (cannot be in the past)
 * @param assignedUserId updated assigned user ID
 * ==============================================================================
 */
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
) {
    /**
     * Compact canonical constructor for normalizing string input values.
     */
    public UpdateTaskRequest {
        title = (title != null) ? title.strip() : null;
        description = (description != null) ? description.strip() : null;
    }
}

