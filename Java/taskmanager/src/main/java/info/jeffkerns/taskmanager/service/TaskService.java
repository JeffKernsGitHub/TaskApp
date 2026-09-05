package info.jeffkerns.taskmanager.service;

import info.jeffkerns.taskmanager.dto.request.CreateTaskRequest;
import info.jeffkerns.taskmanager.dto.request.UpdateTaskRequest;
import info.jeffkerns.taskmanager.dto.response.TaskResponse;
import info.jeffkerns.taskmanager.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * ==============================================================================
 * Task Service (Business Logic Interface)
 * ==============================================================================
 * Defines the contract (public API) for task management business operations.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>Interface-Based Programming</b>:
 *       In professional Java enterprise applications, we define an interface for the service
 *       contract and a separate implementation class (e.g., {@code TaskServiceImpl}).
 *       This provides:
 *       <ol>
 *         <li>Loose coupling: Controllers depend on this interface, not the concrete implementation.</li>
 *         <li>Easy unit testing: In tests, you can easily mock this interface.</li>
 *         <li>Spring AOP support: Spring creates dynamic proxies around interfaces to handle
 *             cross-cutting concerns like {@code @Transactional} and security checks.</li>
 *       </ol>
 *   </li>
 * </ul>
 * ==============================================================================
 */
public interface TaskService {

    /**
     * Searches and lists tasks with optional filtering and pagination.
     *
     * @param status   optional status filter (e.g. TODO, IN_PROGRESS, DONE)
     * @param search   optional case-insensitive keyword to match in the title
     * @param pageable pagination parameters (page number, page size, sort order)
     * @return a paginated page of task response DTOs
     */
    Page<TaskResponse> getTasks(TaskStatus status, String search, Pageable pageable);

    /**
     * Retrieves a single task by its database primary key ID.
     *
     * @param id the task's unique ID
     * @return the task response DTO
     * @throws info.jeffkerns.taskmanager.exception.TaskNotFoundException if no task matches the given ID
     */
    TaskResponse getTaskById(Long id);

    /**
     * Creates a new task based on the provided request DTO.
     *
     * @param request validated input payload containing title, status, priority, etc.
     * @return the newly persisted task response DTO with generated ID and timestamps
     */
    TaskResponse createTask(CreateTaskRequest request);

    /**
     * Updates an existing task's fields.
     *
     * @param id      the primary key ID of the task to update
     * @param request validated input payload with new values
     * @return the updated task response DTO
     * @throws info.jeffkerns.taskmanager.exception.TaskNotFoundException if no task matches the given ID
     */
    TaskResponse updateTask(Long id, UpdateTaskRequest request);

    /**
     * Deletes an existing task from the database.
     *
     * @param id the primary key ID of the task to delete
     * @throws info.jeffkerns.taskmanager.exception.TaskNotFoundException if no task matches the given ID
     */
    void deleteTask(Long id);
}

