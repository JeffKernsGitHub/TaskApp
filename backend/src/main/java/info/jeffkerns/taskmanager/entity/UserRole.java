package info.jeffkerns.taskmanager.entity;

/**
 * ==============================================================================
 * User Role Enum
 * ==============================================================================
 * Defines the security roles available to users in the system.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>Spring Security "ROLE_" Prefix Convention</b>:
 *       Spring Security methods like {@code hasRole('ADMIN')} automatically prepend
 *       {@code "ROLE_"} when checking granted authorities behind the scenes.
 *       Therefore, an authority string must be formatted as {@code "ROLE_ADMIN"} to match
 *       {@code hasRole('ADMIN')}.</li>
 *
 *   <li><b>Modern Java Switch Expressions (Java 14+)</b>:
 *       Notice the {@code return switch (this) { case ... -> ... };} syntax below.
 *       Unlike traditional {@code switch} statements, switch expressions:
 *       <ol>
 *         <li>Return a value directly without needing a separate temporary variable.</li>
 *         <li>Use {@code ->} (arrow syntax) which prevents accidental fall-through (no {@code break} needed!).</li>
 *         <li>Are exhaustively checked by the compiler: if you add a new enum value (e.g. {@code MODERATOR})
 *             and forget to handle it here, the compiler will produce an error.</li>
 *       </ol>
 *   </li>
 * </ul>
 * ==============================================================================
 */
public enum UserRole {

  /** Standard end user with access to their own tasks. */
  USER,

  /** Administrative user with full system privileges across all tasks and users. */
  ADMIN;

  /**
   * Converts the role enum constant into the Spring Security granted authority string format.
   *
   * @return the role prefixed with "ROLE_" (e.g., "ROLE_USER" or "ROLE_ADMIN")
   */
  public String getAuthority() {
    return switch (this) {
      case USER -> "ROLE_USER";
      case ADMIN -> "ROLE_ADMIN";
    };
  }
}

