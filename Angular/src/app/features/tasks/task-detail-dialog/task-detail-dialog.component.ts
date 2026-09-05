import { Component, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { TaskResponse } from '../../../core/models/task.models';

export interface TaskDetailDialogData {
  task: TaskResponse;
  canEdit: boolean;
  canDelete: boolean;
}

@Component({
  selector: 'app-task-detail-dialog',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatChipsModule
  ],
  template: `
    <div class="task-detail-dialog">
      <div class="dialog-header">
        <div class="header-badges">
          <span class="status-badge" [ngClass]="data.task.status">{{ formatStatus(data.task.status) }}</span>
          <span class="priority-badge" [ngClass]="data.task.priority">{{ data.task.priority }} Priority</span>
          <span class="task-id">#{{ data.task.id }}</span>
        </div>
        <button mat-icon-button mat-dialog-close class="close-btn">
          <mat-icon>close</mat-icon>
        </button>
      </div>

      <mat-dialog-content class="dialog-body">
        <h2 class="task-title">{{ data.task.title }}</h2>

        <div class="task-description-box">
          <h4 class="section-label">Description</h4>
          <p class="description-text">
            {{ data.task.description || 'No description provided for this task.' }}
          </p>
        </div>

        <div class="metadata-grid">
          <div class="meta-item">
            <span class="meta-label"><mat-icon>person</mat-icon> Assignee</span>
            <span class="meta-value">
              @if (data.task.assignedUser) {
                <div class="assignee-tag">
                  <div class="assignee-avatar">{{ data.task.assignedUser.username.charAt(0).toUpperCase() }}</div>
                  <span>{{ data.task.assignedUser.username }}</span>
                </div>
              } @else {
                <span class="unassigned-tag">Unassigned</span>
              }
            </span>
          </div>

          <div class="meta-item">
            <span class="meta-label"><mat-icon>event</mat-icon> Due Date</span>
            <span class="meta-value">
              {{ data.task.dueDate ? (data.task.dueDate | date:'mediumDate') : 'No deadline' }}
            </span>
          </div>

          <div class="meta-item">
            <span class="meta-label"><mat-icon>schedule</mat-icon> Created</span>
            <span class="meta-value">
              {{ data.task.createdAt | date:'medium' }}
            </span>
          </div>

          <div class="meta-item">
            <span class="meta-label"><mat-icon>update</mat-icon> Last Updated</span>
            <span class="meta-value">
              {{ data.task.updatedAt | date:'medium' }}
            </span>
          </div>
        </div>
      </mat-dialog-content>

      <mat-dialog-actions align="end" class="dialog-actions">
        @if (data.canDelete) {
          <button mat-button color="warn" (click)="onDelete()">
            <mat-icon>delete</mat-icon>
            <span>Delete</span>
          </button>
        }
        @if (data.canEdit) {
          <button mat-stroked-button color="primary" (click)="onEdit()">
            <mat-icon>edit</mat-icon>
            <span>Edit</span>
          </button>
        }
        <button mat-flat-button color="primary" mat-dialog-close>Close</button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .task-detail-dialog {
      min-width: 520px;
      max-width: 650px;
    }

    .dialog-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1.25rem 1.5rem 0.5rem 1.5rem;
    }

    .header-badges {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .task-id {
      font-family: 'JetBrains Mono', monospace;
      font-size: 0.8rem;
      color: var(--text-muted);
      font-weight: 600;
      margin-left: 4px;
    }

    .task-title {
      font-size: 1.4rem;
      font-weight: 700;
      color: var(--text-main);
      margin: 0.5rem 0 1.25rem 0;
      line-height: 1.3;
    }

    .task-description-box {
      background: var(--surface-bg);
      border: 1px solid var(--border-color);
      border-radius: 8px;
      padding: 1rem;
      margin-bottom: 1.25rem;
    }

    .section-label {
      margin: 0 0 0.5rem 0;
      font-size: 0.75rem;
      font-weight: 700;
      color: var(--text-muted);
      letter-spacing: 0.05em;
      text-transform: uppercase;
    }

    .description-text {
      margin: 0;
      font-size: 0.95rem;
      line-height: 1.5;
      color: var(--text-main);
      white-space: pre-wrap;
    }

    .metadata-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1.25rem;
      margin-top: 1rem;
    }

    .meta-item {
      display: flex;
      flex-direction: column;
      gap: 0.35rem;
    }

    .meta-label {
      display: flex;
      align-items: center;
      gap: 0.35rem;
      font-size: 0.75rem;
      font-weight: 600;
      color: var(--text-muted);
    }

    .meta-label mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
    }

    .meta-value {
      font-size: 0.9rem;
      font-weight: 500;
      color: var(--text-main);
    }

    .assignee-tag {
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      background: rgba(2, 132, 199, 0.1);
      padding: 2px 8px;
      border-radius: 9999px;
      font-size: 0.8rem;
      font-weight: 600;
      color: #0284c7;
    }

    .assignee-avatar {
      width: 20px;
      height: 20px;
      border-radius: 50%;
      background: #0284c7;
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.7rem;
    }

    .unassigned-tag {
      color: var(--text-muted);
      font-style: italic;
    }

    .dialog-actions {
      padding: 1rem 1.5rem 1.25rem 1.5rem;
      border-top: 1px solid var(--border-color);
      gap: 0.5rem;
    }
  `]
})
export class TaskDetailDialogComponent {
  dialogRef = inject(MatDialogRef<TaskDetailDialogComponent>);
  data: TaskDetailDialogData = inject(MAT_DIALOG_DATA);

  formatStatus(status: string): string {
    return status.replace('_', ' ');
  }

  onEdit(): void {
    this.dialogRef.close({ action: 'edit', task: this.data.task });
  }

  onDelete(): void {
    this.dialogRef.close({ action: 'delete', task: this.data.task });
  }
}
