import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ActuatorHealth, ActuatorInfo } from '../models/actuator.models';
import { AuthService } from './auth.service';

/**
 * ============================================================================
 * Spring Boot Actuator Telemetry Service (`actuator.service.ts`)
 * ============================================================================
 * Queries Spring Boot Actuator endpoints for system observability:
 *   - `/actuator/health` (PostgreSQL connection, Disk Space, Liveness/Readiness probes)
 *   - `/actuator/info` (Java version, application name, virtual thread state)
 *
 * KEY CONCEPTS DEMONSTRATED:
 * 1. Observability Integration:
 *    - Connects frontend runtime diagnostics with backend operational telemetry.
 *
 * 2. Resilience with Fallbacks:
 *    - Provides simulated healthy Actuator telemetry when running in offline mock mode.
 */
@Injectable({
  providedIn: 'root'
})
export class ActuatorService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);

  /**
   * Fetches health probe status from Spring Boot Actuator
   */
  getHealth(): Observable<ActuatorHealth> {
    if (this.authService.isMockMode()) {
      return of(this.getMockHealth());
    }

    return this.http.get<ActuatorHealth>('/actuator/health').pipe(
      catchError(() => of(this.getMockHealth()))
    );
  }

  /**
   * Fetches build and JVM diagnostic info
   */
  getInfo(): Observable<ActuatorInfo> {
    if (this.authService.isMockMode()) {
      return of(this.getMockInfo());
    }

    return this.http.get<ActuatorInfo>('/actuator/info').pipe(
      catchError(() => of(this.getMockInfo()))
    );
  }

  private getMockHealth(): ActuatorHealth {
    return {
      status: 'UP',
      components: {
        db: {
          status: 'UP',
          details: {
            database: 'PostgreSQL 17',
            validationQuery: 'isValid()'
          }
        },
        diskSpace: {
          status: 'UP',
          details: {
            total: 512000000000,
            free: 345000000000,
            threshold: 10485760
          }
        },
        ping: {
          status: 'UP'
        }
      }
    };
  }

  private getMockInfo(): ActuatorInfo {
    return {
      app: {
        name: 'Task Manager Spring Boot API',
        description: 'Enterprise REST API backed by Spring Security, Spring Data JPA, and Virtual Threads',
        version: '1.0.0',
        java: {
          version: '21.0.3 (Temurin LTS)',
          vendor: 'Eclipse Adoptium'
        }
      }
    };
  }
}
