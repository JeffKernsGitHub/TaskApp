import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { NotificationService } from '../services/notification.service';

/**
 * ============================================================================
 * Role-Based Admin Guard (`admin.guard.ts`)
 * ============================================================================
 * Functional Route Guard enforcing Role-Based Access Control (RBAC).
 *
 * KEY CONCEPTS DEMONSTRATED:
 * 1. RBAC Alignment with Spring Security:
 *    - Validates that the active user possesses the `ADMIN` role.
 *    - Mirrors Spring's `@PreAuthorize("hasRole('ADMIN')")` at the routing layer.
 *
 * 2. Graceful User Redirection:
 *    - Displays an error toast explaining the privilege requirement and redirects to `/tasks/board`.
 */
export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const notification = inject(NotificationService);
  const router = inject(Router);

  // Allow access if user has ADMIN authority or is running mock sandbox
  if (authService.isAdmin() || authService.isMockMode()) {
    return true;
  }

  // Reject unauthorized navigation
  notification.showError('Access Denied: Administrator privileges are required for this section.');
  router.navigate(['/tasks/board']);
  return false;
};
