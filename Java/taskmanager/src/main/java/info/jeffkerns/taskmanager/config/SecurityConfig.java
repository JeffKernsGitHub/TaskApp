/**
 * ==============================================================================
 * Spring Security Configuration
 * ==============================================================================
 * This class is the central security control center for the application.
 *
 * In Spring Security 6+ (Spring Boot 3+), security is configured using a
 * component-based approach with {@link SecurityFilterChain} beans rather than
 * extending deprecated classes like `WebSecurityConfigurerAdapter`.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>Authentication vs. Authorization</b>:
 *       <ul>
 *         <li><b>Authentication</b> answers: <i>"Who are you?"</i> (verifying username and password).</li>
 *         <li><b>Authorization</b> answers: <i>"What are you allowed to do?"</i> (checking roles/permissions).</li>
 *       </ul>
 *   </li>
 *   <li><b>{@code @Configuration}</b>:
 *       Marks this class as a source of Spring bean definitions. Methods annotated with
 *       {@code @Bean} return objects that Spring creates, manages, and injects elsewhere.</li>
 *   <li><b>{@code @EnableWebSecurity}</b>:
 *       Turns on Spring Security's web protection and filter chains.</li>
 *   <li><b>{@code @EnableMethodSecurity}</b>:
 *       Enables method-level authorization annotations like {@code @PreAuthorize} across
 *       service methods and controller endpoints.</li>
 * </ul>
 * ==============================================================================
 */
package info.jeffkerns.taskmanager.config;

import info.jeffkerns.taskmanager.repository.UserRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserRepository userRepository;

    /**
     * Spring automatically calls this constructor, passing the required dependencies.
     * This is known as <b>Constructor Injection</b>, the recommended pattern in modern Spring.
     *
     * @param jwtAuthFilter  custom filter checking JWT tokens on incoming HTTP requests
     * @param userRepository repository for querying user accounts from the database
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserRepository userRepository) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userRepository = userRepository;
    }

    /**
     * Defines the primary security filter chain that intercepts all HTTP requests.
     *
     * @param http the {@link HttpSecurity} builder used to configure web-based security rules
     * @return the assembled {@link SecurityFilterChain}
     * @throws Exception if an error occurs while assembling the security rules
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF (Cross-Site Request Forgery):
            // CSRF protection is vital for browser cookie-based sessions, but unnecessary
            // and burdensome for stateless REST APIs using JWT tokens in headers.
            .csrf(csrf -> csrf.disable())

            // 2. Stateless Session Management:
            // Instruct Spring Security to never create an HTTP session (HttpSession).
            // Every request must be independently authenticated via its JWT Bearer token.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 3. Define URL Authorization Rules:
            .authorizeHttpRequests(auth -> auth
                // Allow unauthenticated access to login, registration, and health check endpoints
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                // Require ADMIN role for endpoints starting with /api/v1/admin/
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                // All other API requests require valid authentication
                .anyRequest().authenticated()
            )

            // 4. Configure our database authentication provider
            .authenticationProvider(authenticationProvider())

            // 5. Add our custom JWT filter BEFORE Spring's standard UsernamePasswordAuthenticationFilter.
            // This ensures our filter intercepts the request first and sets up the security context.
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Defines how Spring Security retrieves user credentials and details from the database.
     *
     * <p>We use a Java lambda expression here: {@code username -> ...} because
     * {@link UserDetailsService} is a <i>Functional Interface</i> with a single abstract method:
     * {@code loadUserByUsername(String username)}.</p>
     *
     * @return a {@link UserDetailsService} querying {@link UserRepository}
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * The {@link AuthenticationProvider} is responsible for fetching user details
     * and verifying their password using our configured {@link PasswordEncoder}.
     *
     * @return a configured {@link DaoAuthenticationProvider}
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Exposes Spring's {@link AuthenticationManager} as a Spring Bean so our
     * {@code AuthServiceImpl} can inject it to authenticate login requests.
     *
     * @param config Spring's internal authentication configuration
     * @return the managed {@link AuthenticationManager}
     * @throws Exception if unable to obtain the authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Cryptographic password encoder bean.
     *
     * <p><b>Security Rule:</b> NEVER store passwords in plain text!
     * {@link BCryptPasswordEncoder} uses the BCrypt strong hashing function with an
     * automatically generated random salt and computational work factor to protect against
     * brute-force and rainbow table attacks.</p>
     *
     * @return the BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
