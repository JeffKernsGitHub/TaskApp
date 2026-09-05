import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { AuthResponse, CurrentUser, LoginRequest, RegisterRequest } from '../models/auth.models';
import { NotificationService } from './notification.service';

/**
 * ============================================================================
 * Authentication Service (`auth.service.ts`)
 * ============================================================================
 * Central state store managing user identity, JWT authentication tokens,
 * RBAC roles, and offline/mock operating mode.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * 1. Angular Signals (`signal<T>()` and `computed<T>()`):
 *    - `signal()` creates a writable reactive primitive holding state.
 *    - `computed()` creates a read-only derived value that automatically recalculates
 *      whenever any dependent signal changes.
 *    - Components can bind directly to `authService.currentUser()` in templates without
 *      needing manual RxJS subscriptions or `async` pipes.
 *
 * 2. Dependency Injection via `inject()`:
 *    - Modern Angular uses `inject(ServiceClass)` instead of constructor parameter injection.
 *    - Provides cleaner syntax and full type inference.
 *
 * 3. Token-Based JWT Flow & Dual Mode (Live Backend + Mock Sandbox):
 *    - Integrates with Spring Security `/api/v1/auth/login` and `/api/v1/auth/register`.
 *    - If the backend is unavailable or when the user toggles Mock Mode, this service
 *      simulates authentication seamlessly in-memory for testing and offline demos.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private notification = inject(NotificationService);

  private readonly TOKEN_KEY = 'taskapp_auth_token';
  private readonly USER_KEY = 'taskapp_auth_user';
  private readonly MOCK_KEY = 'taskapp_mock_mode';

  private baseUrl = '/api/v1/auth';

  // --- Writable Signals (Internal State) ---
  currentUser = signal<CurrentUser | null>(null);
  token = signal<string | null>(null);
  isMockMode = signal<boolean>(false);

  // --- Computed Signals (Derived State) ---
  /** True if a user is currently authenticated */
  isAuthenticated = computed(() => !!this.currentUser() && !!this.token());

  /** True if the authenticated user has ADMIN authority */
  isAdmin = computed(() => this.currentUser()?.role === 'ADMIN');

  /** Current user's username or 'Guest' */
  username = computed(() => this.currentUser()?.username ?? 'Guest');

  /** Current user's role: 'ADMIN' | 'USER' | null */
  role = computed(() => this.currentUser()?.role ?? null);

  constructor() {
    this.hydrateFromStorage();
  }

  /**
   * Restores user session and mock mode preferences from browser localStorage
   */
  private hydrateFromStorage(): void {
    const savedMock = localStorage.getItem(this.MOCK_KEY);
    if (savedMock === 'true') {
      this.isMockMode.set(true);
    }

    const savedToken = localStorage.getItem(this.TOKEN_KEY);
    const savedUser = localStorage.getItem(this.USER_KEY);

    if (savedToken && savedUser) {
      try {
        const userObj: CurrentUser = JSON.parse(savedUser);
        this.token.set(savedToken);
        this.currentUser.set(userObj);
      } catch (err) {
        console.error('Failed to parse cached user data from localStorage', err);
        this.logout();
      }
    }
  }

  /**
   * Toggles between Live Spring Boot Backend and In-Memory Mock Sandbox
   */
  setMockMode(enable: boolean): void {
    this.isMockMode.set(enable);
    localStorage.setItem(this.MOCK_KEY, String(enable));
  }

  /**
   * Authenticates user via credentials or mock fallback
   */
  login(request: LoginRequest): Observable<AuthResponse> {
    if (this.isMockMode()) {
      return this.handleMockLogin(request);
    }

    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, request).pipe(
      tap(res => this.handleAuthSuccess(res)),
      catchError(err => {
        // Automatically switch to mock mode if backend is not reachable
        if (err.status === 0 || err.status === 404 || err.status === 504) {
          console.warn('Backend unavailable; authenticating via mock sandbox mode.');
          this.setMockMode(true);
          return this.handleMockLogin(request);
        }
        return throwError(() => err);
      })
    );
  }

  /**
   * Registers a new user account with Spring REST backend
   */
  register(request: RegisterRequest): Observable<AuthResponse> {
    if (this.isMockMode()) {
      return this.handleMockRegister(request);
    }

    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, request).pipe(
      tap(res => this.handleAuthSuccess(res)),
      catchError(err => {
        if (err.status === 0 || err.status === 404 || err.status === 504) {
          this.setMockMode(true);
          return this.handleMockRegister(request);
        }
        return throwError(() => err);
      })
    );
  }

  /**
   * Fast Demo Login: Immediately authenticates as Admin user
   */
  quickLoginAsAdmin(): void {
    this.handleAuthSuccess({
      accessToken: 'mock-jwt-admin-token-xyz',
      tokenType: 'Bearer',
      expiresIn: 3600,
      username: 'admin',
      role: 'ADMIN'
    });
    this.router.navigate(['/tasks/board']);
  }

  /**
   * Fast Demo Login: Immediately authenticates as Standard User
   */
  quickLoginAsUser(): void {
    this.handleAuthSuccess({
      accessToken: 'mock-jwt-user-token-abc',
      tokenType: 'Bearer',
      expiresIn: 3600,
      username: 'alice',
      role: 'USER'
    });
    this.router.navigate(['/tasks/board']);
  }

  /**
   * Terminates active session, clears signal stores and localStorage
   */
  logout(): void {
    this.token.set(null);
    this.currentUser.set(null);
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.router.navigate(['/login']);
  }

  /**
   * Internal helper to persist authenticated state into signals and localStorage
   */
  private handleAuthSuccess(res: AuthResponse): void {
    const user: CurrentUser = {
      id: res.username === 'admin' ? 1 : res.username === 'alice' ? 2 : 3,
      username: res.username,
      email: `${res.username}@example.com`,
      role: res.role,
      token: res.accessToken
    };

    this.token.set(res.accessToken);
    this.currentUser.set(user);
    localStorage.setItem(this.TOKEN_KEY, res.accessToken);
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }

  /**
   * Mock sandbox login generator
   */
  private handleMockLogin(request: LoginRequest): Observable<AuthResponse> {
    const isAdmin = request.username.toLowerCase().includes('admin');
    const mockRes: AuthResponse = {
      accessToken: `mock-jwt-${isAdmin ? 'admin' : 'user'}-${Date.now()}`,
      tokenType: 'Bearer',
      expiresIn: 3600,
      username: request.username,
      role: isAdmin ? 'ADMIN' : 'USER'
    };
    this.handleAuthSuccess(mockRes);
    return of(mockRes);
  }

  /**
   * Mock sandbox registration generator
   */
  private handleMockRegister(request: RegisterRequest): Observable<AuthResponse> {
    const mockRes: AuthResponse = {
      accessToken: `mock-jwt-reg-${Date.now()}`,
      tokenType: 'Bearer',
      expiresIn: 3600,
      username: request.username,
      role: 'USER'
    };
    this.handleAuthSuccess(mockRes);
    return of(mockRes);
  }
}
