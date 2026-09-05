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

    // 1. Handle 404 Not Found (TaskNotFoundException, ResourceNotFoundException)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return createProblemDetail(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage(), "not-found", request);
    }

    // 2. Handle 409 Conflict using JDK 21+ Pattern Matching for switch
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

    // 3. Handle 400 Bad Request: DTO Field Validation Errors (@Valid) with unnamed lambda parameter (_)
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

    // 4. Handle 400 Bad Request: Malformed JSON Syntax
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

    // 5. Handle 401 Unauthorized: Invalid Credentials
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

    // 6. Handle 403 Forbidden: Insufficient Permissions / RBAC Denial
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

    // 7. Handle 500 Internal Server Error: Catch-all Fallback
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

    private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail, String typeSlug, HttpServletRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create(ERROR_BASE_URL + typeSlug));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
