package info.jeffkerns.taskmanager.service;

import info.jeffkerns.taskmanager.dto.request.CreateTaskRequest;
import info.jeffkerns.taskmanager.dto.request.UpdateTaskRequest;
import info.jeffkerns.taskmanager.dto.response.TaskResponse;
import info.jeffkerns.taskmanager.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {

    Page<TaskResponse> getTasks(TaskStatus status, String search, Pageable pageable);

    TaskResponse getTaskById(Long id);

    TaskResponse createTask(CreateTaskRequest request);

    TaskResponse updateTask(Long id, UpdateTaskRequest request);

    void deleteTask(Long id);
}
