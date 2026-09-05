/**
 * ==============================================================================
 * Task Service Implementation (Business Logic Layer)
 * ==============================================================================
 * Implements the core business logic for managing tasks.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>{@code @Service}</b>:
 *       Marks this class as a business service bean managed by Spring's IoC container.</li>
 *
 *   <li><b>{@code @Transactional(readOnly = true)} at Class Level</b>:
 *       By default, all methods in this class execute in a read-only database transaction.
 *       This provides significant performance benefits:
 *       <ol>
 *         <li>Hibernate disables dirty-checking snapshots, reducing CPU and memory overhead.</li>
 *         <li>PostgreSQL can optimize query execution and route reads to read-replicas.</li>
 *       </ol>
 *   </li>
 *
 *   <li><b>{@code @Transactional} at Method Level</b>:
 *       Methods that mutate data (create, update, delete) override the class-level annotation
 *       with read-write transactions. If an unhandled {@link RuntimeException} occurs,
 *       Spring automatically rolls back the transaction, leaving the database intact!</li>
 *
 *   <li><b>Hibernate Dirty Checking</b>:
 *       Notice that {@code updateTask} does NOT call {@code repository.save(task)}!
 *       When an entity is loaded within a {@code @Transactional} method, Hibernate tracks
 *       any changes made via setters. When the transaction commits, Hibernate automatically
 *       generates and executes the SQL {@code UPDATE} statement.</li>
 * </ul>
 * ==============================================================================
 */
package info.jeffkerns.taskmanager.service.impl;

import info.jeffkerns.taskmanager.dto.request.CreateTaskRequest;
import info.jeffkerns.taskmanager.dto.request.UpdateTaskRequest;
import info.jeffkerns.taskmanager.dto.response.TaskResponse;
import info.jeffkerns.taskmanager.entity.TaskEntity;
import info.jeffkerns.taskmanager.entity.TaskStatus;
import info.jeffkerns.taskmanager.entity.UserEntity;
import info.jeffkerns.taskmanager.exception.ResourceNotFoundException;
import info.jeffkerns.taskmanager.exception.TaskNotFoundException;
import info.jeffkerns.taskmanager.mapper.TaskMapper;
import info.jeffkerns.taskmanager.repository.TaskRepository;
import info.jeffkerns.taskmanager.repository.UserRepository;
import info.jeffkerns.taskmanager.service.TaskService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

  private final TaskRepository taskRepository;
  private final UserRepository userRepository;

  /**
   * Constructor Injection: Spring provides the repository dependencies when creating this service.
   *
   * @param taskRepository data access repository for tasks
   * @param userRepository data access repository for users
   */
  public TaskServiceImpl(TaskRepository taskRepository, UserRepository userRepository) {
    this.taskRepository = taskRepository;
    this.userRepository = userRepository;
  }

  /**
   * Searches and retrieves tasks matching optional filter criteria in paginated form.
   *
   * <p><b>Beginner Tip - JPA Specification and JDK 22+ Unnamed Variable:</b>
   * {@code Specification<TaskEntity>} lets us build dynamic SQL WHERE clauses at runtime.
   * In {@code (root, _, cb) -> ...}, {@code _} is an unnamed variable representing the unused
   * {@code CriteriaQuery} parameter (a modern Java feature to indicate an unused lambda parameter).
   * {@code root} represents the {@code TaskEntity} table columns, and {@code cb} is the
   * {@code CriteriaBuilder} providing SQL comparison operators (e.g. {@code equal}, {@code like}).</p>
   */
  @Override
  public Page<TaskResponse> getTasks(TaskStatus status, String search, Pageable pageable) {
    Specification<TaskEntity> spec = (root, _, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      // If a status filter is requested, add WHERE status = ?
      if (status != null) {
        predicates.add(cb.equal(root.get("status"), status));
      }

      // If a search keyword is provided, add WHERE LOWER(title) LIKE %search%
      if (search != null && !search.isBlank()) {
        predicates.add(cb.like(cb.lower(root.get("title")), "%" + search.strip().toLowerCase() + "%"));
      }

      // If no filters were added, return null (meaning no WHERE clause)
      // Otherwise combine all predicates with SQL AND: WHERE condition1 AND condition2
      return predicates.isEmpty() ? null : cb.and(predicates.toArray(Predicate[]::new));
    };

    // Execute query and convert each TaskEntity on the page into a TaskResponse DTO
    return taskRepository.findAll(spec, pageable)
        .map(TaskMapper::toResponse);
  }

  /**
   * Finds a task by ID or throws an exception.
   */
  @Override
  public TaskResponse getTaskById(Long id) {
    var task = taskRepository.findById(id)
        .orElseThrow(() -> new TaskNotFoundException(id));
    return TaskMapper.toResponse(task);
  }

  /**
   * Creates and persists a new task entity.
   * {@code @Transactional} ensures write operations are committed atomically.
   */
  @Override
  @Transactional
  public TaskResponse createTask(CreateTaskRequest request) {
    UserEntity assignedUser = null;
    if (request.assignedUserId() != null) {
      assignedUser = userRepository.findById(request.assignedUserId())
          .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.assignedUserId()));
    }

    var entity = new TaskEntity(
        request.title(),
        request.description(),
        request.status(),
        request.priority(),
        request.dueDate(),
        assignedUser
    );

    var saved = taskRepository.save(entity);
    return TaskMapper.toResponse(saved);
  }

  /**
   * Updates an existing task entity.
   *
   * <p><b>Beginner Tip - Automatic Dirty Checking:</b>
   * Notice that we do not call {@code taskRepository.save(task)}!
   * Within a {@code @Transactional} method, Hibernate automatically tracks mutations made to
   * managed entities and flushes the SQL {@code UPDATE} statement to the database on commit.</p>
   */
  @Override
  @Transactional
  public TaskResponse updateTask(Long id, UpdateTaskRequest request) {
    var task = taskRepository.findById(id)
        .orElseThrow(() -> new TaskNotFoundException(id));

    task.setTitle(request.title());
    task.setDescription(request.description());
    task.setStatus(request.status());
    task.setPriority(request.priority());
    task.setDueDate(request.dueDate());

    if (request.assignedUserId() != null) {
      var user = userRepository.findById(request.assignedUserId())
          .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.assignedUserId()));
      task.setUser(user);
    } else {
      task.setUser(null);
    }

    return TaskMapper.toResponse(task);
  }

  /**
   * Deletes a task by ID after validating its existence.
   */
  @Override
  @Transactional
  public void deleteTask(Long id) {
    if (!taskRepository.existsById(id)) {
      throw new TaskNotFoundException(id);
    }
    taskRepository.deleteById(id);
  }
}

