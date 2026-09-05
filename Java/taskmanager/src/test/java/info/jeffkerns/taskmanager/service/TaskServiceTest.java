/**
 * ==============================================================================
 * Task Service Unit Tests (JUnit 5 + Mockito)
 * ==============================================================================
 * This test class demonstrates pure, isolated unit testing of the business logic
 * in {@link TaskServiceImpl} without booting the Spring Framework container.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>Why Unit Tests?</b>:
 *       Unit tests do not start an embedded web server, Spring ApplicationContext,
 *       or relational database. Because there is zero startup overhead, hundreds of
 *       unit tests execute in milliseconds.</li>
 *
 *   <li><b>{@code @ExtendWith(MockitoExtension.class)}</b>:
 *       Tells JUnit 5 to enable Mockito. It automatically processes annotations like
 *       {@code @Mock} and {@code @InjectMocks} before running test methods.</li>
 *
 *   <li><b>{@code @Mock} vs. {@code @InjectMocks}</b>:
 *       <ul>
 *         <li><b>{@code @Mock}</b> creates a "dummy" or "simulated" version of a dependency.
 *             By default, mocked methods return {@code null}, {@code 0}, or {@code false}
 *             until you tell them what to return using {@code when(...).thenReturn(...)}.</li>
 *         <li><b>{@code @InjectMocks}</b> creates an instance of the <i>concrete implementation class</i>
 *             under test (here, {@code TaskServiceImpl}) and automatically injects all the
 *             {@code @Mock} fields into its constructor.</li>
 *       </ul>
 *   </li>
 *
 *   <li><b>The AAA Pattern (Arrange - Act - Assert)</b>:
 *       Every unit test is organized into three distinct, readable phases:
 *       <ol>
 *         <li><b>Arrange (Given)</b>: Set up test input data and stub mock behaviors.</li>
 *         <li><b>Act (When)</b>: Execute the single method under test.</li>
 *         <li><b>Assert (Then)</b>: Verify the returned values and interactions with mocks.</li>
 *       </ol>
 *   </li>
 * </ul>
 * ==============================================================================
 */
package info.jeffkerns.taskmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import info.jeffkerns.taskmanager.dto.request.CreateTaskRequest;
import info.jeffkerns.taskmanager.dto.request.UpdateTaskRequest;
import info.jeffkerns.taskmanager.dto.response.TaskResponse;
import info.jeffkerns.taskmanager.entity.TaskEntity;
import info.jeffkerns.taskmanager.entity.TaskPriority;
import info.jeffkerns.taskmanager.entity.TaskStatus;
import info.jeffkerns.taskmanager.entity.UserEntity;
import info.jeffkerns.taskmanager.entity.UserRole;
import info.jeffkerns.taskmanager.exception.ResourceNotFoundException;
import info.jeffkerns.taskmanager.exception.TaskNotFoundException;
import info.jeffkerns.taskmanager.repository.TaskRepository;
import info.jeffkerns.taskmanager.repository.UserRepository;
import info.jeffkerns.taskmanager.service.impl.TaskServiceImpl;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests (Mockito)")
class TaskServiceTest {

    /**
     * Simulated repository dependency.
     * Real database queries are replaced with controlled Mockito stubbing.
     */
    @Mock
    private TaskRepository taskRepository;

    /**
     * Simulated user repository dependency for validating task assignees.
     */
    @Mock
    private UserRepository userRepository;

    /**
     * The class under test.
     * Mockito instantiates {@code TaskServiceImpl} and passes {@code taskRepository}
     * and {@code userRepository} into its constructor.
     * Note: We specify {@code TaskServiceImpl} (the concrete class), NOT the interface!
     */
    @InjectMocks
    private TaskServiceImpl taskService;

    // =========================================================================
    // 1. createTask Tests
    // =========================================================================

    @Test
    @DisplayName("createTask: Successfully saves task and returns mapped response")
    void createTask_ValidRequestWithoutAssignee_ReturnsTaskResponse() {
        // ---------------------------------------------------------------------
        // Phase 1: Arrange (Given)
        // ---------------------------------------------------------------------
        var request = new CreateTaskRequest(
            "Write Unit Tests",
            "Cover service layer with Mockito",
            TaskStatus.TODO,
            TaskPriority.HIGH,
            LocalDate.now().plusDays(3),
            null // No assigned user
        );

        // Prepare the entity that taskRepository.save() will return
        var savedEntity = new TaskEntity(
            request.title(),
            request.description(),
            request.status(),
            request.priority(),
            request.dueDate(),
            null
        );
        // In JPA, the ID is generated upon saving; we simulate that via ReflectionTestUtils
        ReflectionTestUtils.setField(savedEntity, "id", 101L);

        // Stubbing: "When taskRepository.save is called with ANY TaskEntity, return savedEntity"
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(savedEntity);

        // ---------------------------------------------------------------------
        // Phase 2: Act (When)
        // ---------------------------------------------------------------------
        var response = taskService.createTask(request);

        // ---------------------------------------------------------------------
        // Phase 3: Assert (Then)
        // ---------------------------------------------------------------------
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(101L);
        assertThat(response.title()).isEqualTo("Write Unit Tests");
        assertThat(response.description()).isEqualTo("Cover service layer with Mockito");
        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
        assertThat(response.priority()).isEqualTo(TaskPriority.HIGH);
        assertThat(response.assignedUser()).isNull();

        // Verify that taskRepository.save() was called exactly once
        verify(taskRepository, times(1)).save(any(TaskEntity.class));
        // Verify that userRepository was never queried since assignedUserId was null
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("createTask: Successfully associates task with assigned user")
    void createTask_ValidRequestWithAssignee_AssociatesUser() {
        // Arrange
        var userId = 5L;
        var request = new CreateTaskRequest(
            "Assigned Task",
            "Has an owner",
            TaskStatus.TODO,
            TaskPriority.MEDIUM,
            LocalDate.now().plusDays(5),
            userId
        );

        var user = new UserEntity("alice", "alice@example.com", "hash", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var savedEntity = new TaskEntity(
            request.title(),
            request.description(),
            request.status(),
            request.priority(),
            request.dueDate(),
            user
        );
        ReflectionTestUtils.setField(savedEntity, "id", 202L);

        when(taskRepository.save(any(TaskEntity.class))).thenReturn(savedEntity);

        // Act
        var response = taskService.createTask(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(202L);
        assertThat(response.assignedUser()).isNotNull();
        assertThat(response.assignedUser().id()).isEqualTo(userId);
        assertThat(response.assignedUser().username()).isEqualTo("alice");

        verify(userRepository, times(1)).findById(userId);
        verify(taskRepository, times(1)).save(any(TaskEntity.class));
    }

    @Test
    @DisplayName("createTask: Throws ResourceNotFoundException when assigned user ID does not exist")
    void createTask_InvalidAssignee_ThrowsResourceNotFoundException() {
        // Arrange
        var missingUserId = 999L;
        var request = new CreateTaskRequest(
            "Orphan Task",
            "Assignee missing",
            TaskStatus.TODO,
            TaskPriority.LOW,
            LocalDate.now().plusDays(1),
            missingUserId
        );

        // Stub userRepository to return empty Optional
        when(userRepository.findById(missingUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.createTask(request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found: " + missingUserId);

        // Crucial verification: ensure repository.save() was NEVER called if user was missing!
        verify(taskRepository, never()).save(any(TaskEntity.class));
    }

    // =========================================================================
    // 2. getTaskById Tests
    // =========================================================================

    @Test
    @DisplayName("getTaskById: Returns TaskResponse when task exists")
    void getTaskById_ExistingId_ReturnsTaskResponse() {
        // Arrange
        var taskId = 10L;
        var existingTask = new TaskEntity(
            "Existing Task",
            "Found in DB",
            TaskStatus.IN_PROGRESS,
            TaskPriority.HIGH,
            LocalDate.now().plusDays(2),
            null
        );
        ReflectionTestUtils.setField(existingTask, "id", taskId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        // Act
        var response = taskService.getTaskById(taskId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(taskId);
        assertThat(response.title()).isEqualTo("Existing Task");
        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);

        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    @DisplayName("getTaskById: Throws TaskNotFoundException when task ID is missing")
    void getTaskById_MissingId_ThrowsTaskNotFoundException() {
        // Arrange
        var missingId = 404L;
        when(taskRepository.findById(missingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.getTaskById(missingId))
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessageContaining(String.valueOf(missingId));

        verify(taskRepository, times(1)).findById(missingId);
    }

    // =========================================================================
    // 3. updateTask Tests
    // =========================================================================

    @Test
    @DisplayName("updateTask: Modifies existing entity and returns updated DTO")
    void updateTask_ValidRequest_UpdatesFields() {
        // Arrange
        var taskId = 1L;
        var existingTask = new TaskEntity(
            "Original Title",
            "Original Description",
            TaskStatus.TODO,
            TaskPriority.LOW,
            LocalDate.now().plusDays(1),
            null
        );
        ReflectionTestUtils.setField(existingTask, "id", taskId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        var updateRequest = new UpdateTaskRequest(
            "Updated Title",
            "Updated Description",
            TaskStatus.DONE,
            TaskPriority.HIGH,
            LocalDate.now().plusDays(10),
            null
        );

        // Act
        var response = taskService.updateTask(taskId, updateRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Updated Title");
        assertThat(response.description()).isEqualTo("Updated Description");
        assertThat(response.status()).isEqualTo(TaskStatus.DONE);
        assertThat(response.priority()).isEqualTo(TaskPriority.HIGH);

        // Note for Beginners: In TaskServiceImpl, Hibernate Dirty Checking is used inside @Transactional.
        // Therefore, taskRepository.save() is NOT explicitly called!
        verify(taskRepository, times(1)).findById(taskId);
    }

    // =========================================================================
    // 4. deleteTask Tests
    // =========================================================================

    @Test
    @DisplayName("deleteTask: Deletes task when ID exists")
    void deleteTask_ExistingId_CallsDeleteById() {
        // Arrange
        var taskId = 50L;
        when(taskRepository.existsById(taskId)).thenReturn(true);

        // Act
        taskService.deleteTask(taskId);

        // Assert
        verify(taskRepository, times(1)).existsById(taskId);
        verify(taskRepository, times(1)).deleteById(taskId);
    }

    @Test
    @DisplayName("deleteTask: Throws TaskNotFoundException when task does not exist")
    void deleteTask_NonExistentId_ThrowsException() {
        // Arrange
        var missingId = 77L;
        when(taskRepository.existsById(missingId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> taskService.deleteTask(missingId))
            .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository, never()).deleteById(anyLong());
    }

    // =========================================================================
    // 5. ArgumentCaptor Demonstration
    // =========================================================================

    @Test
    @DisplayName("ArgumentCaptor: Inspects the exact TaskEntity instance passed to save()")
    void createTask_ValidRequest_CapturesPersistedEntity() {
        // Arrange
        var request = new CreateTaskRequest(
            "Captor Task",
            "Verify entity mapping",
            TaskStatus.IN_PROGRESS,
            TaskPriority.HIGH,
            LocalDate.now().plusDays(7),
            null
        );

        var dummySaved = new TaskEntity(
            request.title(),
            request.description(),
            request.status(),
            request.priority(),
            request.dueDate(),
            null
        );
        ReflectionTestUtils.setField(dummySaved, "id", 777L);
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(dummySaved);

        // Act
        taskService.createTask(request);

        // Assert with ArgumentCaptor
        // Captures the object passed as a parameter to taskRepository.save(...)
        var entityCaptor = ArgumentCaptor.forClass(TaskEntity.class);
        verify(taskRepository).save(entityCaptor.capture());

        var capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getTitle()).isEqualTo("Captor Task");
        assertThat(capturedEntity.getDescription()).isEqualTo("Verify entity mapping");
        assertThat(capturedEntity.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(capturedEntity.getPriority()).isEqualTo(TaskPriority.HIGH);
    }
}
