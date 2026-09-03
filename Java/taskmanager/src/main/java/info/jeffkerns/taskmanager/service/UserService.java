package info.jeffkerns.taskmanager.service;

import info.jeffkerns.taskmanager.dto.request.CreateUserRequest;
import info.jeffkerns.taskmanager.dto.request.UpdateUserRequest;
import info.jeffkerns.taskmanager.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<UserResponse> getUsers(Pageable pageable);

    UserResponse getUserById(Long id);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);
}
