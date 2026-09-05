package info.jeffkerns.taskmanager.entity;

/**
 * ==============================================================================
 * Task Status Enum
 * ==============================================================================
 * Represents the current lifecycle stage of a task.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>Java {@code enum} (Enumeration)</b>:
 *       An enum is a special Java data type that defines a fixed set of named constants.
 *       Using enums instead of raw strings (like {@code "TODO"}) or integer codes (like {@code 1}):
 *       <ol>
 *         <li>Prevents typos at compile time (the compiler rejects invalid states).</li>
 *         <li>Improves code readability and auto-completion in your IDE.</li>
 *         <li>Allows safe pattern matching and switch statements.</li>
 *       </ol>
 *   </li>
 * </ul>
 * ==============================================================================
 */
public enum TaskStatus {

    /** The task is created and waiting to be worked on. */
    TODO,

    /** Work is currently in progress on this task. */
    IN_PROGRESS,

    /** The task has been completed. */
    DONE
}

