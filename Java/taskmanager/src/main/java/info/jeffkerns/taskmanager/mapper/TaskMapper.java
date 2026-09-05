package info.jeffkerns.taskmanager.mapper;

import info.jeffkerns.taskmanager.dto.response.TaskResponse;
import info.jeffkerns.taskmanager.dto.response.UserSummaryResponse;
import info.jeffkerns.taskmanager.entity.TaskEntity;

import java.util.Optional;

/**
 * ==============================================================================
 * Task Mapper (Entity to DTO Transformation Utility)
 * ==============================================================================
 * A pure utility class responsible for converting {@link TaskEntity} database
 * objects into {@link TaskResponse} DTOs.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>Utility Class Pattern</b>:
 *       <ol>
 *         <li>Declared {@code final} so it cannot be extended/subclassed.</li>
 *         <li>Has a {@code private} constructor so nobody can instantiate it (e.g. {@code new TaskMapper()} is illegal).</li>
 *         <li>All methods are {@code static}.</li>
 *       </ol>
 *   </li>
 *
 *   <li><b>Method Reference Friendly ({@code TaskMapper::toResponse})</b>:
 *       Because this method takes a single {@code TaskEntity} and returns a {@code TaskResponse},
 *       it can be passed as a Java method reference:
 *       <pre>{@code page.map(TaskMapper::toResponse)}</pre>
 *       which is a clean shorthand for {@code page.map(task -> TaskMapper.toResponse(task))}.</li>
 *
 *   <li><b>Handling Optional Associations</b>:
 *       A task may or may not have an assigned user (it can be unassigned).
 *       Using {@code Optional.ofNullable(entity.getUser())} safely maps the user to a
 *       {@link UserSummaryResponse} if present, or returns {@code null} without risking a
 *       {@link NullPointerException}.</li>
 * </ul>
 * ==============================================================================
 */
public final class TaskMapper {

  /** Private constructor prevents creating instances of this utility class. */
  private TaskMapper() {}

  /**
   * Transforms a persistent {@link TaskEntity} into an immutable {@link TaskResponse} DTO.
   *
   * @param entity the JPA task entity to convert
   * @return the populated response DTO
   */
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

