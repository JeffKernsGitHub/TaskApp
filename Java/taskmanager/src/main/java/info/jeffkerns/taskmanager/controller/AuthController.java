package info.jeffkerns.taskmanager.controller;

import info.jeffkerns.taskmanager.dto.request.LoginRequest;
import info.jeffkerns.taskmanager.dto.request.RegisterRequest;
import info.jeffkerns.taskmanager.dto.response.AuthResponse;
import info.jeffkerns.taskmanager.dto.response.UserSummaryResponse;
import info.jeffkerns.taskmanager.service.AuthService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserSummaryResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserSummaryResponse response = authService.register(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + response.id())).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
