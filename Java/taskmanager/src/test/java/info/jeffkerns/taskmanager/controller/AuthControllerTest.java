/**
 * ==============================================================================
 * Auth Controller Web Slice Tests (@WebMvcTest + MockMvc)
 * ==============================================================================
 * Tests the public authentication endpoints (registration and login) in {@link AuthController}.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>Testing Public Endpoints</b>:
 *       Unlike {@link TaskController}, the routes under {@code /api/v1/auth/**} are
 *       whitelisted with {@code .permitAll()} in {@link SecurityConfig}. Therefore,
 *       tests here do NOT require {@code @WithMockUser}.</li>
 *
 *   <li><b>Testing Authentication Exceptions</b>:
 *       When invalid credentials are submitted, the service throws {@link BadCredentialsException}.
 *       We verify that {@link GlobalExceptionHandler} intercepts it and produces a
 *       standardized RFC 7807/9457 HTTP 401 Unauthorized ProblemDetail.</li>
 * </ul>
 * ==============================================================================
 */
package info.jeffkerns.taskmanager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import info.jeffkerns.taskmanager.config.JwtAuthenticationFilter;
import info.jeffkerns.taskmanager.config.SecurityConfig;
import info.jeffkerns.taskmanager.dto.request.LoginRequest;
import info.jeffkerns.taskmanager.dto.request.RegisterRequest;
import info.jeffkerns.taskmanager.dto.response.AuthResponse;
import info.jeffkerns.taskmanager.dto.response.UserSummaryResponse;
import info.jeffkerns.taskmanager.exception.GlobalExceptionHandler;
import info.jeffkerns.taskmanager.repository.UserRepository;
import info.jeffkerns.taskmanager.service.AuthService;
import info.jeffkerns.taskmanager.service.JwtService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
@DisplayName("AuthController Web Slice Tests (MockMvc)")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    // =========================================================================
    // 1. POST /api/v1/auth/register Tests
    // =========================================================================

    @Test
    @DisplayName("POST /api/v1/auth/register: Returns 201 Created on valid registration")
    void register_ValidPayload_Returns201Created() throws Exception {
        // Arrange: Java Text Block (""") for clean request body
        var payload = """
            {
              "username": "newbie",
              "email": "newbie@example.com",
              "password": "SecurePassword123!"
            }
            """;

        UserSummaryResponse summary = new UserSummaryResponse(12L, "newbie", "newbie@example.com");
        when(authService.register(any(RegisterRequest.class))).thenReturn(summary);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/v1/users/12"))
            .andExpect(jsonPath("$.id").value(12))
            .andExpect(jsonPath("$.username").value("newbie"))
            .andExpect(jsonPath("$.email").value("newbie@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register: Returns 400 Bad Request when password is too short")
    void register_ShortPassword_Returns400ProblemDetail() throws Exception {
        // Arrange: Password with fewer than 8 characters violates @Size(min = 8)
        var invalidPayload = """
            {
              "username": "newbie",
              "email": "newbie@example.com",
              "password": "short"
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    // =========================================================================
    // 2. POST /api/v1/auth/login Tests
    // =========================================================================

    @Test
    @DisplayName("POST /api/v1/auth/login: Returns 200 OK with AuthResponse token")
    void login_ValidCredentials_ReturnsToken() throws Exception {
        // Arrange
        var payload = """
            {
              "username": "newbie",
              "password": "SecurePassword123!"
            }
            """;

        AuthResponse authResponse = AuthResponse.of("mock.jwt.token", 900000L, "newbie", "ROLE_USER");
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("mock.jwt.token"))
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.username").value("newbie"))
            .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login: Returns 401 Unauthorized when credentials are bad")
    void login_BadCredentials_Returns401ProblemDetail() throws Exception {
        // Arrange
        var payload = """
            {
              "username": "newbie",
              "password": "WrongPassword!"
            }
            """;

        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new BadCredentialsException("Invalid username or password"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.title").value("Authentication Failed"))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.detail").value("Invalid username or password"));
    }
}
