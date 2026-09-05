package info.jeffkerns.taskmanager.service;

import info.jeffkerns.taskmanager.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * ==============================================================================
 * JWT Service (Cryptographic Token Generator and Parser)
 * ==============================================================================
 * Manages the creation, digital signing, and validation of JSON Web Tokens (JWT)
 * using the JJWT (Java JWT) library.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>What is a JWT (JSON Web Token)?</b>:
 *       A compact, URL-safe string divided into three Base64-encoded sections separated by dots:
 *       <pre>Header.Payload.Signature</pre>
 *       <ul>
 *         <li><b>Header</b>: Identifies the signing algorithm (e.g. HS256).</li>
 *         <li><b>Payload</b>: Contains "claims" (statements about an entity, like username, roles, expiration).</li>
 *         <li><b>Signature</b>: A cryptographic HMAC-SHA hash computed using the secret key,
 *             ensuring that the token cannot be tampered with by clients.</li>
 *       </ul>
 *   </li>
 *
 *   <li><b>Stateless Nature</b>:
 *       Because the signature guarantees integrity, the server does not need to store the token
 *       in a database. It simply re-calculates the signature to verify the token is legitimate!</li>
 * </ul>
 * ==============================================================================
 */
@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey secretKey;

    /**
     * Initializes the JWT Service by converting the configured string secret
     * into an HMAC-SHA cryptographic {@link SecretKey}.
     *
     * @param properties configuration properties containing secretKey and expirationMs
     */
    public JwtService(JwtProperties properties) {
        this.properties = properties;
        // HMAC-SHA requires a SecretKey object derived from raw secret bytes
        this.secretKey = Keys.hmacShaKeyFor(properties.secretKey().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates and digitally signs a new JWT token for an authenticated user.
     *
     * @param userDetails the authenticated user details
     * @return a compact, URL-safe signed JWT string
     */
    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.expirationMs());

        return Jwts.builder()
            // The "sub" (subject) standard claim: holds the unique username
            .subject(userDetails.getUsername())
            // Custom claim: store list of roles (e.g. ["ROLE_USER"]) in the payload
            .claim("roles", userDetails.getAuthorities().stream().map(a -> a.getAuthority()).toList())
            // The "iat" (issued at) claim: timestamp when token was minted
            .issuedAt(now)
            // The "exp" (expiration) claim: timestamp when token becomes invalid
            .expiration(expiry)
            // Digitally sign the token using our cryptographic secret key
            .signWith(secretKey)
            // Serialize the entire JWT into the compact string: "header.payload.signature"
            .compact();
    }

    /**
     * Extracts the subject (username) from the token payload.
     *
     * @param token the signed JWT string
     * @return the username stored in the token's subject claim
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Validates whether a token is valid for a given user.
     *
     * @param token       the signed JWT string
     * @param userDetails user details loaded from the database
     * @return {@code true} if username matches and the token is not expired
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Checks if the token's expiration date is before the current system timestamp.
     *
     * @param token the signed JWT string
     * @return {@code true} if expired, {@code false} if still valid
     */
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Generic helper method to parse a token, verify its signature, and extract a specific claim.
     *
     * <p><b>Beginner Tip - {@code Function<Claims, T>}:</b>
     * This uses Java's functional interface {@link java.util.function.Function}.
     * Callers can pass a method reference like {@code Claims::getSubject} or {@code Claims::getExpiration},
     * allowing this single method to extract any claim safely without duplicate parsing code.</p>
     *
     * @param <T>            the return type of the requested claim
     * @param token          the signed JWT string
     * @param claimsResolver a function mapping {@link Claims} to the desired field {@code T}
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return claimsResolver.apply(claims);
    }
}

