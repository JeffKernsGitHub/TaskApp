package info.jeffkerns.taskmanager.service;


import info.jeffkerns.taskmanager.dto.request.CreateTaskRequest;
import info.jeffkerns.taskmanager.dto.request.UpdateTaskRequest;
import info.jeffkerns.taskmanager.dto.response.TaskResponse;
import info.jeffkerns.taskmanager.entity.TaskEntity;
import info.jeffkerns.taskmanager.entity.TaskStatus;
import info.jeffkerns.taskmanager.exception.TaskNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaskService {

    // Thread-safe in-memory store for Milestone 1
    private final Map<Long, TaskEntity> taskStore = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    public List<TaskResponse> findAll(TaskStatus statusFilter) {
        return taskStore.values().stream()
                .filter(task -> statusFilter == null || task.getStatus() == statusFilter)
                .map(TaskResponse::fromEntity)
                .toList();
    }

    public TaskResponse findById(Long id) {
        TaskEntity entity = taskStore.get(id);
        if (entity == null) {
            throw new TaskNotFoundException(id);
        }
        return TaskResponse.fromEntity(entity);
    }

    public TaskResponse create(CreateTaskRequest request) {
        Long id = idSequence.getAndIncrement();
        Instant now = Instant.now();

        TaskEntity entity = new TaskEntity(
                id,
                request.title().strip(),
                request.description() != null ? request.description().strip() : null,
                TaskStatus.TODO,
                request.priority(),
                request.dueDate(),
                now,
                now
        );

        taskStore.put(id, entity);
        return TaskResponse.fromEntity(entity);
    }

    public TaskResponse update(Long id, UpdateTaskRequest request) {
        TaskEntity entity = taskStore.get(id);
        if (entity == null) {
            throw new TaskNotFoundException(id);
        }

        entity.setTitle(request.title().strip());
        entity.setDescription(request.description() != null ? request.description().strip() : null);
        entity.setStatus(request.status());
        entity.setPriority(request.priority());
        entity.setDueDate(request.dueDate());
        entity.setUpdatedAt(Instant.now());

        return TaskResponse.fromEntity(entity);
    }

    public void delete(Long id) {
        if (!taskStore.containsKey(id)) {
            throw new TaskNotFoundException(id);
        }
        taskStore.remove(id);
    }
}