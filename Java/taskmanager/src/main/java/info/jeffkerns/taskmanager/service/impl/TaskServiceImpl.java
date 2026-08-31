package info.jeffkerns.taskmanager.service.impl;

import info.jeffkerns.taskmanager.dto.request.CreateTaskRequest;
import info.jeffkerns.taskmanager.dto.request.UpdateTaskRequest;
import info.jeffkerns.taskmanager.dto.response.TaskResponse;
import info.jeffkerns.taskmanager.entity.TaskEntity;
import info.jeffkerns.taskmanager.entity.TaskStatus;
import info.jeffkerns.taskmanager.entity.UserEntity;
import info.jeffkerns.taskmanager.exception.ResourceNotFoundException;
import info.jeffkerns.taskmanager.exception.TaskNotFoundException;
import info.jeffkerns.taskmanager.mapper.TaskMapper;
import info.jeffkerns.taskmanager.repository.TaskRepository;
import info.jeffkerns.taskmanager.repository.UserRepository;
import info.jeffkerns.taskmanager.service.TaskService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

  private final TaskRepository taskRepository;
  private final UserRepository userRepository;

  public TaskServiceImpl(TaskRepository taskRepository, UserRepository userRepository) {
    this.taskRepository = taskRepository;
    this.userRepository = userRepository;
  }

  @Override
  public Page<TaskResponse> getTasks(TaskStatus status, String search, Pageable pageable) {
    Specification<TaskEntity> spec = (root, _, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (status != null) {
        predicates.add(cb.equal(root.get("status"), status));
      }
      if (search != null && !search.isBlank()) {
        predicates.add(cb.like(cb.lower(root.get("title")), "%" + search.strip().toLowerCase() + "%"));
      }
      return predicates.isEmpty() ? null : cb.and(predicates.toArray(Predicate[]::new));
    };

    return taskRepository.findAll(spec, pageable)
        .map(TaskMapper::toResponse);
  }

  @Override
  public TaskResponse getTaskById(Long id) {
    var task = taskRepository.findById(id)
        .orElseThrow(() -> new TaskNotFoundException(id));
    return TaskMapper.toResponse(task);
  }

  @Override
  @Transactional
  public TaskResponse createTask(CreateTaskRequest request) {
    UserEntity assignedUser = null;
    if (request.assignedUserId() != null) {
      assignedUser = userRepository.findById(request.assignedUserId())
          .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.assignedUserId()));
    }

    var entity = new TaskEntity(
        request.title(),
        request.description(),
        request.status(),
        request.priority(),
        request.dueDate(),
        assignedUser
    );

    var saved = taskRepository.save(entity);
    return TaskMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public TaskResponse updateTask(Long id, UpdateTaskRequest request) {
    var task = taskRepository.findById(id)
        .orElseThrow(() -> new TaskNotFoundException(id));

    task.setTitle(request.title());
    task.setDescription(request.description());
    task.setStatus(request.status());
    task.setPriority(request.priority());
    task.setDueDate(request.dueDate());

    if (request.assignedUserId() != null) {
      var user = userRepository.findById(request.assignedUserId())
          .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.assignedUserId()));
      task.setUser(user);
    } else {
      task.setUser(null);
    }

    // Hibernate Dirty Checking automatically flushes updates on commit!
    return TaskMapper.toResponse(task);
  }

  @Override
  @Transactional
  public void deleteTask(Long id) {
    if (!taskRepository.existsById(id)) {
      throw new TaskNotFoundException(id);
    }
    taskRepository.deleteById(id);
  }
}
