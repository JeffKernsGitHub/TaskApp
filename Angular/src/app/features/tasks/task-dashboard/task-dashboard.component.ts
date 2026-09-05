import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { TaskService } from '../../../core/services/task.service';
import { TaskResponse } from '../../../core/models/task.models';

/**
 * ============================================================================
 * Task Analytics Dashboard Component (`task-dashboard.component.ts`)
 * ============================================================================
 * High-level executive dashboard presenting project metrics, completion velocity,
 * status distribution, and upcoming deadlines.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * 1. Computed Signal Derivations (`computed()`):
 *    - All analytics (`totalCount`, `todoCount`, `inProgressCount`, `doneCount`,
 *      `highPriorityCount`, `completionPercentage`, `upcomingTasks`) are pure computed signals.
 *    - They automatically recompute whenever the underlying `tasks` signal changes, with zero
 *      manual recalculation boilerplate.
 *
 * 2. Material 3 Metric KPI Cards:
 *    - Uses responsive CSS grid and Material cards to highlight project KPIs.
 */
@Component({
  selector: 'app-task-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatChipsModule
  ],
  template: `
    <div class="dashboard-page">
      <div class="page-header">
        <div>
          <h1 class="page-title">Project Analytics & KPIs</h1>
          <p class="page-subtitle">Real-time task velocity and workflow telemetry derived from Angular Signals</p>
        </div>
        <div class="header-actions">
          <button mat-flat-button color="primary" routerLink="/tasks/board">
            <mat-icon>view_kanban</mat-icon>
            <span>Open Kanban Board</span>
          </button>
        </div>
      </div>

      <!-- KPI Stat Cards Row -->
      <div class="kpi-grid">
        <mat-card class="kpi-card">
          <div class="kpi-icon-wrapper total-icon">
            <mat-icon>assignment</mat-icon>
          </div>
          <div class="kpi-content">
            <span class="kpi-label">Total Tasks</span>
            <span class="kpi-value">{{ totalCount() }}</span>
          </div>
        </mat-card>

        <mat-card class="kpi-card">
          <div class="kpi-icon-wrapper todo-icon">
            <mat-icon>pending_actions</mat-icon>
          </div>
          <div class="kpi-content">
            <span class="kpi-label">To Do</span>
            <span class="kpi-value">{{ todoCount() }}</span>
          </div>
        </mat-card>

        <mat-card class="kpi-card">
          <div class="kpi-icon-wrapper progress-icon">
            <mat-icon>autorenew</mat-icon>
          </div>
          <div class="kpi-content">
            <span class="kpi-label">In Progress</span>
            <span class="kpi-value">{{ inProgressCount() }}</span>
          </div>
        </mat-card>

        <mat-card class="kpi-card">
          <div class="kpi-icon-wrapper done-icon">
            <mat-icon>check_circle</mat-icon>
          </div>
          <div class="kpi-content">
            <span class="kpi-label">Completed</span>
            <span class="kpi-value">{{ doneCount() }}</span>
          </div>
        </mat-card>
      </div>

      <!-- Velocity & Progress Overview -->
      <div class="analytics-grid">
        <mat-card class="analytics-card">
          <mat-card-header>
            <mat-icon mat-card-avatar color="primary">trending_up</mat-icon>
            <mat-card-title>Completion Velocity</mat-card-title>
            <mat-card-subtitle>Overall sprint resolution progress</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content class="card-content-padded">
            <div class="progress-meta">
              <span class="progress-percent">{{ completionPercentage() }}%</span>
              <span class="progress-sub">{{ doneCount() }} of {{ totalCount() }} tasks finished</span>
            </div>
            <mat-progress-bar mode="determinate" [value]="completionPercentage()" class="velocity-bar"></mat-progress-bar>

            <div class="priority-breakdown">
              <div class="breakdown-item">
                <span class="priority-badge HIGH">HIGH</span>
                <span class="breakdown-count">{{ highPriorityCount() }} tasks</span>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Upcoming Deadlines Card -->
        <mat-card class="analytics-card">
          <mat-card-header>
            <mat-icon mat-card-avatar color="accent">event_upcoming</mat-icon>
            <mat-card-title>Upcoming Deadlines</mat-card-title>
            <mat-card-subtitle>Tasks with scheduled completion targets</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content class="card-content-padded">
            <div class="upcoming-list">
              @for (task of upcomingTasks(); track task.id) {
                <div class="upcoming-item">
                  <div class="upcoming-meta">
                    <span class="upcoming-title">{{ task.title }}</span>
                    <span class="priority-badge" [ngClass]="task.priority">{{ task.priority }}</span>
                  </div>
                  <div class="upcoming-date">
                    <mat-icon class="due-icon">schedule</mat-icon>
                    <span>{{ task.dueDate | date:'mediumDate' }}</span>
                  </div>
                </div>
              } @empty {
                <div class="empty-upcoming">
                  <mat-icon>event_available</mat-icon>
                  <span>No impending deadlines scheduled.</span>
                </div>
              }
            </div>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-page {
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

    .kpi-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 1.25rem;
    }

    .kpi-card {
      padding: 1.25rem;
      display: flex;
      flex-direction: row;
      align-items: center;
      gap: 1rem;
      background: var(--card-bg);
      border: 1px solid var(--border-color);
      border-radius: 12px;
      box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.03);
    }

    .kpi-icon-wrapper {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .total-icon { background: rgba(2, 132, 199, 0.1); color: #0284c7; }
    .todo-icon { background: rgba(100, 116, 139, 0.1); color: #64748b; }
    .progress-icon { background: rgba(245, 158, 11, 0.1); color: #f59e0b; }
    .done-icon { background: rgba(16, 185, 129, 0.1); color: #10b981; }

    .kpi-content {
      display: flex;
      flex-direction: column;
    }

    .kpi-label {
      font-size: 0.8125rem;
      font-weight: 600;
      color: var(--text-muted);
      text-transform: uppercase;
      letter-spacing: 0.03em;
    }

    .kpi-value {
      font-size: 1.75rem;
      font-weight: 800;
      color: var(--text-main);
      line-height: 1.2;
    }

    .analytics-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 1.25rem;
    }

    @media (max-width: 900px) {
      .analytics-grid {
        grid-template-columns: 1fr;
      }
    }

    .analytics-card {
      background: var(--card-bg);
      border: 1px solid var(--border-color);
      border-radius: 12px;
      box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.03);
    }

    .card-content-padded {
      padding: 1.25rem 1rem !important;
    }

    .progress-meta {
      display: flex;
      justify-content: space-between;
      align-items: baseline;
      margin-bottom: 0.75rem;
    }

    .progress-percent {
      font-size: 2rem;
      font-weight: 800;
      color: #0284c7;
    }

    .progress-sub {
      font-size: 0.875rem;
      color: var(--text-muted);
    }

    .velocity-bar {
      height: 10px;
      border-radius: 5px;
      margin-bottom: 1.25rem;
    }

    .priority-breakdown {
      display: flex;
      gap: 1rem;
    }

    .breakdown-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 0.875rem;
      color: var(--text-muted);
    }

    .upcoming-list {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .upcoming-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.65rem 0.75rem;
      background: var(--surface-bg);
      border: 1px solid var(--border-color);
      border-radius: 8px;
    }

    .upcoming-meta {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .upcoming-title {
      font-weight: 600;
      font-size: 0.875rem;
      color: var(--text-main);
    }

    .upcoming-date {
      display: flex;
      align-items: center;
      gap: 0.25rem;
      font-size: 0.75rem;
      color: var(--text-muted);
    }

    .due-icon {
      font-size: 14px;
      width: 14px;
      height: 14px;
    }

    .empty-upcoming {
      padding: 2rem 1rem;
      text-align: center;
      color: var(--text-muted);
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.5rem;
      font-size: 0.875rem;
    }
  `]
})
export class TaskDashboardComponent implements OnInit {
  private taskService = inject(TaskService);

  tasks = signal<TaskResponse[]>([]);

  // Computed metrics automatically deriving from `tasks` signal
  totalCount = computed(() => this.tasks().length);
  todoCount = computed(() => this.tasks().filter(t => t.status === 'TODO').length);
  inProgressCount = computed(() => this.tasks().filter(t => t.status === 'IN_PROGRESS').length);
  doneCount = computed(() => this.tasks().filter(t => t.status === 'DONE').length);
  highPriorityCount = computed(() => this.tasks().filter(t => t.priority === 'HIGH').length);

  completionPercentage = computed(() => {
    const total = this.totalCount();
    if (total === 0) return 0;
    return Math.round((this.doneCount() / total) * 100);
  });

  upcomingTasks = computed(() => {
    return this.tasks()
      .filter(t => t.dueDate && t.status !== 'DONE')
      .slice(0, 5);
  });

  ngOnInit(): void {
    this.taskService.getTasks({ size: 100 }).subscribe(page => {
      this.tasks.set(page.content);
    });
  }
}
