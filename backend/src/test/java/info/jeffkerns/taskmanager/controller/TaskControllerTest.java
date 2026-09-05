/**
 * ==============================================================================
 * Task Controller Web Slice Tests (@WebMvcTest + MockMvc)
 * ==============================================================================
 * Focuses exclusively on testing the presentation layer for {@link TaskController}.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>What is a Slice Test?</b>:
 *       Unlike {@code @SpringBootTest} which initializes your entire application,
 *       {@code @WebMvcTest} boots ONLY the web infrastructure: controllers, filters,
 *       and controller advice (error handlers). It skips repositories and database
 *       connections entirely!</li>
 *
 *   <li><b>{@link MockMvc}</b>:
 *       A high-performance simulated HTTP client that sends requests into the Spring
 *       DispatcherServlet in-memory without binding to a physical network socket.</li>
 *
 *   <li><b>{@link MockitoBean @MockitoBean}</b>:
 *       In Spring Boot 4 (Spring Framework 6.2+), {@code @MockitoBean} directly replaces
 *       the older {@code @MockBean}. It registers a Mockito mock inside the Spring
 *       ApplicationContext, satisfying controller constructor injection.</li>
 *
 *   <li><b>{@code @WithMockUser}</b>:
 *       Synthesizes an authenticated user in Spring Security's context for the duration
 *       of a test method without needing an actual login request or JWT token.</li>
 *
 *   <li><b>JSONPath Assertions</b>:
 *       {@code jsonPath("$.title")} uses standard JSONPath expressions to verify that
 *       JSON fields in the HTTP response match expected values.</li>
 * </ul>
 * ==============================================================================
 */
package info.jeffkerns.taskmanager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import info.jeffkerns.taskmanager.config.JwtAuthenticationFilter;
import info.jeffkerns.taskmanager.config.SecurityConfig;
import info.jeffkerns.taskmanager.dto.request.CreateTaskRequest;
import info.jeffkerns.taskmanager.dto.response.TaskResponse;
import info.jeffkerns.taskmanager.dto.response.UserSummaryResponse;
import info.jeffkerns.taskmanager.entity.TaskPriority;
import info.jeffkerns.taskmanager.entity.TaskStatus;
import info.jeffkerns.taskmanager.exception.GlobalExceptionHandler;
import info.jeffkerns.taskmanager.exception.TaskNotFoundException;
import info.jeffkerns.taskmanager.repository.UserRepository;
import info.jeffkerns.taskmanager.security.TaskSecurity;
import info.jeffkerns.taskmanager.service.JwtService;
import info.jeffkerns.taskmanager.service.TaskService;

import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
@DisplayName("TaskController Web Slice Tests (MockMvc)")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean(name = "taskSecurity")
    private TaskSecurity taskSecurity;

    // =========================================================================
    // 1. GET /api/v1/tasks/{id} Tests
    // =========================================================================

    @Test
    @WithMockUser(username = "jane_doe", roles = {"USER"})
    @DisplayName("GET /api/v1/tasks/{id}: Returns 200 OK with Task JSON when task exists")
    void getTaskById_ExistingId_ReturnsTaskResponse() throws Exception {
        // Arrange
        var taskId = 1L;
        var mockResponse = new TaskResponse(
            taskId,
            "Refactor Controller Tests",
            "Use MockMvc for web slices",
            TaskStatus.TODO,
            TaskPriority.HIGH,
            LocalDate.now().plusDays(2),
            new UserSummaryResponse(10L, "jane_doe", "jane@example.com"),
            Instant.now(),
            Instant.now()
        );

        when(taskService.getTaskById(taskId)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/tasks/{id}", taskId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Refactor Controller Tests"))
            .andExpect(jsonPath("$.status").value("TODO"))
            .andExpect(jsonPath("$.priority").value("HIGH"))
            .andExpect(jsonPath("$.assignedUser.username").value("jane_doe"));
    }

    @Test
    @WithMockUser(username = "jane_doe")
    @DisplayName("GET /api/v1/tasks/{id}: Returns 404 ProblemDetail when task is missing")
    void getTaskById_MissingId_Returns404NotFound() throws Exception {
        // Arrange
        var missingId = 404L;
        when(taskService.getTaskById(missingId)).thenThrow(new TaskNotFoundException(missingId));

        // Act & Assert (GlobalExceptionHandler handles TaskNotFoundException with RFC 7807/9457)
        mockMvc.perform(get("/api/v1/tasks/{id}", missingId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Resource Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("Task with ID '404' was not found"));
    }

    // =========================================================================
    // 2. POST /api/v1/tasks Tests
    // =========================================================================

    @Test
    @WithMockUser(username = "jane_doe")
    @DisplayName("POST /api/v1/tasks: Returns 201 Created with Location header on valid request")
    void createTask_ValidRequest_Returns201Created() throws Exception {
        // Arrange: Java Text Block (""") for clean, readable JSON payload
        var payload = """
            {
              "title": "Valid Task Headline",
              "description": "Detailed explanation of what to do",
              "status": "TODO",
              "priority": "MEDIUM",
              "dueDate": "%s"
            }
            """.formatted(LocalDate.now().plusDays(3));

        TaskResponse createdResponse = new TaskResponse(
            55L,
            "Valid Task Headline",
            "Detailed explanation of what to do",
            TaskStatus.TODO,
            TaskPriority.MEDIUM,
            LocalDate.now().plusDays(3),
            null,
            Instant.now(),
            Instant.now()
        );

        when(taskService.createTask(any(CreateTaskRequest.class))).thenReturn(createdResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").value(55))
            .andExpect(jsonPath("$.title").value("Valid Task Headline"));
    }

    @Test
    @WithMockUser(username = "jane_doe")
    @DisplayName("POST /api/v1/tasks: Returns 400 Bad Request with fieldErrors when title is blank")
    void createTask_BlankTitle_Returns400ProblemDetail() throws Exception {
        // Arrange: Invalid payload violating @NotBlank and @Size constraints on title
        var invalidPayload = """
            {
              "title": "",
              "description": "Valid Description",
              "status": "TODO",
              "priority": "MEDIUM",
              "dueDate": "%s"
            }
            """.formatted(LocalDate.now().plusDays(1));

        // Act & Assert
        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    // =========================================================================
    // 3. Security Tests
    // =========================================================================

    @Test
    @DisplayName("GET /api/v1/tasks: Returns 401/403 when unauthenticated")
    void listTasks_Unauthenticated_ReturnsForbidden() throws Exception {
        // Omitting @WithMockUser simulates an unauthenticated anonymous caller
        mockMvc.perform(get("/api/v1/tasks"))
            .andExpect(status().isForbidden());
    }
}
