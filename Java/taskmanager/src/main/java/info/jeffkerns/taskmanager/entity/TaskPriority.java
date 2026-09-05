package info.jeffkerns.taskmanager.entity;

/**
 * ==============================================================================
 * Task Priority Enum
 * ==============================================================================
 * Represents the urgency or importance level of a task.
 *
 * <p>Persisted to the database as a string (e.g. {@code "LOW"}, {@code "MEDIUM"}, {@code "HIGH"})
 * via JPA's {@code @Enumerated(EnumType.STRING)} annotation on {@link TaskEntity}.</p>
 * ==============================================================================
 */
public enum TaskPriority {

    /** Low urgency task; can be addressed when time permits. */
    LOW,

    /** Normal urgency task; default priority level. */
    MEDIUM,

    /** High urgency task; requires immediate attention. */
    HIGH
}

