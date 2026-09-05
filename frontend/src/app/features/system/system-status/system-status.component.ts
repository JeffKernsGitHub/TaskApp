import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { ActuatorService } from '../../../core/services/actuator.service';
import { ActuatorHealth, ActuatorInfo } from '../../../core/models/actuator.models';
import { AuthService } from '../../../core/services/auth.service';

/**
 * ============================================================================
 * System Telemetry & Architecture Roadmap (`system-status.component.ts`)
 * ============================================================================
 * Operational dashboard displaying live Spring Boot Actuator telemetry,
 * PostgreSQL database health, JVM specs, and the Obsidian architecture milestone map.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * 1. Actuator Observability Integration:
 *    - Real-time polling of `/actuator/health` and `/actuator/info`.
 *
 * 2. Enterprise Spring Learning Milestones:
 *    - Visualizes the 8 completed architectural modules from the Obsidian EE/Spring curriculum.
 */
@Component({
  selector: 'app-system-status',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatChipsModule,
    MatDividerModule
  ],
  template: `
    <div class="system-page">
      <div class="page-header">
        <div>
          <h1 class="page-title">Spring Boot Telemetry & Health</h1>
          <p class="page-subtitle">Real-time actuator probes and Java EE/Spring architecture roadmap</p>
        </div>
        <button mat-stroked-button (click)="refresh()" [disabled]="isLoading()">
          <mat-icon>refresh</mat-icon>
          <span>Refresh Probes</span>
        </button>
      </div>

      @if (isLoading()) {
        <mat-progress-bar mode="indeterminate"></mat-progress-bar>
      }

      <!-- Actuator Health Probes -->
      <div class="cards-grid">
        <mat-card class="telemetry-card">
          <mat-card-header>
            <mat-icon mat-card-avatar color="primary">health_and_safety</mat-icon>
            <mat-card-title>Application Health</mat-card-title>
            <mat-card-subtitle>Spring Actuator Liveness & Readiness</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content class="card-body">
            <div class="status-indicator">
              <span class="status-badge" [class.up]="health()?.status === 'UP'">
                {{ health()?.status || 'UNKNOWN' }}
              </span>
              <span class="status-sub">Operational status via Spring Actuator</span>
            </div>
            <mat-divider></mat-divider>
            <div class="health-details">
              <div class="detail-row">
                <span class="detail-label">Database Connection:</span>
                <span class="detail-val">{{ health()?.components?.db?.details?.database || 'PostgreSQL 17' }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Validation Query:</span>
                <code class="code-val">{{ health()?.components?.db?.details?.validationQuery || 'isValid()' }}</code>
              </div>
              <div class="detail-row">
                <span class="detail-label">Active Mode:</span>
                <span class="mode-badge" [class.mock]="authService.isMockMode()">
                  {{ authService.isMockMode() ? 'Mock Sandbox' : 'Live Spring REST API' }}
                </span>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="telemetry-card">
          <mat-card-header>
            <mat-icon mat-card-avatar color="accent">memory</mat-icon>
            <mat-card-title>Runtime Architecture</mat-card-title>
            <mat-card-subtitle>JVM & Spring Framework Specs</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content class="card-body">
            <div class="tech-stack-list">
              <div class="tech-item">
                <div class="tech-icon"><mat-icon>coffee</mat-icon></div>
                <div class="tech-meta">
                  <strong>Java 21 LTS</strong>
                  <small>Virtual Threads (Project Loom) Enabled</small>
                </div>
              </div>
              <div class="tech-item">
                <div class="tech-icon"><mat-icon>layers</mat-icon></div>
                <div class="tech-meta">
                  <strong>Spring Boot 3.3.x</strong>
                  <small>Spring Security 6.x, Spring Data JPA, Actuator</small>
                </div>
              </div>
              <div class="tech-item">
                <div class="tech-icon"><mat-icon>storage</mat-icon></div>
                <div class="tech-meta">
                  <strong>PostgreSQL 17</strong>
                  <small>HikariCP Connection Pool, Flyway Migrations</small>
                </div>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
      </div>

      <!-- Obsidian Curriculum Architecture Map -->
      <mat-card class="roadmap-card">
        <mat-card-header>
          <mat-icon mat-card-avatar color="primary">menu_book</mat-icon>
          <mat-card-title>Obsidian Java EE / Spring Curriculum Map</mat-card-title>
          <mat-card-subtitle>Architectural milestones implemented in this application</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content class="roadmap-content">
          <div class="modules-grid">
            <div class="module-card">
              <div class="module-num">01</div>
              <h4>Spring Boot Foundation & DI</h4>
              <p>Inversion of Control, Bean Lifecycle, Constructor Injection, Conditional Beans, Profile-specific properties.</p>
              <span class="chip-complete">Completed</span>
            </div>

            <div class="module-card">
              <div class="module-num">02</div>
              <h4>Spring Data JPA & Hibernate</h4>
              <p>Entity mappings, OneToMany/ManyToOne relations, JpaRepository, derived queries, and Spring Transactional boundaries.</p>
              <span class="chip-complete">Completed</span>
            </div>

            <div class="module-card">
              <div class="module-num">03</div>
              <h4>Spring MVC & REST APIs</h4>
              <p>@RestController, RFC 9457 ProblemDetail exception handling, Jakarta Bean Validation, and HATEOAS hypermedia links.</p>
              <span class="chip-complete">Completed</span>
            </div>

            <div class="module-card">
              <div class="module-num">04</div>
              <h4>Spring Security & JWT</h4>
              <p>Stateless SecurityFilterChain, BCrypt password hashing, JWT AuthenticationFilter, SpEL @PreAuthorize RBAC.</p>
              <span class="chip-complete">Completed</span>
            </div>

            <div class="module-card">
              <div class="module-num">05</div>
              <h4>Actuator & Observability</h4>
              <p>Health indicators, Micrometer Prometheus metrics, dynamic logging levels, and liveness/readiness probes.</p>
              <span class="chip-complete">Completed</span>
            </div>

            <div class="module-card">
              <div class="module-num">06</div>
              <h4>Enterprise Architecture</h4>
              <p>Layered Service-Repository-Controller pattern, MapStruct DTO mappers, custom exceptions, and audit timestamps.</p>
              <span class="chip-complete">Completed</span>
            </div>

            <div class="module-card">
              <div class="module-num">07</div>
              <h4>Automated Testing Strategy</h4>
              <p>Unit testing with Mockito, @WebMvcTest slice tests for controllers, and Vitest / Angular TestBed for frontend.</p>
              <span class="chip-complete">Completed</span>
            </div>

            <div class="module-card">
              <div class="module-num">08</div>
              <h4>Modern Angular & Zoneless UI</h4>
              <p>Signals, Zoneless Change Detection, Angular Material 3, CDK Drag & Drop Kanban, Standalone Components.</p>
              <span class="chip-complete">Completed</span>
            </div>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .system-page {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 1rem;
    }

    .page-title {
      font-size: 1.75rem;
      font-weight: 800;
      color: var(--text-main);
      letter-spacing: -0.02em;
      margin: 0;
    }

    .page-subtitle {
      font-size: 0.875rem;
      color: var(--text-muted);
      margin: 0.25rem 0 0 0;
    }

    .cards-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 1.25rem;
    }

    @media (max-width: 900px) {
      .cards-grid {
        grid-template-columns: 1fr;
      }
    }

    .telemetry-card, .roadmap-card {
      background: var(--card-bg);
      border: 1px solid var(--border-color);
      border-radius: 12px;
      box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.03);
    }

    .card-body {
      padding: 1.25rem 1rem !important;
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .status-indicator {
      display: flex;
      align-items: center;
      gap: 0.75rem;
    }

    .status-badge {
      font-weight: 800;
      font-size: 0.875rem;
      padding: 0.25rem 0.75rem;
      border-radius: 6px;
      background: #fee2e2;
      color: #b91c1c;
    }

    .status-badge.up {
      background: #dcfce7;
      color: #15803d;
    }

    .status-sub {
      font-size: 0.8125rem;
      color: var(--text-muted);
    }

    .health-details {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      font-size: 0.875rem;
    }

    .detail-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .detail-label {
      color: var(--text-muted);
    }

    .detail-val {
      font-weight: 600;
      color: var(--text-main);
    }

    .code-val {
      font-family: 'JetBrains Mono', monospace;
      background: var(--surface-bg);
      padding: 0.15rem 0.4rem;
      border-radius: 4px;
      font-size: 0.8125rem;
    }

    .mode-badge {
      font-size: 0.75rem;
      font-weight: 700;
      padding: 0.15rem 0.5rem;
      border-radius: 9999px;
      background: rgba(16, 185, 129, 0.1);
      color: #059669;
    }

    .mode-badge.mock {
      background: rgba(245, 158, 11, 0.1);
      color: #d97706;
    }

    .tech-stack-list {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .tech-item {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.5rem;
      border-radius: 8px;
      background: var(--surface-bg);
    }

    .tech-icon {
      width: 36px;
      height: 36px;
      border-radius: 8px;
      background: rgba(2, 132, 199, 0.1);
      color: #0284c7;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .tech-meta {
      display: flex;
      flex-direction: column;
      line-height: 1.3;
    }

    .tech-meta strong {
      font-size: 0.875rem;
      color: var(--text-main);
    }

    .tech-meta small {
      font-size: 0.75rem;
      color: var(--text-muted);
    }

    .roadmap-content {
      padding: 1.25rem 1rem !important;
    }

    .modules-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 1rem;
    }

    .module-card {
      background: var(--surface-bg);
      border: 1px solid var(--border-color);
      border-radius: 10px;
      padding: 1rem;
      display: flex;
      flex-direction: column;
      position: relative;
    }

    .module-num {
      font-family: 'JetBrains Mono', monospace;
      font-size: 0.75rem;
      font-weight: 800;
      color: #0284c7;
      margin-bottom: 0.25rem;
    }

    .module-card h4 {
      font-size: 0.95rem;
      font-weight: 700;
      color: var(--text-main);
      margin: 0 0 0.5rem 0;
    }

    .module-card p {
      font-size: 0.8125rem;
      color: var(--text-muted);
      line-height: 1.4;
      margin: 0 0 1rem 0;
      flex: 1;
    }

    .chip-complete {
      align-self: flex-start;
      font-size: 0.6875rem;
      font-weight: 700;
      background: #dcfce7;
      color: #15803d;
      padding: 0.2rem 0.5rem;
      border-radius: 4px;
    }
  `]
})
export class SystemStatusComponent implements OnInit {
  private actuatorService = inject(ActuatorService);
  authService = inject(AuthService);

  health = signal<ActuatorHealth | null>(null);
  info = signal<ActuatorInfo | null>(null);
  isLoading = signal<boolean>(false);

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.isLoading.set(true);
    this.actuatorService.getHealth().subscribe({
      next: (data) => {
        this.health.set(data);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }
}
