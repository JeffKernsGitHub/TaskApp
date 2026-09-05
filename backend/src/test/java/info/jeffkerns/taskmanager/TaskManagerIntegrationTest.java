/**
 * ==============================================================================
 * End-to-End Integration Tests (@SpringBootTest + @AutoConfigureMockMvc)
 * ==============================================================================
 * Verifies the full application pipeline across all architectural tiers:
 * HTTP Request ➔ Security Filter Chain (JWT) ➔ REST Controller ➔
 * Service Business Logic ➔ JPA / Hibernate ➔ PostgreSQL Database.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>{@code @SpringBootTest}</b>:
 *       Initializes the full application context with all real components:
 *       repositories, services, controllers, and database configurations.</li>
 *
 *   <li><b>{@link AutoConfigureMockMvc @AutoConfigureMockMvc}</b>:
 *       Provides a configured {@link MockMvc} instance capable of executing
 *       requests through the real {@link org.springframework.security.web.SecurityFilterChain}
 *       and custom {@link info.jeffkerns.taskmanager.config.JwtAuthenticationFilter}.</li>
 *
 *   <li><b>{@code @Transactional} on Test Classes</b>:
 *       Guarantees that all database mutations (user insertions, task records) are
 *       automatically rolled back when the test method finishes. Your test database
 *       remains pristine!</li>
 *
 *   <li><b>The Complete User Journey</b>:
 *       <ol>
 *         <li>Register a new account via {@code POST /api/v1/auth/register}.</li>
 *         <li>Login via {@code POST /api/v1/auth/login} to obtain a signed cryptographic JWT.</li>
 *         <li>Supply {@code Authorization: Bearer <token>} to create a task via {@code POST /api/v1/tasks}.</li>
 *         <li>Fetch the created task via {@code GET /api/v1/tasks/{id}} and verify data integrity.</li>
 *       </ol>
 *   </li>
 * </ul>
 * ==============================================================================
 */
package info.jeffkerns.taskmanager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("TaskManager End-to-End Integration Tests (Full Stack)")
class TaskManagerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Complete User Journey: Register -> Login -> Create Task -> Fetch Task")
    void fullUserLifecycleWorkflow_Succeeds() throws Exception {
        var uniqueSuffix = "_" + System.currentTimeMillis();
        var testUsername = "e2e_user" + uniqueSuffix;
        var testEmail = "e2e" + uniqueSuffix + "@example.com";
        var testPassword = "SecurePassword123!";

        // =====================================================================
        // Step 1: Register a new account via public POST /api/v1/auth/register
        // =====================================================================
        var regPayload = """
            {
              "username": "%s",
              "email": "%s",
              "password": "%s"
            }
            """.formatted(testUsername, testEmail, testPassword);

        var registerResult = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(regPayload))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.username").value(testUsername))
            .andExpect(jsonPath("$.email").value(testEmail))
            .andReturn();

        Number registeredUserIdNum = JsonPath.read(registerResult.getResponse().getContentAsString(), "$.id");
        Long registeredUserId = registeredUserIdNum.longValue();
        assertThat(registeredUserId).isNotNull();

        // =====================================================================
        // Step 2: Login via POST /api/v1/auth/login to retrieve JWT access token
        // =====================================================================
        var loginPayload = """
            {
              "username": "%s",
              "password": "%s"
            }
            """.formatted(testUsername, testPassword);

        var loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isString())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.role").value("ROLE_USER"))
            .andExpect(jsonPath("$.username").value(testUsername))
            .andReturn();

        String jwtToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.accessToken");
        assertThat(jwtToken).isNotBlank();

        // =====================================================================
        // Step 3: Create a new task with "Authorization: Bearer <jwt>" header
        // =====================================================================
        var taskPayload = """
            {
              "title": "%s",
              "description": "Testing full stack pipeline with JWT authentication",
              "status": "TODO",
              "priority": "HIGH",
              "dueDate": "%s",
              "assignedUserId": %d
            }
            """.formatted("E2E Integration Task" + uniqueSuffix, LocalDate.now().plusDays(5), registeredUserId);

        MvcResult createResult = mockMvc.perform(post("/api/v1/tasks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskPayload))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.title").value("E2E Integration Task" + uniqueSuffix))
            .andExpect(jsonPath("$.status").value("TODO"))
            .andExpect(jsonPath("$.priority").value("HIGH"))
            .andExpect(jsonPath("$.assignedUser.username").value(testUsername))
            .andReturn();

        Number createdTaskIdNum = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");
        Long createdTaskId = createdTaskIdNum.longValue();

        // =====================================================================
        // Step 4: Query the persisted task via GET /api/v1/tasks/{id}
        // =====================================================================
        mockMvc.perform(get("/api/v1/tasks/{id}", createdTaskId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(createdTaskId))
            .andExpect(jsonPath("$.title").value("E2E Integration Task" + uniqueSuffix))
            .andExpect(jsonPath("$.assignedUser.id").value(registeredUserId));
    }
}
