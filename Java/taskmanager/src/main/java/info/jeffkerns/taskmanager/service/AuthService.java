package info.jeffkerns.taskmanager.service;

import info.jeffkerns.taskmanager.dto.request.LoginRequest;
import info.jeffkerns.taskmanager.dto.request.RegisterRequest;
import info.jeffkerns.taskmanager.dto.response.AuthResponse;
import info.jeffkerns.taskmanager.dto.response.UserSummaryResponse;

public interface AuthService {
    UserSummaryResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
