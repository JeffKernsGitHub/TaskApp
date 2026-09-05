import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CreateTaskRequest, TaskPriority, TaskResponse, TaskStatus, UpdateTaskRequest } from '../../../core/models/task.models';
import { UserService } from '../../../core/services/user.service';
import { UserResponse } from '../../../core/models/user.models';

export interface TaskDialogData {
  mode: 'create' | 'edit';
  task?: TaskResponse;
  initialStatus?: TaskStatus;
}

/**
 * ============================================================================
 * Task Create / Edit Dialog Component (`task-dialog.component.ts`)
 * ============================================================================
 * Modal dialog backed by Angular Reactive Forms with validation constraints
 * mirrored directly from Spring Boot Jakarta Bean Validation (@Size, @NotBlank).
 *
 * KEY CONCEPTS DEMONSTRATED:
 * 1. Angular Reactive Forms (`FormBuilder`, `FormGroup`, `Validators`):
 *    - Strict form controls with synchronous validation rules.
 *    - Title: Required, 3 - 120 characters (aligns with Spring `@Size(min=3, max=120)`).
 *    - Description: Max 256 characters (aligns with Spring `@Size(max=256)`).
 *
 * 2. Material Dialog Injection:
 *    - `MAT_DIALOG_DATA`: Injects caller data into dialog instance.
 *    - `MatDialogRef`: Provides `.close(payload)` to return modified task data to caller.
 *
 * 3. Angular Material Datepicker (`mat-datepicker`):
 *    - Formats selected date as `YYYY-MM-DD` ISO string for Spring's `LocalDate` deserializer.
 */
@Component({
  selector: 'app-task-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <div class="task-dialog">
      <h2 mat-dialog-title class="dialog-title">
        <mat-icon color="primary">{{ data.mode === 'create' ? 'add_task' : 'edit_note' }}</mat-icon>
        <span>{{ data.mode === 'create' ? 'Create New Task' : 'Edit Task #' + data.task?.id }}</span>
      </h2>

      <mat-dialog-content class="dialog-content">
        <form [formGroup]="taskForm" class="task-form">
          <!-- Title Input -->
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Task Title</mat-label>
            <input matInput formControlName="title" placeholder="e.g. Implement OAuth2 Resource Server" required>
            @if (taskForm.get('title')?.hasError('required') && taskForm.get('title')?.touched) {
              <mat-error>Title is required</mat-error>
            }
            @if (taskForm.get('title')?.hasError('minlength')) {
              <mat-error>Title must be at least 3 characters</mat-error>
            }
            @if (taskForm.get('title')?.hasError('maxlength')) {
              <mat-error>Title cannot exceed 120 characters</mat-error>
            }
          </mat-form-field>

          <!-- Description Input -->
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Description</mat-label>
            <textarea
              matInput
              formControlName="description"
              rows="3"
              placeholder="Provide technical implementation details and acceptance criteria..."
            ></textarea>
            @if (taskForm.get('description')?.hasError('maxlength')) {
              <mat-error>Description cannot exceed 256 characters</mat-error>
            }
          </mat-form-field>

          <!-- Two-column Row: Status & Priority -->
          <div class="form-row">
            <mat-form-field appearance="outline" class="half-width">
              <mat-label>Status</mat-label>
              <mat-select formControlName="status">
                <mat-option value="TODO">To Do</mat-option>
                <mat-option value="IN_PROGRESS">In Progress</mat-option>
                <mat-option value="DONE">Done</mat-option>
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline" class="half-width">
              <mat-label>Priority</mat-label>
              <mat-select formControlName="priority">
                <mat-option value="LOW">Low</mat-option>
                <mat-option value="MEDIUM">Medium</mat-option>
                <mat-option value="HIGH">High</mat-option>
              </mat-select>
            </mat-form-field>
          </div>

          <!-- Two-column Row: Due Date & Assignee -->
          <div class="form-row">
            <mat-form-field appearance="outline" class="half-width">
              <mat-label>Due Date</mat-label>
              <input matInput [matDatepicker]="picker" formControlName="dueDate" placeholder="Choose a date">
              <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
              <mat-datepicker #picker></mat-datepicker>
            </mat-form-field>

            <mat-form-field appearance="outline" class="half-width">
              <mat-label>Assignee</mat-label>
              <mat-select formControlName="assignedUserId">
                <mat-option [value]="null">Unassigned</mat-option>
                @for (user of users(); track user.id) {
                  <mat-option [value]="user.id">{{ user.username }} ({{ user.role }})</mat-option>
                }
              </mat-select>
            </mat-form-field>
          </div>
        </form>
      </mat-dialog-content>

      <mat-dialog-actions align="end" class="dialog-actions">
        <button mat-button mat-dialog-close>Cancel</button>
        <button mat-flat-button color="primary" [disabled]="taskForm.invalid" (click)="onSave()">
          <mat-icon>check</mat-icon>
          <span>{{ data.mode === 'create' ? 'Create Task' : 'Save Changes' }}</span>
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .task-dialog {
      min-width: 480px;
      max-width: 580px;
    }

    .dialog-title {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 1.25rem;
      font-weight: 700;
      color: var(--text-main);
      padding: 1.25rem 1.5rem 0.5rem 1.5rem;
    }

    .dialog-content {
      padding: 1rem 1.5rem !important;
    }

    .task-form {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .full-width {
      width: 100%;
    }

    .form-row {
      display: flex;
      gap: 1rem;
    }

    .half-width {
      flex: 1;
    }

    .dialog-actions {
      padding: 1rem 1.5rem 1.25rem 1.5rem;
      border-top: 1px solid var(--border-color);
    }
  `]
})
export class TaskDialogComponent implements OnInit {
  dialogRef = inject(MatDialogRef<TaskDialogComponent>);
  data: TaskDialogData = inject(MAT_DIALOG_DATA);
  private fb = inject(FormBuilder);
  private userService = inject(UserService);

  users = signal<UserResponse[]>([]);

  taskForm: FormGroup = this.fb.group({
    title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
    description: ['', [Validators.maxLength(256)]],
    status: ['TODO', [Validators.required]],
    priority: ['MEDIUM', [Validators.required]],
    dueDate: [null],
    assignedUserId: [null]
  });

  ngOnInit(): void {
    this.userService.getAllUsersList().subscribe(list => {
      this.users.set(list);
    });

    if (this.data.mode === 'create' && this.data.initialStatus) {
      this.taskForm.patchValue({ status: this.data.initialStatus });
    }

    if (this.data.mode === 'edit' && this.data.task) {
      const t = this.data.task;
      this.taskForm.patchValue({
        title: t.title,
        description: t.description,
        status: t.status,
        priority: t.priority,
        dueDate: t.dueDate ? new Date(t.dueDate) : null,
        assignedUserId: t.assignedUser?.id ?? null
      });
    }
  }

  onSave(): void {
    if (this.taskForm.invalid) return;

    const val = this.taskForm.value;
    const formattedDueDate = val.dueDate instanceof Date
      ? val.dueDate.toISOString().split('T')[0]
      : val.dueDate;

    const payload = {
      ...val,
      dueDate: formattedDueDate
    };

    this.dialogRef.close(payload);
  }
}
