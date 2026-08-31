package info.jeffkerns.taskmanager.repository;

import info.jeffkerns.taskmanager.entity.TaskEntity;
import info.jeffkerns.taskmanager.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long>, JpaSpecificationExecutor<TaskEntity> {

  @EntityGraph(attributePaths = {"user"})
  Page<TaskEntity> findByStatus(TaskStatus status, Pageable pageable);

  @EntityGraph(attributePaths = {"user"})
  @Query("SELECT t FROM TaskEntity t")
  Page<TaskEntity> findAllWithUser(Pageable pageable);

  @Override
  @EntityGraph(attributePaths = {"user"})
  Page<TaskEntity> findAll(@Nullable Specification<TaskEntity> spec, Pageable pageable);
}