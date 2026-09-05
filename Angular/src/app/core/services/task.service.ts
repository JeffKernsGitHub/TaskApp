import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import {
  CreateTaskRequest,
  Page,
  TaskFilterParams,
  TaskPriority,
  TaskResponse,
  TaskStatus,
  UpdateTaskRequest
} from '../models/task.models';
import { AuthService } from './auth.service';
import { NotificationService } from './notification.service';

/**
 * ============================================================================
 * Task Management Service (`task.service.ts`)
 * ============================================================================
 * Handles all task-related communication with Spring Boot REST API:
 *   - `GET /api/v1/tasks` (with dynamic search, pagination, status & priority filtering)
 *   - `GET /api/v1/tasks/{id}`
 *   - `POST /api/v1/tasks`
 *   - `PUT /api/v1/tasks/{id}`
 *   - `DELETE /api/v1/tasks/{id}`
 *
 * KEY CONCEPTS DEMONSTRATED:
 * 1. Dual Execution Engine (Spring HTTP & In-Memory Mock Reactive Store):
 *    - In live mode: Executes typed `HttpClient` calls to the Spring backend.
 *    - In mock mode: Manipulates an in-memory `signal<TaskResponse[]>` simulating
 *      Spring Data JPA pagination and filtering logic.
 *
 * 2. Immutable Signal Updates with `.update()`:
 *    - Uses `mockTasks.update(tasks => [...])` to emit new state immutably.
 *
 * 3. RxJS Operator Pipeline:
 *    - `pipe(catchError(...))` catches HTTP errors gracefully and enables seamless
 *      fallback to local mock data if the Spring backend is stopped.
 */
@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private notification = inject(NotificationService);

  private baseUrl = '/api/v1/tasks';

  /**
   * In-Memory Task Database (Initial Seed Data aligning with Spring Learning Roadmap)
   */
  private mockTasks = signal<TaskResponse[]>([
    {
      id: 1,
      title: 'Implement Spring Data JPA Repository & Custom Queries',
      description: 'Configure JpaRepository for TaskEntity with derived queries, pagination, and transactional boundaries.',
      status: 'DONE',
      priority: 'HIGH',
      dueDate: '2026-09-10',
      assignedUser: { id: 1, username: 'admin', email: 'admin@taskmanager.com' },
      createdAt: '2026-08-25T10:00:00Z',
      updatedAt: '2026-08-27T14:30:00Z'
    },
    {
      id: 2,
      title: 'Configure JWT Stateless Security Filter Chain',
      description: 'Implement JwtAuthenticationFilter, BCryptPasswordEncoder, and UserDetailsService with SpEL @PreAuthorize.',
      status: 'DONE',
      priority: 'HIGH',
      dueDate: '2026-09-12',
      assignedUser: { id: 1, username: 'admin', email: 'admin@taskmanager.com' },
      createdAt: '2026-08-28T09:15:00Z',
      updatedAt: '2026-08-30T11:45:00Z'
    },
    {
      id: 3,
      title: 'Design Angular Material 3 Kanban Board and Data Table',
      description: 'Build interactive Drag & Drop Kanban board with CDK, reactive forms, and RFC 9457 Problem Details handling.',
      status: 'IN_PROGRESS',
      priority: 'HIGH',
      dueDate: '2026-09-15',
      assignedUser: { id: 2, username: 'alice', email: 'alice@example.com' },
      createdAt: '2026-09-01T08:00:00Z',
      updatedAt: '2026-09-05T12:00:00Z'
    },
    {
      id: 4,
      title: 'Setup Spring Boot Actuator & Virtual Threads',
      description: 'Expose /actuator/health and /actuator/metrics. Enable virtual threads (Loom) for lightweight request concurrency.',
      status: 'IN_PROGRESS',
      priority: 'MEDIUM',
      dueDate: '2026-09-18',
      assignedUser: { id: 2, username: 'alice', email: 'alice@example.com' },
      createdAt: '2026-09-02T14:20:00Z',
      updatedAt: '2026-09-04T16:00:00Z'
    },
    {
      id: 5,
      title: 'Write Automated Integration Tests with MockMvc & Testcontainers',
      description: 'Implement @WebMvcTest for REST controllers and @DataJpaTest slices for database persistence layer.',
      status: 'TODO',
      priority: 'MEDIUM',
      dueDate: '2026-09-22',
      assignedUser: { id: 3, username: 'bob', email: 'bob@example.com' },
      createdAt: '2026-09-03T11:00:00Z',
      updatedAt: '2026-09-03T11:00:00Z'
    },
    {
      id: 6,
      title: 'Create Multi-Stage Dockerfile & Kubernetes Ingress Manifests',
      description: 'Build minimal JRE runtime using jdeps and jlink; prepare deployment YAMLs for production release.',
      status: 'TODO',
      priority: 'LOW',
      dueDate: '2026-09-30',
      assignedUser: { id: 1, username: 'admin', email: 'admin@taskmanager.com' },
      createdAt: '2026-09-04T15:30:00Z',
      updatedAt: '2026-09-04T15:30:00Z'
    }
  ]);

  /**
   * Retrieves a paginated list of tasks matching the specified criteria
   */
  getTasks(filter?: TaskFilterParams): Observable<Page<TaskResponse>> {
    if (this.authService.isMockMode()) {
      return of(this.filterMockTasks(filter));
    }

    let params = new HttpParams();
    if (filter?.status && filter.status !== 'ALL') {
      params = params.set('status', filter.status);
    }
    if (filter?.priority && filter.priority !== 'ALL') {
      params = params.set('priority', filter.priority);
    }
    if (filter?.search && filter.search.trim()) {
      params = params.set('search', filter.search.trim());
    }
    if (filter?.page !== undefined) {
      params = params.set('page', filter.page.toString());
    }
    if (filter?.size !== undefined) {
      params = params.set('size', filter.size.toString());
    }
    if (filter?.sort) {
      params = params.set('sort', filter.sort);
    }

    return this.http.get<Page<TaskResponse>>(this.baseUrl, { params }).pipe(
      catchError(err => {
        if (err.status === 0 || err.status === 404 || err.status === 504) {
          console.warn('Backend server unreachable, loading in-memory mock tasks.');
          this.authService.isMockMode.set(true);
          return of(this.filterMockTasks(filter));
        }
        return throwError(() => err);
      })
    );
  }

  /**
   * Fetches a single task by its unique identifier
   */
  getTaskById(id: number): Observable<TaskResponse> {
    if (this.authService.isMockMode()) {
      const task = this.mockTasks().find(t => t.id === id);
      if (!task) {
        return throwError(() => new Error(`Task with id ${id} not found`));
      }
      return of(task);
    }

    return this.http.get<TaskResponse>(`${this.baseUrl}/${id}`).pipe(
      catchError(err => {
        if (err.status === 0 || err.status === 404 || err.status === 504) {
          const task = this.mockTasks().find(t => t.id === id);
          if (task) return of(task);
        }
        return throwError(() => err);
      })
    );
  }

  /**
   * Creates a new task and broadcasts changes
   */
  createTask(request: CreateTaskRequest): Observable<TaskResponse> {
    if (this.authService.isMockMode()) {
      const currentAuthUser = this.authService.currentUser();
      const newTask: TaskResponse = {
        id: Math.max(...this.mockTasks().map(t => t.id), 0) + 1,
        title: request.title,
        description: request.description ?? '',
        status: request.status ?? 'TODO',
        priority: request.priority,
        dueDate: request.dueDate ?? null,
        assignedUser: request.assignedUserId ? {
          id: request.assignedUserId,
          username: request.assignedUserId === 1 ? 'admin' : request.assignedUserId === 2 ? 'alice' : 'bob',
          email: request.assignedUserId === 1 ? 'admin@taskmanager.com' : `${request.assignedUserId === 2 ? 'alice' : 'bob'}@example.com`
        } : (currentAuthUser ? {
          id: currentAuthUser.id ?? 1,
          username: currentAuthUser.username,
          email: currentAuthUser.email ?? `${currentAuthUser.username}@example.com`
        } : null),
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };

      // Immutable array update using signal
      this.mockTasks.update(tasks => [newTask, ...tasks]);
      return of(newTask);
    }

    return this.http.post<TaskResponse>(this.baseUrl, request).pipe(
      catchError(err => {
        if (err.status === 0 || err.status === 404 || err.status === 504) {
          this.authService.isMockMode.set(true);
          return this.createTask(request);
        }
        return throwError(() => err);
      })
    );
  }

  /**
   * Updates an existing task's title, description, status, priority, or assignee
   */
  updateTask(id: number, request: UpdateTaskRequest): Observable<TaskResponse> {
    if (this.authService.isMockMode()) {
      let foundTask: TaskResponse | null = null;
      this.mockTasks.update(tasks => {
        return tasks.map(t => {
          if (t.id === id) {
            const updated: TaskResponse = {
              ...t,
              title: request.title,
              description: request.description ?? '',
              status: request.status,
              priority: request.priority,
              dueDate: request.dueDate ?? null,
              assignedUser: request.assignedUserId ? {
                id: request.assignedUserId,
                username: request.assignedUserId === 1 ? 'admin' : request.assignedUserId === 2 ? 'alice' : 'bob',
                email: `${request.assignedUserId === 1 ? 'admin' : request.assignedUserId === 2 ? 'alice' : 'bob'}@example.com`
              } : t.assignedUser,
              updatedAt: new Date().toISOString()
            };
            foundTask = updated;
            return updated;
          }
          return t;
        });
      });

      if (!foundTask) {
        return throwError(() => new Error(`Task with id ${id} not found`));
      }
      return of(foundTask as TaskResponse);
    }

    return this.http.put<TaskResponse>(`${this.baseUrl}/${id}`, request).pipe(
      catchError(err => {
        if (err.status === 0 || err.status === 404 || err.status === 504) {
          this.authService.isMockMode.set(true);
          return this.updateTask(id, request);
        }
        return throwError(() => err);
      })
    );
  }

  /**
   * Specialized helper for Kanban drag-and-drop status column transitions
   */
  updateTaskStatus(id: number, newStatus: TaskStatus): Observable<TaskResponse> {
    const current = this.mockTasks().find(t => t.id === id);
    if (!current) {
      return this.getTaskById(id).pipe(
        map(task => {
          const req: UpdateTaskRequest = {
            title: task.title,
            description: task.description,
            status: newStatus,
            priority: task.priority,
            dueDate: task.dueDate,
            assignedUserId: task.assignedUser?.id
          };
          return req;
        }),
        tap(req => this.updateTask(id, req))
      ) as any;
    }

    const updateReq: UpdateTaskRequest = {
      title: current.title,
      description: current.description,
      status: newStatus,
      priority: current.priority,
      dueDate: current.dueDate,
      assignedUserId: current.assignedUser?.id
    };
    return this.updateTask(id, updateReq);
  }

  /**
   * Deletes a task by ID
   */
  deleteTask(id: number): Observable<void> {
    if (this.authService.isMockMode()) {
      this.mockTasks.update(tasks => tasks.filter(t => t.id !== id));
      return of(void 0);
    }

    return this.http.delete<void>(`${this.baseUrl}/${id}`).pipe(
      catchError(err => {
        if (err.status === 0 || err.status === 404 || err.status === 504) {
          this.mockTasks.update(tasks => tasks.filter(t => t.id !== id));
          return of(void 0);
        }
        return throwError(() => err);
      })
    );
  }

  /**
   * Client-side pagination and filtering for mock sandbox mode
   */
  private filterMockTasks(filter?: TaskFilterParams): Page<TaskResponse> {
    let items = [...this.mockTasks()];

    if (filter?.status && filter.status !== 'ALL') {
      items = items.filter(t => t.status === filter.status);
    }

    if (filter?.priority && filter.priority !== 'ALL') {
      items = items.filter(t => t.priority === filter.priority);
    }

    if (filter?.search && filter.search.trim()) {
      const q = filter.search.trim().toLowerCase();
      items = items.filter(t =>
        t.title.toLowerCase().includes(q) ||
        (t.description && t.description.toLowerCase().includes(q))
      );
    }

    const page = filter?.page ?? 0;
    const size = filter?.size ?? 10;
    const totalElements = items.length;
    const totalPages = Math.ceil(totalElements / size) || 1;
    const startIndex = page * size;
    const content = items.slice(startIndex, startIndex + size);

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
