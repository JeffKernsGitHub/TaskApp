package info.jeffkerns.taskmanager.dto.response;

import info.jeffkerns.taskmanager.entity.TaskPriority;
import java.time.Instant;
import java.time.LocalDate;
import info.jeffkerns.taskmanager.entity.TaskStatus;

/**
 * ==============================================================================
 * Task Response (Output DTO)
 * ==============================================================================
 * Represents the complete JSON response returned to the API client when viewing tasks.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>Separation of Concerns</b>:
 *       Instead of returning the raw {@link info.jeffkerns.taskmanager.entity.TaskEntity},
 *       we return this immutable record. This allows us to format dates, exclude internal
 *       database details, and avoid infinite JSON recursion when serializing entity relationships.</li>
 *
 *   <li><b>Nested Summary DTO ({@link UserSummaryResponse})</b>:
 *       Notice that the {@code assignedUser} is typed as {@code UserSummaryResponse} rather than
 *       the full {@code UserEntity}. This prevents inadvertently exposing the user's password
 *       hash or internal timestamps!</li>
 * </ul>
 *
 * @param id           the unique database ID of the task
 * @param title        the task headline
 * @param description  detailed description
 * @param status       current status (TODO, IN_PROGRESS, DONE)
 * @param priority     priority level (LOW, MEDIUM, HIGH)
 * @param dueDate      target completion date
 * @param assignedUser summary of the user who owns/is assigned to this task
 * @param createdAt    timestamp when created
 * @param updatedAt    timestamp when last modified
 * ==============================================================================
 */
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