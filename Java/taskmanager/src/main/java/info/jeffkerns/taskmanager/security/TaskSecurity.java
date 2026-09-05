package info.jeffkerns.taskmanager.security;

import info.jeffkerns.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("taskSecurity")
public class TaskSecurity {

    private final TaskRepository taskRepository;

    public TaskSecurity(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public boolean isTaskOwner(Long taskId, String username) {
        return taskRepository.findById(taskId)
            .map(task -> task.getUser() != null && username.equals(task.getUser().getUsername()))
            .orElse(false);
    }
}
