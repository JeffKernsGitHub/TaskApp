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

	@Test
	void contextLoads() {
	}

	@Test
	void testGetTasksService() {
		var page = taskService.getTasks(null, null, PageRequest.of(0, 5, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "priority")));
		if (!page.isEmpty()) {
			// JDK 21+ Sequenced Collections: getFirst()
			var first = page.getContent().getFirst();
			assertNotNull(first);
		}
	}

	@Test
	void testUnauthenticatedAccessReturnsForbiddenOrUnauthorized() throws Exception {
		mockMvc.perform(get("/api/v1/tasks"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "regular_user", roles = {"USER"})
	void testAuthenticatedUserCanListTasks() throws Exception {
		mockMvc.perform(get("/api/v1/tasks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page").exists())
				.andExpect(jsonPath("$.content").isArray());
	}

	@Test
	@WithMockUser(username = "regular_user", roles = {"USER"})
	void testRegularUserDeniedFromListingUsers() throws Exception {
		mockMvc.perform(get("/api/v1/users"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "admin_user", roles = {"ADMIN"})
	void testAdminCanListUsers() throws Exception {
		mockMvc.perform(get("/api/v1/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page").exists())
				.andExpect(jsonPath("$.content").isArray());
	}

	@Test
	@WithMockUser(username = "admin_user", roles = {"ADMIN"})
	void testAdminCreateUserEndpoint() throws Exception {
		String uniqueUsername = "testadminuser_" + System.currentTimeMillis();
		String uniqueEmail = uniqueUsername + "@example.com";
		String payload = """
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

	@Test
	void testPublicRegisterAndLoginFlow() throws Exception {
		String uniqueUsername = "reguser_" + System.currentTimeMillis();
		String uniqueEmail = uniqueUsername + "@example.com";
		String regPayload = """
				{
				  "username": "%s",
				  "email": "%s",
				  "password": "SecurePassword123!"
				}
				""".formatted(uniqueUsername, uniqueEmail);

		// 1. Register
		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(regPayload))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.username").value(uniqueUsername))
				.andExpect(jsonPath("$.email").value(uniqueEmail));

		// 2. Login
		String loginPayload = """
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

	@Test
	void testUserSecurityOwnerCanViewOwnProfile() throws Exception {
		String username = "owner_user_" + System.currentTimeMillis();
		UserResponse user = userService.createUser(new CreateUserRequest(
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

	@Test
	@WithMockUser(username = "other_user", roles = {"USER"})
	void testUserSecurityNonOwnerForbiddenFromViewingProfile() throws Exception {
		UserResponse user = userService.createUser(new CreateUserRequest(
				"target_user_" + System.currentTimeMillis(),
				"target_user_" + System.currentTimeMillis() + "@example.com",
				"SecretPassword123!",
				UserRole.USER
		));

		mockMvc.perform(get("/api/v1/users/" + user.id()))
				.andExpect(status().isForbidden());
	}

	@Test
	void testTaskSecurityOwnerCanUpdateAndDeleteTask() throws Exception {
		String username = "task_owner_" + System.currentTimeMillis();
		UserResponse user = userService.createUser(new CreateUserRequest(
				username,
				username + "@example.com",
				"SecretPassword123!",
				UserRole.USER
		));

		TaskResponse task = taskService.createTask(new CreateTaskRequest(
				"Owned Task " + System.currentTimeMillis(),
				"Description",
				TaskStatus.TODO,
				TaskPriority.MEDIUM,
				LocalDate.now().plusDays(5),
				user.id()
		));

		String updatePayload = """
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

	@Test
	@WithMockUser(username = "other_user_stranger", roles = {"USER"})
	void testTaskSecurityNonOwnerForbiddenFromUpdatingOrDeletingTask() throws Exception {
		UserResponse user = userService.createUser(new CreateUserRequest(
				"task_creator_" + System.currentTimeMillis(),
				"task_creator_" + System.currentTimeMillis() + "@example.com",
				"SecretPassword123!",
				UserRole.USER
		));

		TaskResponse task = taskService.createTask(new CreateTaskRequest(
				"Secret Task " + System.currentTimeMillis(),
				"Confidential",
				TaskStatus.TODO,
				TaskPriority.HIGH,
				LocalDate.now().plusDays(5),
				user.id()
		));

		String updatePayload = """
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
