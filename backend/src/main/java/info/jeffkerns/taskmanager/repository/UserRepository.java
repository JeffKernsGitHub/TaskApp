package info.jeffkerns.taskmanager.repository;

import info.jeffkerns.taskmanager.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * ==============================================================================
 * User Repository (Data Access Layer)
 * ==============================================================================
 * Spring Data JPA repository for performing CRUD operations on {@link UserEntity}.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>Why is this just an interface with no class implementation?</b>:
 *       Spring Data JPA creates a dynamic runtime proxy implementing this interface.
 *       You never have to write SQL queries like {@code SELECT * FROM tasks.users WHERE username = ?}
 *       or boilerplate JDBC connection code.</li>
 *
 *   <li><b>{@link JpaRepository}&lt;UserEntity, Long&gt;</b>:
 *       By extending {@code JpaRepository}, you instantly inherit dozens of standard database methods:
 *       <ul>
 *         <li>{@code save(entity)}: Inserts or updates a user.</li>
 *         <li>{@code findById(id)}: Finds a user by primary key.</li>
 *         <li>{@code findAll()}: Returns all user records.</li>
 *         <li>{@code deleteById(id)}: Deletes a user by primary key.</li>
 *       </ul>
 *   </li>
 *
 *   <li><b>Query Derivation by Method Name</b>:
 *       Spring parses the method name itself to construct the query!
 *       For instance, {@code findByUsername(String username)} is automatically translated to:
 *       <pre>{@code SELECT * FROM tasks.users WHERE username = ?}</pre>
 *   </li>
 *
 *   <li><b>{@link Optional}&lt;T&gt;</b>:
 *       Instead of returning {@code null} when a user is not found, returning {@code Optional}
 *       forces the calling code to handle the missing case cleanly (e.g. using {@code .orElseThrow()}),
 *       eliminating nasty {@link NullPointerException} bugs.</li>
 * </ul>
 * ==============================================================================
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Finds a user by their unique username.
     *
     * @param username the username to look up
     * @return an {@link Optional} containing the user if found, or empty if no matching record exists
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * Finds a user by their unique email address.
     *
     * @param email the email address to look up
     * @return an {@link Optional} containing the user if found, or empty if not found
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Checks if a user already exists with the given username.
     * Generates a high-performance {@code SELECT COUNT(...) > 0} SQL query.
     *
     * @param username the username to check
     * @return {@code true} if a record exists, {@code false} otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Checks if a user already exists with the given email address.
     *
     * @param email the email address to check
     * @return {@code true} if a record exists, {@code false} otherwise
     */
    boolean existsByEmail(String email);
}

