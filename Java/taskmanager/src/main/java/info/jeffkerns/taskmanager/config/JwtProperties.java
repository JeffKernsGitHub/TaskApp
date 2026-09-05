package info.jeffkerns.taskmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.security.jwt")
public record JwtProperties(
    String secretKey,
    long expirationMs
) {}
