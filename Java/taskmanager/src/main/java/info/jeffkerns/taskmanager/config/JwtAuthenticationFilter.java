/**
 * ==============================================================================
 * JWT Authentication Filter (HTTP Request Interceptor)
 * ==============================================================================
 * In a Spring Boot web application, incoming HTTP requests pass through a
 * "Filter Chain" before they ever reach a {@code @RestController}.
 *
 * This filter intercepts every incoming HTTP request to check whether it carries
 * a valid JSON Web Token (JWT) in its {@code Authorization} header.
 * If valid, it authenticates the user within Spring's {@link SecurityContextHolder}.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>{@link OncePerRequestFilter}</b>:
 *       A Spring base class guaranteeing that this filter runs exactly once per
 *       incoming HTTP request dispatch, avoiding duplicate processing.</li>
 *
 *   <li><b>Stateless Authentication (JWT)</b>:
 *       Unlike traditional sessions where the server stores session IDs in memory/database
 *       and sets a cookie, JWTs are self-contained tokens stored by the client. The client
 *       sends the token with every request, and the server validates its cryptographic
 *       signature without querying a session store.</li>
 *
 *   <li><b>{@link SecurityContextHolder}</b>:
 *       Spring Security's thread-local storage holding information about the currently
 *       authenticated principal (user). When a request is authenticated, Spring Security
 *       stores an {@link org.springframework.security.core.Authentication} object here.</li>
 * </ul>
 * ==============================================================================
 */
package info.jeffkerns.taskmanager.config;

import info.jeffkerns.taskmanager.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * {@code @Component} tells Spring to automatically create and manage an instance of this class
 * as a Spring Bean so it can be injected into {@link SecurityConfig}.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Constructor-based dependency injection.
     *
     * <p><b>Beginner Tip - Why {@code @Lazy}?</b>
     * {@code SecurityConfig} depends on {@code JwtAuthenticationFilter}, while {@code JwtAuthenticationFilter}
     * depends on {@code UserDetailsService} (which is defined as a bean inside {@code SecurityConfig}).
     * Without {@code @Lazy}, Spring would encounter a "circular dependency" during startup.
     * {@code @Lazy} tells Spring: "Don't create the {@code UserDetailsService} right this second;
     * provide a proxy and initialize it when first accessed."</p>
     *
     * @param jwtService         helper service for decoding, validating, and reading claims from JWTs
     * @param userDetailsService Spring Security service used to load user account details from the database
     */
    public JwtAuthenticationFilter(JwtService jwtService, @Lazy UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * The core filter logic executed for every incoming HTTP request.
     *
     * @param request     the incoming HTTP request (headers, query params, body)
     * @param response    the outgoing HTTP response
     * @param filterChain the chain of remaining filters to invoke after this one
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Step 1: Look for the HTTP "Authorization" header (e.g. "Authorization: Bearer eyJhbGciOi...")
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Step 2: If header is missing or does not start with "Bearer ", this request has no token.
        // Pass control to the next filter in the chain and exit.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Step 3: Extract the raw JWT string by removing the 7-character prefix "Bearer "
            final String jwt = authHeader.substring(7);

            // Step 4: Extract the subject (username) from the token payload
            final String username = jwtService.extractUsername(jwt);

            // Step 5: Check if username exists and user is not ALREADY authenticated in the current security context
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Load user details from the database to check status (roles, enabled, non-locked, etc.)
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Step 6: Verify token signature, expiration, and username match
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Create an authenticated token containing the user's principal, credentials (null for security),
                    // and granted authorities (e.g., ROLE_USER, ROLE_ADMIN)
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );

                    // Attach request-specific details (such as client IP address and session ID)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Step 7: Store the authenticated token in the SecurityContext.
                    // Now Spring Security knows who is making this request for any @PreAuthorize or security checks!
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception _) {
            // JDK 22+ Unnamed variable (_):
            // When an exception occurs (e.g., token expired, malformed signature), we intentionally catch it.
            // We do not reject the request here; instead, we leave SecurityContextHolder unauthenticated.
            // Downstream security checks (e.g., anyRequest().authenticated()) will reject unauthorized requests
            // with proper 401/403 HTTP status codes.
        }

        // Step 8: Continue down the filter chain to the controller or next filter
        filterChain.doFilter(request, response);
    }
}
