import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { TaskResponse } from '../../../core/models/task.models';

@Component({
  selector: 'app-task-delete-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <div class="delete-dialog">
      <h2 mat-dialog-title class="dialog-title">
        <mat-icon color="warn">warning</mat-icon>
        <span>Confirm Task Deletion</span>
      </h2>

      <mat-dialog-content class="dialog-content">
        <p>Are you sure you want to permanently delete this task?</p>
        <div class="task-preview">
          <strong>"{{ data.task.title }}"</strong>
        </div>
        <p class="warning-sub">This action cannot be undone. It will remove the record from PostgreSQL database.</p>
      </mat-dialog-content>

      <mat-dialog-actions align="end" class="dialog-actions">
        <button mat-button mat-dialog-close>Cancel</button>
        <button mat-flat-button color="warn" (click)="onConfirm()">
          <mat-icon>delete_forever</mat-icon>
          <span>Delete Task</span>
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .delete-dialog {
      min-width: 400px;
      max-width: 500px;
    }

    .dialog-title {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: #b91c1c;
      font-weight: 700;
      padding: 1.25rem 1.5rem 0.5rem 1.5rem;
    }

    .dialog-content {
      padding: 0.5rem 1.5rem 1rem 1.5rem !important;
      font-size: 0.95rem;
      color: var(--text-main);
    }

    .task-preview {
      background: rgba(185, 28, 28, 0.05);
      border-left: 4px solid #b91c1c;
      padding: 0.75rem 1rem;
      margin: 0.75rem 0;
      border-radius: 0 6px 6px 0;
    }

    .warning-sub {
      font-size: 0.8rem;
      color: var(--text-muted);
      margin-top: 0.5rem;
    }

    .dialog-actions {
      padding: 0.75rem 1.5rem 1.25rem 1.5rem;
      border-top: 1px solid var(--border-color);
    }
  `]
})
export class TaskDeleteDialogComponent {
  dialogRef = inject(MatDialogRef<TaskDeleteDialogComponent>);
  data: { task: TaskResponse } = inject(MAT_DIALOG_DATA);

  onConfirm(): void {
    this.dialogRef.close(true);
  }
}
