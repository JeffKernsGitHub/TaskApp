package info.jeffkerns.taskmanager.dto.response;

/**
 * ==============================================================================
 * User Summary Response (Nested Lightweight DTO)
 * ==============================================================================
 * A lightweight projection of user details embedded inside {@link TaskResponse}.
 *
 * <p>Contains only the minimal public identifiers needed to display assignee
 * information on task cards and lists.</p>
 *
 * @param id       user database primary key ID
 * @param username user display username
 * @param email    user contact email
 * ==============================================================================
 */
public record UserSummaryResponse(
    Long id,
    String username,
    String email
) {}