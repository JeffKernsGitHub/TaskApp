package info.jeffkerns.taskmanager.service.impl;

import info.jeffkerns.taskmanager.dto.request.CreateUserRequest;
import info.jeffkerns.taskmanager.dto.request.UpdateUserRequest;
import info.jeffkerns.taskmanager.dto.response.UserResponse;
import info.jeffkerns.taskmanager.entity.UserEntity;
import info.jeffkerns.taskmanager.exception.DuplicateUserException;
import info.jeffkerns.taskmanager.exception.UserNotFoundException;
import info.jeffkerns.taskmanager.mapper.UserMapper;
import info.jeffkerns.taskmanager.repository.UserRepository;
import info.jeffkerns.taskmanager.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Page<UserResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserMapper::toResponse);
    }

    @Override
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
            .map(UserMapper::toResponse)
            .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUserException("A user with username '" + request.username() + "' already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException("A user with email '" + request.email() + "' already exists");
        }

        var entity = new UserEntity(
            request.username(),
            request.email(),
            request.password(),
            request.role()
        );

        var saved = userRepository.save(entity);
        return UserMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        var user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));

        if (!user.getUsername().equals(request.username()) && userRepository.existsByUsername(request.username())) {
            throw new DuplicateUserException("A user with username '" + request.username() + "' already exists");
        }

        if (!user.getEmail().equalsIgnoreCase(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException("A user with email '" + request.email() + "' already exists");
        }

        user.setUsername(request.username());
        user.setEmail(request.email());
        if (request.role() != null) {
            user.setRole(request.role());
        }

        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }
}
