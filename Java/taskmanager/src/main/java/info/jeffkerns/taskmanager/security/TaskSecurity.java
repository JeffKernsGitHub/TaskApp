package info.jeffkerns.taskmanager.security;

import info.jeffkerns.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ==============================================================================
 * Task Security (Method-Level Authorization Evaluator)
 * ==============================================================================
 * This class provides custom authorization helper methods evaluated dynamically
 * by Spring Security at runtime.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>SpEL (Spring Expression Language)</b>:
 *       In controller endpoints or service methods, you often see annotations like:
 *       <pre>{@code @PreAuthorize("hasRole('ADMIN') or @taskSecurity.isTaskOwner(#id, authentication.name)")}</pre>
 *       The {@code @taskSecurity} syntax tells Spring: "Look up the Spring bean named
 *       'taskSecurity' and call its method passing the method argument (#id) and the
 *       currently logged-in user's name."</li>
 *
 *   <li><b>Resource-Based Access Control</b>:
 *       Roles like {@code ROLE_USER} are often not enough. A regular user should only be able
 *       to view, update, or delete <i>their own</i> tasks, not tasks belonging to other users.
 *       This class enforces that ownership boundary.</li>
 * </ul>
 * ==============================================================================
 */
@Component("taskSecurity")
public class TaskSecurity {

    private final TaskRepository taskRepository;

    /**
     * Constructor injection of the {@link TaskRepository}.
     *
     * @param taskRepository repository to load task entities from the database
     */
    public TaskSecurity(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Checks if the task with the given ID belongs to the user with the specified username.
     *
     * <p><b>Beginner Tip - {@code Optional.map(...).orElse(...)}:</b>
     * {@code taskRepository.findById(taskId)} returns an {@code Optional<TaskEntity>}.
     * Instead of an awkward {@code if (optional.isPresent())} check, Java's functional
     * {@code map()} executes the lambda if a task is found, and {@code orElse(false)}
     * safely returns {@code false} if the task does not exist in the database.</p>
     *
     * @param taskId   the primary key ID of the task to verify
     * @param username the username of the currently authenticated user
     * @return {@code true} if the task exists and is owned by the user, {@code false} otherwise
     */
    @Transactional(readOnly = true)
    public boolean isTaskOwner(Long taskId, String username) {
        return taskRepository.findById(taskId)
            .map(task -> task.getUser() != null && username.equals(task.getUser().getUsername()))
            .orElse(false);
    }
}
