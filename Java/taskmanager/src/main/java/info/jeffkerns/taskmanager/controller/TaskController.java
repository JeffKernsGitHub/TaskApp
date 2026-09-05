/**
 * ==============================================================================
 * Task Controller (REST API Presentation Layer)
 * ==============================================================================
 * Exposes HTTP REST API endpoints for client applications (e.g. Angular web app,
 * mobile app, or curl/Postman) to perform task operations.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>{@code @RestController}</b>:
 *       A convenience annotation combining {@code @Controller} and {@code @ResponseBody}.
 *       It signals that returned Java objects should be serialized directly into JSON
 *       in the HTTP response body using Jackson.</li>
 *
 *   <li><b>{@code @RequestMapping("/api/v1/tasks")}</b>:
 *       Specifies the base URL path for all endpoints in this controller.
 *       Versioning your API ({@code /api/v1/}) is an industry best practice so future
 *       breaking changes can live at {@code /api/v2/} without disrupting existing clients.</li>
 *
 *   <li><b>{@link ResponseEntity}&lt;T&gt;</b>:
 *       Represents the entire HTTP response, including:
 *       <ol>
 *         <li>Status code (e.g. 200 OK, 201 Created, 204 No Content, 404 Not Found).</li>
 *         <li>HTTP response headers (e.g. {@code Location}, {@code Content-Type}).</li>
 *         <li>Response body payload.</li>
 *       </ol>
 *   </li>
 * </ul>
 * ==============================================================================
 */
package info.jeffkerns.taskmanager.controller;

import info.jeffkerns.taskmanager.dto.request.CreateTaskRequest;
import info.jeffkerns.taskmanager.dto.request.UpdateTaskRequest;
import info.jeffkerns.taskmanager.dto.response.TaskResponse;
import info.jeffkerns.taskmanager.entity.TaskStatus;
import info.jeffkerns.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    /**
     * Constructor injection of {@link TaskService}.
     *
     * @param taskService service managing task operations
     */
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Retrieves a paginated list of tasks with optional filtering.
     *
     * <p><b>Example HTTP Request:</b>
     * {@code GET /api/v1/tasks?status=TODO&search=clean&page=0&size=10&sort=dueDate,asc}</p>
     *
     * @param status   optional query parameter to filter by task status
     * @param search   optional query parameter to search text in titles
     * @param pageable page, size, and sort options (defaults to page 0, size 10, newest first)
     * @return 200 OK with paginated tasks
     */
    @GetMapping
    public ResponseEntity<Page<TaskResponse>> listTasks(
        @RequestParam(required = false) TaskStatus status,
        @RequestParam(required = false) String search,
        @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(taskService.getTasks(status, search, pageable));
    }

    /**
     * Retrieves a single task by its database ID.
     *
     * <p><b>Example HTTP Request:</b>
     * {@code GET /api/v1/tasks/42}</p>
     *
     * @param id the primary key extracted from the URL path
     * @return 200 OK with the task response
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    /**
     * Creates a new task.
     *
     * <p><b>Beginner Tip - {@code @Valid} and {@code Location} Header:</b>
     * <ul>
     *   <li>{@code @Valid} instructs Spring to run validation constraints (e.g. {@code @NotBlank})
     *       on {@link CreateTaskRequest} before invoking this method.</li>
     *   <li>{@code ServletUriComponentsBuilder} creates a standard REST {@code Location} header
     *       (e.g. {@code Location: /api/v1/tasks/42}) indicating where the newly created resource lives.</li>
     *   <li>Returns HTTP 201 Created instead of 200 OK.</li>
     * </ul>
     * </p>
     *
     * @param request validated JSON body
     * @return 201 Created with Location header and task body
     */
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponse created = taskService.createTask(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Updates an existing task.
     *
     * <p><b>Beginner Tip - {@code @PreAuthorize}:</b>
     * Uses Spring Expression Language (SpEL) to ensure only administrators OR the user
     * who owns this specific task can update it.</p>
     *
     * @param id      task ID from path
     * @param request validated update payload
     * @return 200 OK with the updated task
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.isTaskOwner(#id, authentication.name)")
    public ResponseEntity<TaskResponse> updateTask(
        @PathVariable Long id,
        @Valid @RequestBody UpdateTaskRequest request
    ) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    /**
     * Deletes a task by ID.
     *
     * @param id task ID from path
     * @return 204 No Content (standard REST response for successful deletion)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.isTaskOwner(#id, authentication.name)")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}

