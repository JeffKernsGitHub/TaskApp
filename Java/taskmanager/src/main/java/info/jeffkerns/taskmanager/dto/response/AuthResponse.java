package info.jeffkerns.taskmanager.dto.response;

/**
 * ==============================================================================
 * Authentication Response (Token DTO)
 * ==============================================================================
 * Returned to the client upon successful registration or login.
 * Contains the JSON Web Token (JWT) and metadata needed for subsequent API requests.
 *
 * <h3>Client Usage:</h3>
 * <p>The client must store {@code accessToken} and supply it in the HTTP header
 * of subsequent requests as:
 * <pre>Authorization: Bearer &lt;accessToken&gt;</pre>
 * </p>
 *
 * @param accessToken the signed cryptographic JWT string
 * @param tokenType   the token authorization scheme (always "Bearer" in OAuth2/JWT standards)
 * @param expiresIn   the token validity lifespan in milliseconds
 * @param username    the authenticated user's username
 * @param role        the authenticated user's assigned role
 * ==============================================================================
 */
public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    String username,
    String role
) {
    /**
     * Static factory helper method.
     * Provides a clean instantiation API with "Bearer" set automatically.
     *
     * @param token     the generated JWT
     * @param expiresIn token lifespan in milliseconds
     * @param username  the authenticated username
     * @param role      the user's role authority
     * @return a fully populated {@link AuthResponse}
     */
    public static AuthResponse of(String token, long expiresIn, String username, String role) {
        return new AuthResponse(token, "Bearer", expiresIn, username, role);
    }
}

