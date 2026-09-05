import { UserRole } from './user.models';

/**
 * ============================================================================
 * Authentication Models (`auth.models.ts`)
 * ============================================================================
 * DTO contracts matching Spring Boot Security 6.x endpoints:
 *   - `POST /api/v1/auth/login`
 *   - `POST /api/v1/auth/register`
 */

/**
 * Request payload sent to `/api/v1/auth/login`
 */
export interface LoginRequest {
  username: string;
  password: string;
}

/**
 * Request payload sent to `/api/v1/auth/register`
 * Matches Jakarta validation: min 3 username, valid email, min 8 password
 */
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

/**
 * Server response returned upon successful authentication
 */
export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  username: string;
  role: UserRole;
}

/**
 * Active authenticated user session details held in Angular Signal state
 */
export interface CurrentUser {
  id?: number;
  username: string;
  email?: string;
  role: UserRole;
  token: string;
}
