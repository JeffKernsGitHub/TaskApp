/**
 * ==============================================================================
 * TaskManager Application Integration Tests
 * ==============================================================================
 * Comprehensive integration tests verifying security rules, REST endpoints,
 * validation constraints, and service layer operations.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>{@code @SpringBootTest}</b>:
 *       Starts the full Spring ApplicationContext, configuring all beans, database connections,
 *       and security configurations just like in production.</li>
 *
 *   <li><b>{@code @AutoConfigureMockMvc}</b>:
 *       Provides a {@link MockMvc} instance to execute simulated HTTP requests against
 *       your {@code @RestController} endpoints in-memory without needing a real running web server.</li>
 *
 *   <li><b>{@code @Transactional} on Test Classes</b>:
 *       Any database modifications made during a test method are automatically rolled back
 *       when the test method completes. This guarantees that tests do not leave dirty data
 *       behind that would break other tests!</li>
 *
 *   <li><b>{@code @WithMockUser}</b>:
 *       Synthesizes an authenticated user in Spring Security's context for the duration
 *       of a test method without needing to perform an actual login request.</li>
 * </ul>
 * ==============================================================================
 */
package info.jeffkerns.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import info.jeffkerns.taskmanager.dto.request.CreateTaskRequest;
import info.jeffkerns.taskmanager.dto.request.CreateUserRequest;
import info.jeffkerns.taskmanager.dto.response.TaskResponse;
import info.jeffkerns.taskmanager.dto.response.UserResponse;
import info.jeffkerns.taskmanager.entity.TaskPriority;
import info.jeffkerns.taskmanager.entity.TaskStatus;
import info.jeffkerns.taskmanager.entity.UserRole;
import info.jeffkerns.taskmanager.service.TaskService;
import info.jeffkerns.taskmanager.service.UserService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskmanagerApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TaskService taskService;

	@Autowired
	private UserService userService;

	/**
	 * Smoke test verifying that the Spring ApplicationContext loads without errors.
	 */
	@Test
	void contextLoads() {
	}

	/**
	 * Tests the task service's pagination and sorting directly at the service layer.
	 */
	@Test
	void testGetTasksService() {
		var page = taskService.getTasks(null, null, PageRequest.of(0, 5, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "priority")));
		if (!page.isEmpty()) {
			// JDK 21+ Sequenced Collections: getFirst() retrieves the first element safely
			var first = page.getContent().getFirst();
			assertNotNull(first);
		}
	}

	/**
	 * Tests that unauthenticated requests to protected endpoints are rejected with 401/403.
	 */
	@Test
	void testUnauthenticatedAccessReturnsForbiddenOrUnauthorized() throws Exception {
		mockMvc.perform(get("/api/v1/tasks"))
				.andExpect(status().isForbidden());
	}

	/**
	 * Tests that a user with the USER role can successfully list tasks.
	 */
	@Test
	@WithMockUser(username = "regular_user", roles = {"USER"})
	void testAuthenticatedUserCanListTasks() throws Exception {
		mockMvc.perform(get("/api/v1/tasks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page").exists())
				.andExpect(jsonPath("$.content").isArray());
	}

	/**
	 * Verifies that regular users are forbidden from accessing admin endpoints (e.g. listing all users).
	 */
	@Test
	@WithMockUser(username = "regular_user", roles = {"USER"})
	void testRegularUserDeniedFromListingUsers() throws Exception {
		mockMvc.perform(get("/api/v1/users"))
				.andExpect(status().isForbidden());
	}

	/**
	 * Verifies that administrators with ADMIN role can list all users.
	 */
	@Test
	@WithMockUser(username = "admin_user", roles = {"ADMIN"})
	void testAdminCanListUsers() throws Exception {
		mockMvc.perform(get("/api/v1/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page").exists())
				.andExpect(jsonPath("$.content").isArray());
	}

	/**
	 * Tests administrative user creation via POST /api/v1/users.
	 */
	@Test
	@WithMockUser(username = "admin_user", roles = {"ADMIN"})
	void testAdminCreateUserEndpoint() throws Exception {
		var uniqueUsername = "testadminuser_" + System.currentTimeMillis();
		var uniqueEmail = uniqueUsername + "@example.com";
		// Java Text Block (""") for multi-line JSON payload
		var payload = """
				{
				  "username": "%s",
				  "email": "%s",
				  "password": "ValidPassword123!",
				  "role": "USER"
				}
				""".formatted(uniqueUsername, uniqueEmail);

		mockMvc.perform(post("/api/v1/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.username").value(uniqueUsername))
				.andExpect(jsonPath("$.email").value(uniqueEmail));
	}

	/**
	 * Tests the end-to-end public authentication flow: register a new account, then login to get a JWT.
	 */
	@Test
	void testPublicRegisterAndLoginFlow() throws Exception {
		var uniqueUsername = "reguser_" + System.currentTimeMillis();
		var uniqueEmail = uniqueUsername + "@example.com";
		var regPayload = """
				{
				  "username": "%s",
				  "email": "%s",
				  "password": "SecurePassword123!"
				}
				""".formatted(uniqueUsername, uniqueEmail);

		// Step 1: Register new account
		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(regPayload))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.username").value(uniqueUsername))
				.andExpect(jsonPath("$.email").value(uniqueEmail));

		// Step 2: Login with registered credentials
		var loginPayload = """
				{
				  "username": "%s",
				  "password": "SecurePassword123!"
				}
				""".formatted(uniqueUsername);

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginPayload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isString())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.role").value("ROLE_USER"))
				.andExpect(jsonPath("$.username").value(uniqueUsername));
	}

	/**
	 * Tests that an account owner can inspect their own user profile.
	 */
	@Test
	void testUserSecurityOwnerCanViewOwnProfile() throws Exception {
		var username = "owner_user_" + System.currentTimeMillis();
		var user = userService.createUser(new CreateUserRequest(
				username,
				username + "@example.com",
				"SecretPassword123!",
				UserRole.USER
		));

		mockMvc.perform(get("/api/v1/users/" + user.id())
				.with(user(username).roles("USER")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(user.id()))
				.andExpect(jsonPath("$.username").value(username));
	}

	/**
	 * Tests that a regular user is forbidden from viewing another user's profile.
	 */
	@Test
	@WithMockUser(username = "other_user", roles = {"USER"})
	void testUserSecurityNonOwnerForbiddenFromViewingProfile() throws Exception {
		var user = userService.createUser(new CreateUserRequest(
				"target_user_" + System.currentTimeMillis(),
				"target_user_" + System.currentTimeMillis() + "@example.com",
				"SecretPassword123!",
				UserRole.USER
		));

		mockMvc.perform(get("/api/v1/users/" + user.id()))
				.andExpect(status().isForbidden());
	}

	/**
	 * Tests that a task's owner can update and delete their own task.
	 */
	@Test
	void testTaskSecurityOwnerCanUpdateAndDeleteTask() throws Exception {
		var username = "task_owner_" + System.currentTimeMillis();
		var user = userService.createUser(new CreateUserRequest(
				username,
				username + "@example.com",
				"SecretPassword123!",
				UserRole.USER
		));

		var task = taskService.createTask(new CreateTaskRequest(
				"Owned Task " + System.currentTimeMillis(),
				"Description",
				TaskStatus.TODO,
				TaskPriority.MEDIUM,
				LocalDate.now().plusDays(5),
				user.id()
		));

		var updatePayload = """
				{
				  "title": "Updated Task Title",
				  "description": "Updated Description",
				  "status": "IN_PROGRESS",
				  "priority": "HIGH",
				  "dueDate": "%s",
				  "assignedUserId": %d
				}
				""".formatted(LocalDate.now().plusDays(5), user.id());

		// Update by owner
		mockMvc.perform(put("/api/v1/tasks/" + task.id())
				.with(user(username).roles("USER"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(updatePayload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Updated Task Title"))
				.andExpect(jsonPath("$.status").value("IN_PROGRESS"));

		// Delete by owner
		mockMvc.perform(delete("/api/v1/tasks/" + task.id())
				.with(user(username).roles("USER")))
				.andExpect(status().isNoContent());
	}

	/**
	 * Tests that a user who is NOT the task owner is forbidden from updating or deleting the task.
	 */
	@Test
	@WithMockUser(username = "other_user_stranger", roles = {"USER"})
	void testTaskSecurityNonOwnerForbiddenFromUpdatingOrDeletingTask() throws Exception {
		var user = userService.createUser(new CreateUserRequest(
				"task_creator_" + System.currentTimeMillis(),
				"task_creator_" + System.currentTimeMillis() + "@example.com",
				"SecretPassword123!",
				UserRole.USER
		));

		var task = taskService.createTask(new CreateTaskRequest(
				"Secret Task " + System.currentTimeMillis(),
				"Confidential",
				TaskStatus.TODO,
				TaskPriority.HIGH,
				LocalDate.now().plusDays(5),
				user.id()
		));

		var updatePayload = """
				{
				  "title": "Hacked Title",
				  "description": "Hacked",
				  "status": "DONE",
				  "priority": "LOW",
				  "dueDate": "%s",
				  "assignedUserId": %d
				}
				""".formatted(LocalDate.now().plusDays(5), user.id());

		// Non-owner update forbidden
		mockMvc.perform(put("/api/v1/tasks/" + task.id())
				.contentType(MediaType.APPLICATION_JSON)
				.content(updatePayload))
				.andExpect(status().isForbidden());

		// Non-owner delete forbidden
		mockMvc.perform(delete("/api/v1/tasks/" + task.id()))
				.andExpect(status().isForbidden());
	}
}

