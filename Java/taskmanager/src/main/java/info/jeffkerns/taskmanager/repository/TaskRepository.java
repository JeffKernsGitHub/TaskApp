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

/**
 * ==============================================================================
 * Task Repository (Data Access Layer)
 * ==============================================================================
 * Manages database persistence, dynamic filtering, and pagination for {@link TaskEntity}.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>{@link JpaSpecificationExecutor}&lt;TaskEntity&gt;</b>:
 *       Allows executing dynamic, multi-field filter queries built programmatically
 *       with the JPA Criteria API (see {@code TaskServiceImpl#getTasks}).
 *       For example, searching by status AND keyword search in the title simultaneously.</li>
 *
 *   <li><b>{@link EntityGraph} and the N+1 Query Problem</b>:
 *       In {@link TaskEntity}, the {@code user} relationship is marked {@code FetchType.LAZY}.
 *       If you load 20 tasks and then loop through them calling {@code task.getUser().getUsername()},
 *       Hibernate would execute 1 initial query for tasks + 20 separate queries for each user (21 queries in total!).
 *       Annotating a query with {@code @EntityGraph(attributePaths = {"user"})} tells Hibernate:
 *       <i>"Perform a SQL LEFT JOIN to fetch the tasks and their associated users in ONE single query."</i></li>
 *
 *   <li><b>Pagination ({@link Pageable} and {@link Page}&lt;T&gt;)</b>:
 *       Databases can hold millions of rows. Never load all rows at once into memory.
 *       Passing {@code Pageable} limits query results via SQL {@code LIMIT} and {@code OFFSET},
 *       returning a {@code Page} that includes the requested slice of data and total count metadata.</li>
 * </ul>
 * ==============================================================================
 */
@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long>, JpaSpecificationExecutor<TaskEntity> {

  /**
   * Retrieves a paginated slice of tasks filtered by a specific workflow status.
   *
   * @param status   the status to filter by (e.g. TODO, IN_PROGRESS, DONE)
   * @param pageable pagination parameters (page index, page size, sort order)
   * @return a {@link Page} of task entities eagerly joined with their owner
   */
  @EntityGraph(attributePaths = {"user"})
  Page<TaskEntity> findByStatus(TaskStatus status, Pageable pageable);

  /**
   * Retrieves all tasks with their owner user eagerly fetched via an explicit JPQL query.
   *
   * @param pageable pagination parameters
   * @return a {@link Page} of task entities with users loaded
   */
  @EntityGraph(attributePaths = {"user"})
  @Query("SELECT t FROM TaskEntity t")
  Page<TaskEntity> findAllWithUser(Pageable pageable);

  /**
   * Overrides the specification-based search method from {@link JpaSpecificationExecutor}
   * to apply {@code @EntityGraph}, preventing N+1 queries during filtered searches.
   *
   * @param spec     the dynamic filter specification (can be null for no filter)
   * @param pageable pagination parameters
   * @return a {@link Page} of matching task entities
   */
  @Override
  @EntityGraph(attributePaths = {"user"})
  Page<TaskEntity> findAll(@Nullable Specification<TaskEntity> spec, Pageable pageable);
}