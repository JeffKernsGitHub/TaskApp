import { UserSummaryResponse } from './user.models';

/**
 * ============================================================================
 * Task Domain Models (`task.models.ts`)
 * ============================================================================
 * TypeScript data transfer objects (DTOs) and types matching the Spring Boot
 * Task Manager REST API specifications.
 */

/**
 * Task Workflow Status Enum
 * Aligns with Spring `TaskStatus` Java Enum
 */
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';

/**
 * Task Priority Enum
 * Aligns with Spring `TaskPriority` Java Enum
 */
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';

/**
 * Task Response Payload returned from `GET /api/v1/tasks/{id}` and `GET /api/v1/tasks`
 */
export interface TaskResponse {
  id: number;
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate: string | null;
  assignedUser: UserSummaryResponse | null;
  createdAt: string;
  updatedAt: string;
}

/**
 * Request payload for `POST /api/v1/tasks`
 * Aligns with Spring Jakarta Bean Validation:
 *   - `@NotBlank`
 *   - `@Size(min = 3, max = 120)` on title
 *   - `@Size(max = 256)` on description
 */
export interface CreateTaskRequest {
  title: string;
  description?: string;
  status?: TaskStatus;
  priority: TaskPriority;
  dueDate?: string | null;
  assignedUserId?: number | null;
}

/**
 * Request payload for `PUT /api/v1/tasks/{id}`
 */
export interface UpdateTaskRequest {
  title: string;
  description?: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate?: string | null;
  assignedUserId?: number | null;
}

/**
 * Spring Data JPA Pagination Wrapper
 * Matches `org.springframework.data.domain.Page<T>`
 */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

/**
 * Query filter parameters sent to `GET /api/v1/tasks`
 */
export interface TaskFilterParams {
  status?: TaskStatus | 'ALL';
  priority?: TaskPriority | 'ALL';
  search?: string;
  page?: number;
  size?: number;
  sort?: string;
}
