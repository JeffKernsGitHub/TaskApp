/**
 * ==============================================================================
 * Task Entity (JPA Domain Model)
 * ==============================================================================
 * This class maps directly to the {@code tasks.tasks} relational table in the database.
 *
 * Each instance of this class corresponds to one task row in the database.
 * Hibernate (the default JPA implementation in Spring Boot) automatically handles
 * translating instances of this class into SQL tables, columns, constraints, and relationships.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>{@code @Entity}</b>:
 *       Tells JPA/Hibernate that this Java class should be tracked as a persistent entity.</li>
 *
 *   <li><b>{@code @Table(name = "tasks", schema = "tasks")}</b>:
 *       Specifies the exact table name and PostgreSQL schema where tasks are stored.</li>
 *
 *   <li><b>{@code @ManyToOne(fetch = FetchType.LAZY)}</b>:
 *       Represents a foreign key relationship: Many tasks belong to One user.
 *       Lazy fetching ensures the user record is not loaded from SQL until explicitly requested.</li>
 *
 *   <li><b>{@code @JoinColumn(name = "user_id")}</b>:
 *       Declares the foreign key column name in the {@code tasks} table pointing to {@code users.id}.</li>
 * </ul>
 * ==============================================================================
 */
package info.jeffkerns.taskmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "tasks", schema = "tasks")
public class TaskEntity {

    /**
     * Primary Key identifier.
     * Auto-incremented by the database using PostgreSQL's {@code IDENTITY} / serial generator.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique title/headline for the task.
     * Max length 120 characters; cannot be null.
     */
    @Column(name = "title", nullable = false, unique = true, length = 120)
    private String title;

    /**
     * Detailed description of what needs to be done.
     * Optional field; max length 256 characters.
     */
    @Column(name = "description", length = 256)
    private String description;

    /**
     * Current workflow state of the task (TODO, IN_PROGRESS, DONE).
     * {@code @JdbcTypeCode(SqlTypes.NAMED_ENUM)} maps this enum to PostgreSQL's native {@code tasks.task_status} type.
     */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "tasks.task_status")
    private TaskStatus status = TaskStatus.TODO;

    /**
     * Priority level of the task (LOW, MEDIUM, HIGH).
     * Maps to PostgreSQL's native {@code tasks.task_priority} type.
     */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "priority", nullable = false, columnDefinition = "tasks.task_priority")
    private TaskPriority priority = TaskPriority.MEDIUM;

    /**
     * Target completion date (e.g. 2026-12-31).
     * Mapped to SQL {@code DATE} without time-of-day information.
     */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /**
     * The user who owns and is assigned to this task.
     * This is the <b>owning side</b> of the relationship, storing the {@code user_id} foreign key.
     * {@code fetch = FetchType.LAZY} avoids unnecessary SQL joins when only task details are needed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    /**
     * Audit timestamp when this task record was created.
     * Never modified after the initial INSERT statement.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Audit timestamp when this task record was last modified.
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Mandatory no-argument constructor required by the JPA specification.
     * Hibernate uses reflection to instantiate entities when reading query results.
     */
    protected TaskEntity() {
    }

    /**
     * Full parameterized constructor for creating new task instances.
     *
     * @param title       unique task title
     * @param description optional task description
     * @param status      initial status (defaults to TODO if null)
     * @param priority    initial priority (defaults to MEDIUM if null)
     * @param dueDate     target completion date
     * @param user        the user who owns this task
     */
    public TaskEntity(String title, String description, TaskStatus status,
        TaskPriority priority, LocalDate dueDate, UserEntity user) {
        this.title = title;
        this.description = description;
        this.status = status != null ? status : TaskStatus.TODO;
        this.priority = priority != null ? priority : TaskPriority.MEDIUM;
        this.dueDate = dueDate;
        this.user = user;
    }

    /**
     * Lifecycle callback executed by JPA immediately before initial persistence (INSERT).
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Lifecycle callback executed by JPA immediately before an update (UPDATE).
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // =========================================================================
    // Standard Getters and Setters
    // =========================================================================

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    /**
     * Equality check based on database primary key identifier.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskEntity other)) return false;
        return id != null && id.equals(other.id);
    }

    /**
     * Constant hash code for Hibernate entity identity consistency.
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
