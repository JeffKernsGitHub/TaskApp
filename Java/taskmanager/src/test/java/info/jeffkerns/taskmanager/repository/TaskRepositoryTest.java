/**
 * ==============================================================================
 * Task Repository Data Slice Tests (@DataJpaTest)
 * ==============================================================================
 * Verifies custom JPA repository queries, entity mappings, and database constraints.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>What is a Data JPA Slice Test?</b>:
 *       Annotated with {@code @DataJpaTest}, Spring initializes ONLY Hibernate,
 *       Spring Data JPA repositories, and the database connection. Web controllers,
 *       security filters, and service classes are NOT loaded.</li>
 *
 *   <li><b>Automatic Transactional Rollback</b>:
 *       Every test method in a {@code @DataJpaTest} class is implicitly annotated with
 *       {@code @Transactional}. When the test method completes (whether passing or failing),
 *       Spring automatically rolls back all database modifications! The database remains
 *       completely clean for the next test.</li>
 *
 *   <li><b>{@link TestEntityManager}</b>:
 *       A test helper provided by Spring Boot to manipulate database state (like persisting
 *       fixtures) without relying on the very {@code JpaRepository} being tested.
 *       This guarantees that your test arrange phase is independent of repository code.</li>
 *
 *   <li><b>{@code @AutoConfigureTestDatabase(replace = Replace.NONE)}</b>:
 *       Because {@link TaskEntity} uses PostgreSQL schemas ({@code tasks}) and native PostgreSQL
 *       enum types, we configure Spring to run against our real PostgreSQL instance rather
 *       than trying to substitute an in-memory H2 database with incompatible DDL.</li>
 * </ul>
 * ==============================================================================
 */
package info.jeffkerns.taskmanager.repository;

import static org.assertj.core.api.Assertions.assertThat;

import info.jeffkerns.taskmanager.entity.TaskEntity;
import info.jeffkerns.taskmanager.entity.TaskPriority;
import info.jeffkerns.taskmanager.entity.TaskStatus;
import info.jeffkerns.taskmanager.entity.UserEntity;
import info.jeffkerns.taskmanager.entity.UserRole;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("TaskRepository Data Slice Tests (@DataJpaTest)")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findByStatus: Retrieves only tasks matching requested status with pagination")
    void findByStatus_MatchesStatus_ReturnsFilteredPage() {
        // ---------------------------------------------------------------------
        // Phase 1: Arrange
        // Create an owner user to satisfy the foreign key constraint
        // ---------------------------------------------------------------------
        var uniqueSuffix = "_" + System.currentTimeMillis();
        var owner = new UserEntity("repo_user" + uniqueSuffix, "repo" + uniqueSuffix + "@test.com", "hash", UserRole.USER);
        entityManager.persist(owner);

        // Task 1: Status TODO
        var task1 = new TaskEntity(
            "Task TODO" + uniqueSuffix,
            "Description 1",
            TaskStatus.TODO,
            TaskPriority.MEDIUM,
            LocalDate.now().plusDays(1),
            owner
        );
        entityManager.persist(task1);

        // Task 2: Status DONE
        var task2 = new TaskEntity(
            "Task DONE" + uniqueSuffix,
            "Description 2",
            TaskStatus.DONE,
            TaskPriority.HIGH,
            LocalDate.now().plusDays(2),
            owner
        );
        entityManager.persist(task2);

        // Flush pushes pending INSERT statements to PostgreSQL immediately
        entityManager.flush();

        // ---------------------------------------------------------------------
        // Phase 2: Act
        // ---------------------------------------------------------------------
        // Sort by ID descending so our newly persisted entity appears on the very first page
        var result = taskRepository.findByStatus(TaskStatus.TODO, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")));

        // ---------------------------------------------------------------------
        // Phase 3: Assert
        // ---------------------------------------------------------------------
        assertThat(result).isNotEmpty();
        // Verify all returned tasks on this slice have status == TODO
        assertThat(result.getContent()).allMatch(t -> t.getStatus() == TaskStatus.TODO);
        // Verify our specific task is present
        assertThat(result.getContent()).anyMatch(t -> t.getTitle().equals("Task TODO" + uniqueSuffix));
    }

    @Test
    @DisplayName("findAllWithUser: Eagerly fetches user association via @EntityGraph")
    void findAllWithUser_EagerlyLoadsUser() {
        // Arrange
        var uniqueSuffix = "_" + System.currentTimeMillis();
        var user = new UserEntity("graph_user" + uniqueSuffix, "graph" + uniqueSuffix + "@test.com", "hash", UserRole.USER);
        entityManager.persist(user);

        var task = new TaskEntity(
            "Graph Task" + uniqueSuffix,
            "Testing eager join",
            TaskStatus.IN_PROGRESS,
            TaskPriority.HIGH,
            LocalDate.now().plusDays(3),
            user
        );
        entityManager.persistAndFlush(task);

        // Clear the persistence context (first-level cache) so Hibernate is forced
        // to execute a fresh SELECT query rather than returning the in-memory cached instance
        entityManager.clear();

        // Act
        // Sort by ID descending so our newly persisted entity appears on the very first page
        var page = taskRepository.findAllWithUser(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")));

        // Assert
        assertThat(page).isNotEmpty();
        // Locate our persisted task
        var foundTask = page.getContent().stream()
            .filter(t -> t.getTitle().equals("Graph Task" + uniqueSuffix))
            .findFirst()
            .orElseThrow();

        // Verify the user relation was eagerly fetched and accessible
        assertThat(foundTask.getUser()).isNotNull();
        assertThat(foundTask.getUser().getUsername()).isEqualTo("graph_user" + uniqueSuffix);
    }
}
