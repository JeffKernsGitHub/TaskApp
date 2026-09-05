import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CreateUserRequest, UpdateUserRequest, UserResponse } from '../models/user.models';
import { Page } from '../models/task.models';
import { AuthService } from './auth.service';

/**
 * ============================================================================
 * User Management Service (`user.service.ts`)
 * ============================================================================
 * Handles administrative user provisioning, role assignments, and directory queries
 * matching Spring Boot `/api/v1/users` endpoints.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * 1. Admin RBAC Integration:
 *    - Calls protected endpoints requiring Spring Security `ROLE_ADMIN`.
 *
 * 2. In-Memory Mock Directory:
 *    - Mirrors pagination and CRUD actions when running offline or in sandbox mode.
 */
@Injectable({
  providedIn: 'root'
})
export class UserService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private baseUrl = '/api/v1/users';

  // Seeded mock users for offline demonstration
  private mockUsers = signal<UserResponse[]>([
    {
      id: 1,
      username: 'admin',
      email: 'admin@taskmanager.com',
      role: 'ADMIN',
      createdAt: '2026-08-20T08:00:00Z',
      updatedAt: '2026-08-20T08:00:00Z'
    },
    {
      id: 2,
      username: 'alice',
      email: 'alice@example.com',
      role: 'USER',
      createdAt: '2026-08-22T10:30:00Z',
      updatedAt: '2026-08-22T10:30:00Z'
    },
    {
      id: 3,
      username: 'bob',
      email: 'bob@example.com',
      role: 'USER',
      createdAt: '2026-08-25T14:15:00Z',
      updatedAt: '2026-08-25T14:15:00Z'
    }
  ]);

  /**
   * Retrieves paginated list of users
   */
  getUsers(page: number = 0, size: number = 10): Observable<Page<UserResponse>> {
    if (this.authService.isMockMode()) {
      return of(this.getMockUsersPage(page, size));
    }

    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<UserResponse>>(this.baseUrl, { params }).pipe(
      catchError(err => {
        if (err.status === 0 || err.status === 404 || err.status === 504) {
          return of(this.getMockUsersPage(page, size));
        }
        return throwError(() => err);
      })
    );
  }

  /**
   * Retrieves full list of users for dropdown assignee selectors
   */
  getAllUsersList(): Observable<UserResponse[]> {
    if (this.authService.isMockMode()) {
      return of(this.mockUsers());
    }

    return this.http.get<Page<UserResponse>>(this.baseUrl, { params: new HttpParams().set('size', '100') }).pipe(
      catchError(() => {
        return of({ content: this.mockUsers() } as any);
      })
    ) as any;
  }

  /**
   * Fetches user by ID
   */
  getUserById(id: number): Observable<UserResponse> {
    if (this.authService.isMockMode()) {
      const user = this.mockUsers().find(u => u.id === id);
      if (!user) {
        return throwError(() => new Error(`User with id ${id} not found`));
      }
      return of(user);
    }

    return this.http.get<UserResponse>(`${this.baseUrl}/${id}`);
  }

  /**
   * Provisions a new user account
   */
  createUser(request: CreateUserRequest): Observable<UserResponse> {
    if (this.authService.isMockMode()) {
      const newUser: UserResponse = {
        id: Math.max(...this.mockUsers().map(u => u.id), 0) + 1,
        username: request.username,
        email: request.email,
        role: request.role || 'USER',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      this.mockUsers.update(users => [...users, newUser]);
      return of(newUser);
    }

    return this.http.post<UserResponse>(this.baseUrl, request).pipe(
      catchError(err => {
        if (err.status === 0 || err.status === 404 || err.status === 504) {
          return this.createUser(request);
        }
        return throwError(() => err);
      })
    );
  }

  /**
   * Updates an existing user's role or email address
   */
  updateUser(id: number, request: UpdateUserRequest): Observable<UserResponse> {
    if (this.authService.isMockMode()) {
      let updated: UserResponse | null = null;
      this.mockUsers.update(users => {
        return users.map(u => {
          if (u.id === id) {
            updated = {
              ...u,
              username: request.username,
              email: request.email,
              role: request.role || u.role,
              updatedAt: new Date().toISOString()
            };
            return updated;
          }
          return u;
        });
      });

      if (!updated) {
        return throwError(() => new Error(`User with id ${id} not found`));
      }
      return of(updated as UserResponse);
    }

    return this.http.put<UserResponse>(`${this.baseUrl}/${id}`, request).pipe(
      catchError(err => {
        if (err.status === 0 || err.status === 404 || err.status === 504) {
          return this.updateUser(id, request);
        }
        return throwError(() => err);
      })
    );
  }

  /**
   * Deletes a user account
   */
  deleteUser(id: number): Observable<void> {
    if (this.authService.isMockMode()) {
      this.mockUsers.update(users => users.filter(u => u.id !== id));
      return of(void 0);
    }

    return this.http.delete<void>(`${this.baseUrl}/${id}`).pipe(
      catchError(err => {
        if (err.status === 0 || err.status === 404 || err.status === 504) {
          this.mockUsers.update(users => users.filter(u => u.id !== id));
          return of(void 0);
        }
        return throwError(() => err);
      })
    );
  }

  private getMockUsersPage(page: number, size: number): Page<UserResponse> {
    const all = this.mockUsers();
    const totalElements = all.length;
    const totalPages = Math.ceil(totalElements / size) || 1;
    const start = page * size;
    const content = all.slice(start, start + size);

    return {
      content,
      totalElements,
      totalPages,
      size,
      number: page,
      first: page === 0,
      last: page >= totalPages - 1,
      empty: content.length === 0
    };
  }
}
