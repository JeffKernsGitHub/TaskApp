package info.jeffkerns.taskmanager.dto.response;

public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    String username,
    String role
) {
    public static AuthResponse of(String token, long expiresIn, String username, String role) {
        return new AuthResponse(token, "Bearer", expiresIn, username, role);
    }
}
