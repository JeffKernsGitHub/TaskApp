package info.jeffkerns.taskmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ==============================================================================
 * JWT Configuration Properties
 * ==============================================================================
 * This class maps configuration values from {@code application.properties} (or
 * {@code application.yml} / environment variables) directly into strongly-typed
 * Java fields.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>{@code @ConfigurationProperties(prefix = "application.security.jwt")}</b>:
 *       Tells Spring Boot to look for configuration keys starting with the specified prefix
 *       and automatically bind their values to this record's components.
 *       For example:
 *       <pre>
 *         application.security.jwt.secret-key=mySuperSecretKey1234567890
 *         application.security.jwt.expiration-ms=86400000
 *       </pre>
 *       Spring automatically handles camelCase to kebab-case translation!</li>
 *
 *   <li><b>Java Records (introduced in Java 14/16)</b>:
 *       {@code record} is a special, concise form of class designed to act as an immutable data carrier.
 *       When you declare a record:
 *       <ol>
 *         <li>Fields ({@code secretKey}, {@code expirationMs}) are automatically {@code private final}.</li>
 *         <li>A canonical constructor matching all parameters is created automatically.</li>
 *         <li>Public accessor methods (e.g., {@code secretKey()} rather than {@code getSecretKey()})
 *             are generated automatically.</li>
 *         <li>{@code equals()}, {@code hashCode()}, and {@code toString()} are generated automatically.</li>
 *       </ol>
 *       This eliminates hundreds of lines of boilerplate code!</li>
 * </ul>
 *
 * @param secretKey     the cryptographic secret key used to sign and verify HMAC-SHA JWT tokens
 * @param expirationMs  the time-to-live (TTL) for generated JWT tokens in milliseconds (e.g., 86400000 ms = 24 hours)
 */
@ConfigurationProperties(prefix = "application.security.jwt")
public record JwtProperties(
    String secretKey,
    long expirationMs
) {}

