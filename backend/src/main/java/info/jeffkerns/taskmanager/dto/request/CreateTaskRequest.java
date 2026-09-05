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
 * Create Task Request (Input DTO)
 * ==============================================================================
 * Represents the incoming JSON payload when an HTTP client creates a new task
 * via {@code POST /api/v1/tasks}.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>DTO (Data Transfer Object) Pattern</b>:
 *       Instead of accepting raw database entities ({@code TaskEntity}) directly from
 *       the HTTP request body, we accept a dedicated DTO. This protects the database
 *       from malicious clients trying to manipulate internal columns like {@code id},
 *       {@code createdAt}, or {@code user_id} directly.</li>
 *
 *   <li><b>Jakarta Bean Validation ({@code @Valid})</b>:
 *       Annotations like {@code @NotBlank} and {@code @Size} automatically validate the
 *       incoming JSON before controller code runs. If validation fails, Spring rejects
 *       the request immediately with an HTTP 400 Bad Request.</li>
 *
 *   <li><b>Compact Canonical Constructor</b>:
 *       Notice the {@code public CreateTaskRequest { ... }} block below without parentheses.
 *       In Java records, this allows you to sanitize or validate data (like calling {@code .strip()})
 *       before the record's immutable fields are assigned.</li>
 * </ul>
 *
 * @param title          the required title of the task (3 to 120 characters)
 * @param description    optional extended details (max 256 characters)
 * @param status         initial status (e.g. TODO; optional, defaults in entity)
 * @param priority       urgency level (LOW, MEDIUM, HIGH; required)
 * @param dueDate        deadline for the task (cannot be a past date)
 * @param assignedUserId optional ID of the user assigned to this task
 * ==============================================================================
 */
public record CreateTaskRequest(
        @NotBlank(message = "Task title is required")
        @Size(min = 3, max = 120, message = "Title must be between 3 and 120 characters")
        String title,

        @Size(max = 256, message = "Description cannot exceed 256 characters")
        String description,

        TaskStatus status,

        @NotNull(message = "Task priority is required")
        TaskPriority priority,

        @FutureOrPresent(message = "Due date cannot be in the past")
        LocalDate dueDate,

        Long assignedUserId
) {
    /**
     * Compact constructor for data normalization.
     * Trims leading/trailing whitespace from string inputs.
     */
    public CreateTaskRequest {
        title = (title != null) ? title.strip() : null;
        description = (description != null) ? description.strip() : null;
    }
}

