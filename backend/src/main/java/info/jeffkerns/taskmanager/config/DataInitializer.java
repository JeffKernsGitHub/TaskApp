package info.jeffkerns.taskmanager.config;

import info.jeffkerns.taskmanager.entity.UserEntity;
import info.jeffkerns.taskmanager.entity.UserRole;
import info.jeffkerns.taskmanager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Automatically seeds default demo users (admin and alice) on application startup
 * if they do not already exist in the database.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            log.info("Seeding demo admin account (username: admin)...");
            UserEntity admin = new UserEntity(
                "admin",
                "admin@taskmanager.com",
                passwordEncoder.encode("Password123!"),
                UserRole.ADMIN
            );
            userRepository.save(admin);
            log.info("Demo admin account seeded successfully.");
        }

        if (!userRepository.existsByUsername("alice")) {
            log.info("Seeding demo standard user account (username: alice)...");
            UserEntity alice = new UserEntity(
                "alice",
                "alice@example.com",
                passwordEncoder.encode("Password123!"),
                UserRole.USER
            );
            userRepository.save(alice);
            log.info("Demo alice account seeded successfully.");
        }
    }
}
