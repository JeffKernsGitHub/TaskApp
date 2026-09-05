/**
 * ============================================================================
 * RFC 9457 Problem Details for HTTP APIs (`problem-detail.models.ts`)
 * ============================================================================
 * Standard error response schema implemented in Spring Boot 3.x / 4.x via
 * `org.springframework.http.ProblemDetail` and global `@RestControllerAdvice`.
 *
 * Provides structured machine-readable error responses across REST endpoints.
 */
export interface ProblemDetail {
  type?: string;
  title: string;
  status: number;
  detail?: string;
  instance?: string;
  timestamp?: string;
  invalidParams?: InvalidParameter[];
  [key: string]: any;
}

/**
 * Individual field validation error detail
 */
export interface InvalidParameter {
  name: string;
  reason: string;
}
