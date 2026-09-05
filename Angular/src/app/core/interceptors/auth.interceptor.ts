import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * ============================================================================
 * JWT Authentication Interceptor (`auth.interceptor.ts`)
 * ============================================================================
 * In modern Angular (v17+ / v22+), HTTP interceptors are written as pure functions
 * (`HttpInterceptorFn`) rather than class-based services implementing `HttpInterceptor`.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * 1. Functional Interceptors:
 *    - Registered via `provideHttpClient(withInterceptors([authInterceptor]))` in `app.config.ts`.
 *    - Simplifies testing and tree-shaking.
 *
 * 2. Immutable Request Cloning:
 *    - Angular HTTP requests (`HttpRequest<T>`) are immutable.
 *    - To add headers, we clone the request using `req.clone({ setHeaders: { ... } })`.
 *
 * 3. Bearer Token Header:
 *    - Automatically reads the active JWT token from `AuthService.token()` signal.
 *    - If present, attaches the `Authorization: Bearer <token>` header to outbound requests.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.token();

  // If a JWT token is present and the request is targeting an API route, attach header
  if (token && req.url.startsWith('/api/')) {
    const clonedReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(clonedReq);
  }

  // Pass untouched request downstream
  return next(req);
};
