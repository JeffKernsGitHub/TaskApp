import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { NotificationService } from '../services/notification.service';
import { AuthService } from '../services/auth.service';
import { ProblemDetail } from '../models/problem-detail.models';

/**
 * ============================================================================
 * Global HTTP Error Interceptor (`error.interceptor.ts`)
 * ============================================================================
 * Intercepts all outbound HTTP responses and parses error payloads according to the
 * RFC 9457 Problem Details for HTTP APIs standard implemented in Spring Boot 3.x / 4.x.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * 1. RFC 9457 ProblemDetail Handling:
 *    - Spring Boot returns error payloads containing `title`, `status`, `detail`,
 *      `instance`, and field-specific validation errors.
 *    - Extracts user-friendly messages for notification snackbars.
 *
 * 2. 401 Unauthorized / Token Expiry:
 *    - Automatically forces user logout and redirects to `/login` when token expires.
 *
 * 3. RxJS `catchError` Pipeline:
 *    - Re-throws errors down the chain using `throwError(() => error)` so callers can
 *      also react if needed.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const notification = inject(NotificationService);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An unexpected system error occurred';

      if (error.error instanceof ErrorEvent) {
        // Client-side or network error
        errorMessage = `Network error: ${error.error.message}`;
      } else {
        // Backend returned an unsuccessful response code
        if (error.status === 401) {
          errorMessage = 'Session expired or invalid credentials. Please log in again.';
          authService.logout();
        } else if (error.status === 403) {
          errorMessage = 'Access denied: You lack sufficient administrative privileges.';
        } else if (error.status === 404) {
          errorMessage = 'The requested resource was not found on the server.';
        } else if (error.status === 400 && error.error) {
          const problem: ProblemDetail = error.error;
          if (problem.detail) {
            errorMessage = problem.detail;
          } else if (problem.invalidParams && problem.invalidParams.length > 0) {
            const firstErr = problem.invalidParams[0];
            errorMessage = `Validation error on ${firstErr.name}: ${firstErr.reason}`;
          }
        } else if (error.status === 500) {
          errorMessage = 'Internal server error. Please try again later.';
        } else if (error.status === 0) {
          errorMessage = 'Unable to connect to Spring Boot backend. Running in Mock Sandbox.';
        }
      }

      // Display user-friendly notification toast
      notification.showError(errorMessage);
      return throwError(() => error);
    })
  );
};
