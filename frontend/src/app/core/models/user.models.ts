/**
 * ============================================================================
 * User Domain Models (`user.models.ts`)
 * ============================================================================
 * DTO contracts for user management and RBAC authorization matching
 * the Spring Boot `/api/v1/users` administration endpoints.
 */

/**
 * User Role Enum for Role-Based Access Control (RBAC)
 */
export type UserRole = 'ADMIN' | 'USER';

/**
 * Lightweight User Summary DTO (nested inside Task responses)
 */
export interface UserSummaryResponse {
  id: number;
  username: string;
  email: string;
}

/**
 * Full User Profile DTO returned by Admin user endpoints
 */
export interface UserResponse {
  id: number;
  username: string;
  email: string;
  role: UserRole;
  createdAt: string;
  updatedAt: string;
}

/**
 * Request payload for creating a user account (Admin endpoint)
 */
export interface CreateUserRequest {
  username: string;
  email: string;
  password?: string;
  role?: UserRole;
}

/**
 * Request payload for updating a user account (Admin endpoint)
 */
export interface UpdateUserRequest {
  username: string;
  email: string;
  role?: UserRole;
}
