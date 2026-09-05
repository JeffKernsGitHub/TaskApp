/**
 * ==============================================================================
 * User Controller (REST API Presentation Layer)
 * ==============================================================================
 * Exposes administrative and profile management endpoints for user accounts.
 *
 * <h3>Key Concepts for Beginners:</h3>
 * <ul>
 *   <li><b>Role-Based Access Control (RBAC)</b>:
 *       Endpoints like listing all users ({@code GET /api/v1/users}) or deleting a user
 *       ({@code DELETE /api/v1/users/{id}}) are restricted to administrators only using
 *       {@code @PreAuthorize("hasRole('ADMIN')")}.</li>
 *
 *   <li><b>Ownership-Based Access Control</b>:
 *       Profile viewing and updates allow <i>either</i> an administrator OR the account owner:
 *       <pre>{@code @PreAuthorize("hasRole('ADMIN') or @userSecurity.isUserOwner(#id, authentication.name)")}</pre>
 *   </li>
 * </ul>
 * ==============================================================================
 */
package info.jeffkerns.taskmanager.controller;

import info.jeffkerns.taskmanager.dto.request.CreateUserRequest;
import info.jeffkerns.taskmanager.dto.request.UpdateUserRequest;
import info.jeffkerns.taskmanager.dto.response.UserResponse;
import info.jeffkerns.taskmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    /**
     * Constructor injection of {@link UserService}.
     *
     * @param userService user business service
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Lists all registered users in paginated format.
     * Only administrators may enumerate users.
     *
     * @param pageable pagination parameters (defaults to 10 users per page, newest first)
     * @return 200 OK with page of user responses
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> listUsers(
        @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(userService.getUsers(pageable));
    }

    /**
     * Retrieves account details for a specific user ID.
     * Accessible by system administrators or the account owner.
     *
     * @param id database ID from URL path
     * @return 200 OK with user profile details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isUserOwner(#id, authentication.name)")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Administratively provisions a new user account with role assignment.
     * (Regular public registration is handled via {@code /api/v1/auth/register}).
     *
     * @param request validated user creation payload
     * @return 201 Created with Location header and user body
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse created = userService.createUser(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Updates an existing user's profile details.
     * Accessible by system administrators or the account owner.
     *
     * @param id      database ID of the user to modify
     * @param request validated update payload
     * @return 200 OK with the updated user profile
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isUserOwner(#id, authentication.name)")
    public ResponseEntity<UserResponse> updateUser(
        @PathVariable Long id,
        @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    /**
     * Permanently removes a user account from the database.
     * Only system administrators may perform this action.
     *
     * @param id database ID of the user to delete
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

