import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * ============================================================================
 * Authentication Guard (`auth.guard.ts`)
 * ============================================================================
 * Modern Functional Route Guard (`CanActivateFn`) that protects private routes
 * from unauthenticated access.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * 1. Functional Guards (`CanActivateFn`):
 *    - Replaces legacy class-based guards implementing `CanActivate`.
 *    - Injects dependencies directly inside the function scope with `inject()`.
 *
 * 2. Signal-Based Auth State Verification:
 *    - Reads `authService.isAuthenticated()` computed signal.
 *    - If unauthenticated, navigates to `/login` and blocks navigation (`return false`).
 */
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // If user is authenticated or working in mock mode, permit navigation
  if (authService.isAuthenticated() || authService.isMockMode()) {
    return true;
  }

  // Redirect to login page
  router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
  return false;
};
