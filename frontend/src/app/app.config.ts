import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideNativeDateAdapter } from '@angular/material/core';
import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';

/**
 * ============================================================================
 * Application Configuration (`app.config.ts`)
 * ============================================================================
 * In modern Angular (v17+ / v22+), standalone applications use an `ApplicationConfig`
 * object rather than `AppModule` to configure global providers and dependency injection.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * 1. `provideZonelessChangeDetection()`:
 *    - Enables Angular's high-performance Zoneless change detection mode.
 *    - Removes the legacy requirement for `zone.js`, eliminating monkey-patching of browser APIs.
 *    - Angular now relies on Signals, microtasks, and explicit event notifications for change detection.
 *
 * 2. `provideRouter()`:
 *    - Configures client-side routing.
 *    - `withComponentInputBinding()` automatically maps route parameters and query params
 *      directly to `@Input()` / `input()` properties in route components.
 *
 * 3. `provideHttpClient()` & `withInterceptors()`:
 *    - Configures Angular's HTTP client with functional interceptors.
 *    - `authInterceptor`: Automatically appends JWT Bearer tokens to outbound requests.
 *    - `errorInterceptor`: Catches RFC 9457 ProblemDetail errors and network failures.
 *
 * 4. `provideAnimationsAsync()`:
 *    - Dynamically loads Angular Material animations on-demand, reducing the initial JavaScript bundle size.
 *
 * 5. `provideNativeDateAdapter()`:
 *    - Enables JavaScript native `Date` object integration for Material Datepicker controls.
 */
export const appConfig: ApplicationConfig = {
  providers: [
    // 1. Enable modern zoneless reactivity (no zone.js required)
    provideZonelessChangeDetection(),

    // 2. Global uncaught browser error handling
    provideBrowserGlobalErrorListeners(),

    // 3. Application routing with route parameter input binding
    provideRouter(routes, withComponentInputBinding()),

    // 4. HTTP client configured with JWT Auth & RFC 9457 Error Interceptors
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),

    // 5. Asynchronous Material animations (optimizes initial load time)
    provideAnimationsAsync(),

    // 6. Date adapter for Angular Material Datepicker components
    provideNativeDateAdapter()
  ]
};
