import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CdkDragDrop, DragDropModule, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialog } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { CreateTaskRequest, TaskFilterParams, TaskPriority, TaskResponse, TaskStatus, UpdateTaskRequest } from '../../../core/models/task.models';
import { TaskService } from '../../../core/services/task.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { TaskDialogComponent } from '../task-dialog/task-dialog.component';
import { TaskDetailDialogComponent } from '../task-detail-dialog/task-detail-dialog.component';
import { TaskDeleteDialogComponent } from '../task-delete-dialog/task-delete-dialog.component';

/**
 * ============================================================================
 * Interactive Kanban Task Board (`task-board.component.ts`)
 * ============================================================================
 * Provides an agile workflow board dividing tasks across `TODO`, `IN_PROGRESS`,
 * and `DONE` swimlanes.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * 1. Angular CDK Drag & Drop (`DragDropModule`):
 *    - `cdkDropListGroup`: Coordinates drag-and-drop between multiple linked columns.
 *    - `cdkDropList`: Defines drop target zones with `[cdkDropListData]`.
 *    - `cdkDrag`: Marks draggable cards.
 *    - `(cdkDropListDropped)`: Triggers event handler when a user drops a card.
 *
 * 2. CDK Helper Functions:
 *    - `moveItemInArray`: Reorders items within the same column.
 *    - `transferArrayItem`: Moves an item between columns and updates its backend status.
 *
 * 3. Signals & Reactive Derivation (`signal`, `computed`):
 *    - `tasks = signal<TaskResponse[]>([])`: Central list of loaded tasks.
 *    - `todoTasks`, `inProgressTasks`, `doneTasks`: Computed signals that automatically
 *      partition and filter tasks by status and active search queries.
 */
@Component({
  selector: 'app-task-board',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    FormsModule,
    DragDropModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTooltipModule,
    MatProgressBarModule
  ],
  template: `
    <div class="board-page">
      <!-- Header Bar with Search & Action Controls -->
      <div class="board-header">
        <div>
          <h1 class="page-title">Agile Kanban Board</h1>
          <p class="page-subtitle">Drag and drop tasks between workflow columns to update Spring backend status</p>
        </div>
        <div class="header-actions">
          <button mat-flat-button color="primary" class="create-btn" (click)="openCreateDialog()">
            <mat-icon>add</mat-icon>
            <span>Create Task</span>
          </button>
        </div>
      </div>

      <!-- Filter Controls Bar -->
      <div class="filter-card">
        <mat-form-field appearance="outline" class="search-input" subscriptSizing="dynamic">
          <mat-icon matPrefix>search</mat-icon>
          <input matInput [(ngModel)]="searchQuery" (ngModelChange)="applyFilters()" placeholder="Search title or description...">
          @if (searchQuery) {
            <button mat-icon-button matSuffix (click)="searchQuery = ''; applyFilters()">
              <mat-icon>clear</mat-icon>
            </button>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="priority-select" subscriptSizing="dynamic">
          <mat-label>Priority Filter</mat-label>
          <mat-select [(ngModel)]="selectedPriority" (ngModelChange)="applyFilters()">
            <mat-option value="ALL">All Priorities</mat-option>
            <mat-option value="HIGH">High Priority</mat-option>
            <mat-option value="MEDIUM">Medium Priority</mat-option>
            <mat-option value="LOW">Low Priority</mat-option>
          </mat-select>
        </mat-form-field>
      </div>

      @if (isLoading()) {
        <mat-progress-bar mode="indeterminate" class="loading-bar"></mat-progress-bar>
      }

      <!-- CDK Drag and Drop Kanban Board Columns -->
      <div class="kanban-grid" cdkDropListGroup>
        <!-- TO DO Column -->
        <div class="kanban-column">
          <div class="column-header todo-header">
            <div class="column-title-group">
              <span class="column-title">To Do</span>
              <span class="column-count">{{ todoTasks().length }}</span>
            </div>
            <button mat-icon-button (click)="openCreateDialog('TODO')" matTooltip="Add To Do">
              <mat-icon>add</mat-icon>
            </button>
          </div>

          <div
            cdkDropList
            [cdkDropListData]="todoTasks()"
            (cdkDropListDropped)="drop($event, 'TODO')"
            id="TODO"
            class="task-drop-zone"
          >
            @for (task of todoTasks(); track task.id) {
              <div class="task-card" cdkDrag>
                <div class="card-header-row">
                  <span class="priority-badge" [ngClass]="task.priority">{{ task.priority }}</span>
                  <span class="task-id">#{{ task.id }}</span>
                </div>
                <h3 class="task-title" (click)="openDetailDialog(task)">{{ task.title }}</h3>
                @if (task.description) {
                  <p class="task-desc">{{ task.description }}</p>
                }
                <div class="task-footer">
                  <div class="task-assignee">
                    @if (task.assignedUser) {
                      <div class="avatar" [matTooltip]="task.assignedUser.email">
                        {{ task.assignedUser.username.charAt(0).toUpperCase() }}
                      </div>
                      <span class="assignee-name">{{ task.assignedUser.username }}</span>
                    } @else {
                      <span class="unassigned-text">Unassigned</span>
                    }
                  </div>
                  @if (task.dueDate) {
                    <div class="due-date">
                      <mat-icon class="due-icon">event</mat-icon>
                      <span>{{ task.dueDate | date:'mediumDate' }}</span>
                    </div>
                  }
                </div>
                <div class="card-actions">
                  <button mat-icon-button (click)="openEditDialog(task)" matTooltip="Edit Task">
                    <mat-icon>edit</mat-icon>
                  </button>
                  <button mat-icon-button (click)="openDeleteDialog(task)" matTooltip="Delete Task" color="warn">
                    <mat-icon>delete</mat-icon>
                  </button>
                </div>
              </div>
            } @empty {
              <div class="empty-column-placeholder">
                <mat-icon>inbox</mat-icon>
                <span>No tasks in To Do</span>
              </div>
            }
          </div>
        </div>

        <!-- IN PROGRESS Column -->
        <div class="kanban-column">
          <div class="column-header progress-header">
            <div class="column-title-group">
              <span class="column-title">In Progress</span>
              <span class="column-count">{{ inProgressTasks().length }}</span>
            </div>
            <button mat-icon-button (click)="openCreateDialog('IN_PROGRESS')" matTooltip="Add In Progress">
              <mat-icon>add</mat-icon>
            </button>
          </div>

          <div
            cdkDropList
            [cdkDropListData]="inProgressTasks()"
            (cdkDropListDropped)="drop($event, 'IN_PROGRESS')"
            id="IN_PROGRESS"
            class="task-drop-zone"
          >
            @for (task of inProgressTasks(); track task.id) {
              <div class="task-card" cdkDrag>
                <div class="card-header-row">
                  <span class="priority-badge" [ngClass]="task.priority">{{ task.priority }}</span>
                  <span class="task-id">#{{ task.id }}</span>
                </div>
                <h3 class="task-title" (click)="openDetailDialog(task)">{{ task.title }}</h3>
                @if (task.description) {
                  <p class="task-desc">{{ task.description }}</p>
                }
                <div class="task-footer">
                  <div class="task-assignee">
                    @if (task.assignedUser) {
                      <div class="avatar" [matTooltip]="task.assignedUser.email">
                        {{ task.assignedUser.username.charAt(0).toUpperCase() }}
                      </div>
                      <span class="assignee-name">{{ task.assignedUser.username }}</span>
                    } @else {
                      <span class="unassigned-text">Unassigned</span>
                    }
                  </div>
                  @if (task.dueDate) {
                    <div class="due-date">
                      <mat-icon class="due-icon">event</mat-icon>
                      <span>{{ task.dueDate | date:'mediumDate' }}</span>
                    </div>
                  }
                </div>
                <div class="card-actions">
                  <button mat-icon-button (click)="openEditDialog(task)" matTooltip="Edit Task">
                    <mat-icon>edit</mat-icon>
                  </button>
                  <button mat-icon-button (click)="openDeleteDialog(task)" matTooltip="Delete Task" color="warn">
                    <mat-icon>delete</mat-icon>
                  </button>
                </div>
              </div>
            } @empty {
              <div class="empty-column-placeholder">
                <mat-icon>pending</mat-icon>
                <span>No tasks In Progress</span>
              </div>
            }
          </div>
        </div>

        <!-- DONE Column -->
        <div class="kanban-column">
          <div class="column-header done-header">
            <div class="column-title-group">
              <span class="column-title">Done</span>
              <span class="column-count">{{ doneTasks().length }}</span>
            </div>
            <button mat-icon-button (click)="openCreateDialog('DONE')" matTooltip="Add Completed Task">
              <mat-icon>add</mat-icon>
            </button>
          </div>

          <div
            cdkDropList
            [cdkDropListData]="doneTasks()"
            (cdkDropListDropped)="drop($event, 'DONE')"
            id="DONE"
            class="task-drop-zone"
          >
            @for (task of doneTasks(); track task.id) {
              <div class="task-card done-card" cdkDrag>
                <div class="card-header-row">
                  <span class="priority-badge" [ngClass]="task.priority">{{ task.priority }}</span>
                  <span class="task-id">#{{ task.id }}</span>
                </div>
                <h3 class="task-title done-text" (click)="openDetailDialog(task)">{{ task.title }}</h3>
                @if (task.description) {
                  <p class="task-desc">{{ task.description }}</p>
                }
                <div class="task-footer">
                  <div class="task-assignee">
                    @if (task.assignedUser) {
                      <div class="avatar" [matTooltip]="task.assignedUser.email">
                        {{ task.assignedUser.username.charAt(0).toUpperCase() }}
                      </div>
                      <span class="assignee-name">{{ task.assignedUser.username }}</span>
                    } @else {
                      <span class="unassigned-text">Unassigned</span>
                    }
                  </div>
                  @if (task.dueDate) {
                    <div class="due-date">
                      <mat-icon class="due-icon">check_circle</mat-icon>
                      <span>{{ task.dueDate | date:'mediumDate' }}</span>
                    </div>
                  }
                </div>
                <div class="card-actions">
                  <button mat-icon-button (click)="openEditDialog(task)" matTooltip="Edit Task">
                    <mat-icon>edit</mat-icon>
                  </button>
                  <button mat-icon-button (click)="openDeleteDialog(task)" matTooltip="Delete Task" color="warn">
                    <mat-icon>delete</mat-icon>
                  </button>
                </div>
              </div>
            } @empty {
              <div class="empty-column-placeholder">
                <mat-icon>task_alt</mat-icon>
                <span>No completed tasks yet</span>
              </div>
            }
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .board-page {
      display: flex;
      flex-direction: column;
      gap: 1.25rem;
    }

    .board-header {
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

    .create-btn {
      height: 48px;
      font-weight: 600;
      border-radius: 8px;
      display: flex;
      align-items: center;
      gap: 0.4rem;
    }

    .filter-card {
      display: flex;
      align-items: center;
      gap: 1rem;
      flex-wrap: wrap;
      background: var(--card-bg);
      border: 1px solid var(--border-color);
      padding: 0.75rem 1rem;
      border-radius: 12px;
    }

    .search-input {
      flex: 1;
      min-width: 240px;
    }

    .priority-select {
      width: 200px;
    }

    .loading-bar {
      border-radius: 4px;
    }

    .kanban-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 1.25rem;
      align-items: flex-start;
    }

    @media (max-width: 1024px) {
      .kanban-grid {
        grid-template-columns: 1fr;
      }
    }

    .kanban-column {
      background: var(--surface-bg);
      border: 1px solid var(--border-color);
      border-radius: 12px;
      overflow: hidden;
      display: flex;
      flex-direction: column;
      min-height: 500px;
    }

    .column-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.75rem 1rem;
      border-bottom: 2px solid var(--border-color);
    }

    .todo-header { border-bottom-color: #64748b; }
    .progress-header { border-bottom-color: #f59e0b; }
    .done-header { border-bottom-color: #10b981; }

    .column-title-group {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .column-title {
      font-weight: 700;
      font-size: 0.95rem;
      color: var(--text-main);
    }

    .column-count {
      font-size: 0.75rem;
      font-weight: 700;
      padding: 0.15rem 0.5rem;
      background: var(--card-bg);
      border: 1px solid var(--border-color);
      border-radius: 9999px;
      color: var(--text-muted);
    }

    .task-drop-zone {
      padding: 0.75rem;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      flex: 1;
      min-height: 400px;
    }

    .task-card {
      background: var(--card-bg);
      border: 1px solid var(--border-color);
      border-radius: 10px;
      padding: 1rem;
      box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.04);
      cursor: grab;
      transition: transform 0.15s ease, box-shadow 0.15s ease;
      position: relative;
    }

    .task-card:hover {
      box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.08);
      border-color: #93c5fd;
    }

    .card-header-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.5rem;
    }

    .task-id {
      font-family: 'JetBrains Mono', monospace;
      font-size: 0.75rem;
      font-weight: 600;
      color: var(--text-muted);
    }

    .task-title {
      font-size: 0.95rem;
      font-weight: 700;
      color: var(--text-main);
      margin: 0 0 0.35rem 0;
      line-height: 1.35;
      cursor: pointer;
    }

    .task-title:hover {
      color: #0284c7;
    }

    .done-text {
      text-decoration: line-through;
      color: var(--text-muted);
    }

    .task-desc {
      font-size: 0.8125rem;
      color: var(--text-muted);
      line-height: 1.4;
      margin: 0 0 0.75rem 0;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .task-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 0.75rem;
      color: var(--text-muted);
      padding-top: 0.5rem;
      border-top: 1px dashed var(--border-color);
    }

    .task-assignee {
      display: flex;
      align-items: center;
      gap: 0.35rem;
    }

    .avatar {
      width: 22px;
      height: 22px;
      border-radius: 50%;
      background: #0284c7;
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.6875rem;
      font-weight: 700;
    }

    .assignee-name {
      font-weight: 500;
      color: var(--text-main);
    }

    .unassigned-text {
      font-style: italic;
    }

    .due-date {
      display: flex;
      align-items: center;
      gap: 0.25rem;
    }

    .due-icon {
      font-size: 14px;
      width: 14px;
      height: 14px;
    }

    .card-actions {
      display: flex;
      justify-content: flex-end;
      gap: 0.25rem;
      margin-top: 0.5rem;
    }

    .card-actions button {
      width: 28px;
      height: 28px;
      line-height: 28px;
    }

    .card-actions mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
    }

    .empty-column-placeholder {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 3rem 1rem;
      color: var(--text-muted);
      gap: 0.5rem;
      font-size: 0.8125rem;
      border: 2px dashed var(--border-color);
      border-radius: 8px;
      margin: 1rem 0;
    }
  `]
})
export class TaskBoardComponent implements OnInit {
  private taskService = inject(TaskService);
  private authService = inject(AuthService);
  private dialog = inject(MatDialog);
  private notification = inject(NotificationService);

  // --- Signal State ---
  tasks = signal<TaskResponse[]>([]);
  isLoading = signal<boolean>(false);

  // --- Filter State ---
  searchQuery = '';
  selectedPriority: TaskPriority | 'ALL' = 'ALL';

  // --- Computed Column Partitions ---
  todoTasks = computed(() => this.filterTasksByStatus('TODO'));
  inProgressTasks = computed(() => this.filterTasksByStatus('IN_PROGRESS'));
  doneTasks = computed(() => this.filterTasksByStatus('DONE'));

  ngOnInit(): void {
    this.loadTasks();
  }

  loadTasks(): void {
    this.isLoading.set(true);
    this.taskService.getTasks({ size: 100 }).subscribe({
      next: (page) => {
        this.tasks.set(page.content);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  private filterTasksByStatus(status: TaskStatus): TaskResponse[] {
    return this.tasks().filter(task => {
      const matchesStatus = task.status === status;
      const matchesPriority = this.selectedPriority === 'ALL' || task.priority === this.selectedPriority;
      const query = this.searchQuery.trim().toLowerCase();
      const matchesQuery = !query ||
        task.title.toLowerCase().includes(query) ||
        (task.description && task.description.toLowerCase().includes(query));

      return matchesStatus && matchesPriority && matchesQuery;
    });
  }

  applyFilters(): void {
    // Computed signals automatically trigger re-evaluation
  }

  /**
   * Handles CDK Drag & Drop events between status swimlanes
   */
  drop(event: CdkDragDrop<TaskResponse[]>, newStatus: TaskStatus): void {
    if (event.previousContainer === event.container) {
      // Reordering within the same column
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      // Moving across columns
      const task = event.previousContainer.data[event.previousIndex];
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );

      // Persist status change to Spring backend
      this.taskService.updateTaskStatus(task.id, newStatus).subscribe({
        next: () => {
          this.notification.showSuccess(`Task #${task.id} moved to ${newStatus.replace('_', ' ')}`);
          this.loadTasks();
        },
        error: () => {
          this.loadTasks(); // Rollback on error
        }
      });
    }
  }

  openCreateDialog(initialStatus: TaskStatus = 'TODO'): void {
    const dialogRef = this.dialog.open(TaskDialogComponent, {
      data: { mode: 'create', initialStatus },
      width: '560px'
    });

    dialogRef.afterClosed().subscribe((req: CreateTaskRequest) => {
      if (req) {
        this.taskService.createTask(req).subscribe({
          next: () => {
            this.notification.showSuccess('Task created successfully');
            this.loadTasks();
          }
        });
      }
    });
  }

  openDetailDialog(task: TaskResponse): void {
    const currentUser = this.authService.currentUser();
    const isAdmin = this.authService.isAdmin();
    const isOwner = currentUser?.username === task.assignedUser?.username;
    const canEdit = isAdmin || isOwner || this.authService.isMockMode();
    const canDelete = isAdmin || isOwner || this.authService.isMockMode();

    const dialogRef = this.dialog.open(TaskDetailDialogComponent, {
      data: { task, canEdit, canDelete },
      width: '600px'
    });

    dialogRef.afterClosed().subscribe(res => {
      if (res?.action === 'edit') {
        this.openEditDialog(res.task);
      } else if (res?.action === 'delete') {
        this.openDeleteDialog(res.task);
      }
    });
  }

  openEditDialog(task: TaskResponse): void {
    const dialogRef = this.dialog.open(TaskDialogComponent, {
      data: { mode: 'edit', task },
      width: '560px'
    });

    dialogRef.afterClosed().subscribe((req: UpdateTaskRequest) => {
      if (req) {
        this.taskService.updateTask(task.id, req).subscribe({
          next: () => {
            this.notification.showSuccess('Task updated successfully');
            this.loadTasks();
          }
        });
      }
    });
  }

  openDeleteDialog(task: TaskResponse): void {
    const dialogRef = this.dialog.open(TaskDeleteDialogComponent, {
      data: { task },
      width: '450px'
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.taskService.deleteTask(task.id).subscribe({
          next: () => {
            this.notification.showSuccess('Task deleted successfully');
            this.loadTasks();
          }
        });
      }
    });
  }
}
