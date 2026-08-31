package info.jeffkerns.taskmanager.dto.response;

public record UserSummaryResponse(
    Long id,
    String username,
    String email
) {}