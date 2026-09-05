/**
 * ==============================================================================
 * Global Exception Handler (Centralized REST Error Interceptor)
 * ==============================================================================
 * Intercepts exceptions thrown by any controller or service across the application
 * and translates them into uniform, machine-readable HTTP error responses conforming
 * to the RFC 7807 (Problem Details for HTTP APIs) standard.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>{@code @RestControllerAdvice}</b>:
 *       An Aspect-Oriented Programming (AOP) annotation that intercepts exceptions
 *       thrown anywhere during HTTP request handling. Instead of writing messy
 *       {@code try-catch} blocks in every controller method, you let exceptions bubble up,
 *       and Spring routes them directly to the matching {@code @ExceptionHandler} method here!</li>
 *
 *   <li><b>RFC 7807 ({@link ProblemDetail})</b>:
 *       Introduced in Spring 6 / Spring Boot 3 as the official standard for REST API errors.
 *       It produces a clean, consistent JSON structure with fields:
 *       <ul>
 *         <li>{@code status}: HTTP status code (e.g., 404)</li>
 *         <li>{@code title}: Short human-readable summary</li>
 *         <li>{@code detail}: Specific error explanation</li>
 *         <li>{@code instance}: The URI path that caused the error</li>
 *         <li>{@code type}: Documentation link URI for the error type</li>
 *         <li>{@code properties}: Custom application metadata (e.g. timestamps, field errors)</li>
 *       </ul>
 *   </li>
 * </ul>
 * ==============================================================================
 */
package info.jeffkerns.taskmanager.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String ERROR_BASE_URL = "https://api.taskmanager.com/errors/";

    /**
     * 1. Handles 404 Not Found.
     * Triggered when a requested task or user does not exist in the database.
     *
     * @param ex      the thrown {@link ResourceNotFoundException}
     * @param request the incoming HTTP servlet request
     * @return standard RFC 7807 ProblemDetail with 404 status
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return createProblemDetail(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage(), "not-found", request);
    }

    /**
     * 2. Handles 409 Conflict.
     * Triggered when attempting to create or update a resource with a unique field
     * that is already taken (e.g., duplicate title, username, or email).
     *
     * <p><b>Beginner Tip - Pattern Matching for {@code switch} (Java 21+):</b>
     * Notice {@code case DuplicateTaskException dte -> ...}. This modern Java syntax
     * tests the runtime type of the exception and automatically casts it to {@code dte}
     * in a single readable step, eliminating traditional {@code instanceof} and manual casting!</p>
     *
     * @param ex      the conflict exception
     * @param request the servlet request
     * @return standard ProblemDetail with 409 status
     */
    @ExceptionHandler({DuplicateTaskException.class, DuplicateUserException.class})
    public ProblemDetail handleDuplicateResource(RuntimeException ex, HttpServletRequest request) {
        String detailMessage = switch (ex) {
            case DuplicateTaskException dte -> dte.getMessage();
            case DuplicateUserException due -> due.getMessage();
            default -> "A resource conflict occurred";
        };
        log.warn("Conflict detected: {}", detailMessage);
        return createProblemDetail(HttpStatus.CONFLICT, "Resource Conflict", detailMessage, "conflict", request);
    }

    /**
     * 3. Handles 400 Bad Request: DTO Field Validation Errors.
     * Triggered when incoming JSON violates Jakarta validation annotations
     * (e.g., {@code @NotBlank}, {@code @Size}, {@code @Email}) on a {@code @Valid @RequestBody}.
     *
     * <p><b>Beginner Tip:</b>
     * It iterates through all violated fields and maps them into a {@code "fieldErrors"}
     * dictionary (e.g., {@code {"title": "Task title is required"}}), which is added to the ProblemDetail.</p>
     *
     * @param ex      the validation exception containing binding errors
     * @param request the servlet request
     * @return ProblemDetail with 400 status and field error breakdown
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        var problem = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "One or more request parameters or payload fields failed validation",
                "validation-failed",
                request
        );

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> Objects.requireNonNullElse(error.getDefaultMessage(), "Invalid value"),
                        (existing, _) -> existing
                ));
        problem.setProperty("fieldErrors", fieldErrors);

        return problem;
    }

    /**
     * 4. Handles 400 Bad Request: Malformed JSON Syntax.
     * Triggered when a client sends invalid JSON syntax (e.g., missing closing brace or quotes).
     *
     * @param ex      unparseable HTTP message exception
     * @param request the servlet request
     * @return ProblemDetail with 400 status
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON payload: {}", ex.getMessage());
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON",
                "Malformed or unparseable JSON payload in request body",
                "malformed-json",
                request
        );
    }

    /**
     * 5. Handles 401 Unauthorized: Invalid Credentials.
     * Triggered during login when the username is not found or password does not match.
     *
     * @param ex      bad credentials exception
     * @param request the servlet request
     * @return ProblemDetail with 401 status
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return createProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "Authentication Failed",
                "Invalid username or password",
                "invalid-credentials",
                request
        );
    }

    /**
     * 6. Handles 403 Forbidden: Insufficient Permissions / RBAC Denial.
     * Triggered when an authenticated user attempts an action forbidden by {@code @PreAuthorize}.
     *
     * @param ex      access denied exception
     * @param request the servlet request
     * @return ProblemDetail with 403 status
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        return createProblemDetail(
                HttpStatus.FORBIDDEN,
                "Access Denied",
                "You do not have permission to perform this action",
                "forbidden",
                request
        );
    }

    /**
     * 7. Handles 500 Internal Server Error: Catch-all Fallback.
     * Triggered by unexpected runtime exceptions or programming bugs.
     * Prevents internal Java stack traces from leaking to public clients.
     *
     * @param ex      any uncaught exception
     * @param request the servlet request
     * @return ProblemDetail with 500 status
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUncaughtException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled server error processing request to {}", request.getRequestURI(), ex);
        return createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected internal error occurred. Please contact system support.",
                "internal-error",
                request
        );
    }

    /**
     * Helper factory method to construct a standard {@link ProblemDetail} instance.
     *
     * @param status   HTTP response status enum
     * @param title    short descriptive title
     * @param detail   detailed explanation
     * @param typeSlug URL slug appended to documentation base URI
     * @param request  the originating HTTP servlet request
     * @return a configured {@link ProblemDetail} instance
     */
    private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail, String typeSlug, HttpServletRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create(ERROR_BASE_URL + typeSlug));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}

