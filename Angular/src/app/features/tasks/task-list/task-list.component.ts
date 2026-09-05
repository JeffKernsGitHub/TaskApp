import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { CreateTaskRequest, TaskFilterParams, TaskPriority, TaskResponse, TaskStatus, UpdateTaskRequest } from '../../../core/models/task.models';
import { TaskService } from '../../../core/services/task.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { TaskDialogComponent } from '../task-dialog/task-dialog.component';
import { TaskDetailDialogComponent } from '../task-detail-dialog/task-detail-dialog.component';
import { TaskDeleteDialogComponent } from '../task-delete-dialog/task-delete-dialog.component';

/**
 * ============================================================================
 * Paginated Task Table Component (`task-list.component.ts`)
 * ============================================================================
 * Displays tasks in a data table integrated with Spring Data JPA pagination
 * and server-side sorting (`mat-table`, `mat-sort`, `mat-paginator`).
 *
 * KEY CONCEPTS DEMONSTRATED:
 * 1. Material Data Table (`MatTableDataSource` & `mat-table`):
 *    - Column definitions (`displayedColumns: string[]`).
 *    - Row action menus (`mat-menu`).
 *
 * 2. Server-Side Pagination & Sorting (`PageEvent`, `Sort`):
 *    - Intercepts pagination clicks `(page)="onPageChange($event)"` and sorting clicks
 *      `(matSortChange)="onSortChange($event)"`.
 *    - Formats sort parameters according to Spring Data conventions (`sort=title,asc` or `sort=createdAt,desc`).
 *
 * 3. Reactive Signal State (`signal<boolean>`, `signal<number>`):
 *    - Drives loading bars and total page counts cleanly.
 */
@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatProgressBarModule,
    MatTooltipModule
  ],
  template: `
    <div class="list-page">
      <div class="page-header">
        <div>
          <h1 class="page-title">Task Directory</h1>
          <p class="page-subtitle">Paginated, sortable table backed by Spring Data JPA</p>
        </div>
        <button mat-flat-button color="primary" class="create-btn" (click)="openCreateDialog()">
          <mat-icon>add</mat-icon>
          <span>Create Task</span>
        </button>
      </div>

      <!-- Filter Bar -->
      <div class="filter-card">
        <mat-form-field appearance="outline" class="search-input" subscriptSizing="dynamic">
          <mat-icon matPrefix>search</mat-icon>
          <input matInput [(ngModel)]="searchQuery" (ngModelChange)="onSearchChange()" placeholder="Search title or description...">
          @if (searchQuery) {
            <button mat-icon-button matSuffix (click)="searchQuery = ''; onSearchChange()">
              <mat-icon>clear</mat-icon>
            </button>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="filter-select" subscriptSizing="dynamic">
          <mat-label>Status</mat-label>
          <mat-select [(ngModel)]="selectedStatus" (ngModelChange)="onFilterChange()">
            <mat-option value="ALL">All Statuses</mat-option>
            <mat-option value="TODO">To Do</mat-option>
            <mat-option value="IN_PROGRESS">In Progress</mat-option>
            <mat-option value="DONE">Done</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="filter-select" subscriptSizing="dynamic">
          <mat-label>Priority</mat-label>
          <mat-select [(ngModel)]="selectedPriority" (ngModelChange)="onFilterChange()">
            <mat-option value="ALL">All Priorities</mat-option>
            <mat-option value="LOW">Low</mat-option>
            <mat-option value="MEDIUM">Medium</mat-option>
            <mat-option value="HIGH">High</mat-option>
          </mat-select>
        </mat-form-field>
      </div>

      @if (isLoading()) {
        <mat-progress-bar mode="indeterminate" class="loading-bar"></mat-progress-bar>
      }

      <!-- Table Container -->
      <div class="table-container">
        <table mat-table [dataSource]="dataSource" matSort (matSortChange)="onSortChange($event)" class="tasks-table">
          <!-- ID Column -->
          <ng-container matColumnDef="id">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>ID</th>
            <td mat-cell *matCellDef="let row" class="id-cell">#{{ row.id }}</td>
          </ng-container>

          <!-- Title Column -->
          <ng-container matColumnDef="title">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Title</th>
            <td mat-cell *matCellDef="let row" class="title-cell">
              <span class="task-title-link" (click)="openDetailDialog(row)">{{ row.title }}</span>
              @if (row.description) {
                <span class="task-desc-preview">{{ row.description }}</span>
              }
            </td>
          </ng-container>

          <!-- Status Column -->
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Status</th>
            <td mat-cell *matCellDef="let row">
              <span class="status-badge" [ngClass]="row.status">{{ formatStatus(row.status) }}</span>
            </td>
          </ng-container>

          <!-- Priority Column -->
          <ng-container matColumnDef="priority">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Priority</th>
            <td mat-cell *matCellDef="let row">
              <span class="priority-badge" [ngClass]="row.priority">{{ row.priority }}</span>
            </td>
          </ng-container>

          <!-- Due Date Column -->
          <ng-container matColumnDef="dueDate">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Due Date</th>
            <td mat-cell *matCellDef="let row">
              {{ row.dueDate ? (row.dueDate | date:'mediumDate') : '—' }}
            </td>
          </ng-container>

          <!-- Assignee Column -->
          <ng-container matColumnDef="assignedUser">
            <th mat-header-cell *matHeaderCellDef>Assignee</th>
            <td mat-cell *matCellDef="let row">
              @if (row.assignedUser) {
                <div class="assignee-item">
                  <div class="avatar">{{ row.assignedUser.username.charAt(0).toUpperCase() }}</div>
                  <span>{{ row.assignedUser.username }}</span>
                </div>
              } @else {
                <span class="unassigned-text">Unassigned</span>
              }
            </td>
          </ng-container>

          <!-- Actions Column -->
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef class="actions-header">Actions</th>
            <td mat-cell *matCellDef="let row" class="actions-cell">
              <button mat-icon-button [matMenuTriggerFor]="rowMenu" aria-label="Row actions">
                <mat-icon>more_vert</mat-icon>
              </button>
              <mat-menu #rowMenu="matMenu" xPosition="before">
                <button mat-menu-item (click)="openDetailDialog(row)">
                  <mat-icon>visibility</mat-icon>
                  <span>View Details</span>
                </button>
                <button mat-menu-item (click)="openEditDialog(row)">
                  <mat-icon>edit</mat-icon>
                  <span>Edit Task</span>
                </button>
                <button mat-menu-item (click)="openDeleteDialog(row)">
                  <mat-icon color="warn">delete</mat-icon>
                  <span class="delete-menu-text">Delete</span>
                </button>
              </mat-menu>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;" class="table-row"></tr>

          <tr class="mat-row" *matNoDataRow>
            <td class="mat-cell empty-table-cell" [attr.colspan]="displayedColumns.length">
              <mat-icon>search_off</mat-icon>
              <span>No matching tasks found.</span>
            </td>
          </tr>
        </table>

        <mat-paginator
          [length]="totalElements()"
          [pageSize]="pageSize()"
          [pageSizeOptions]="[5, 10, 25, 50]"
          [pageIndex]="pageIndex()"
          (page)="onPageChange($event)"
          aria-label="Select task page"
          class="table-paginator"
        >
        </mat-paginator>
      </div>
    </div>
  `,
  styles: [`
    .list-page {
      display: flex;
      flex-direction: column;
      gap: 1.25rem;
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

    .filter-select {
      width: 170px;
    }

    .loading-bar {
      border-radius: 4px;
    }

    .table-container {
      background: var(--card-bg);
      border: 1px solid var(--border-color);
      border-radius: 12px;
      overflow: hidden;
      box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.03);
    }

    .tasks-table {
      width: 100%;
      background: transparent;
    }

    .tasks-table th {
      font-weight: 700;
      color: var(--text-muted);
      font-size: 0.75rem;
      letter-spacing: 0.05em;
      text-transform: uppercase;
      border-bottom: 1px solid var(--border-color);
      background: var(--surface-bg);
    }

    .tasks-table td {
      border-bottom: 1px solid var(--border-color);
      color: var(--text-main);
      font-size: 0.875rem;
    }

    .table-row:hover {
      background: rgba(2, 132, 199, 0.03);
    }

    .id-cell {
      font-family: 'JetBrains Mono', monospace;
      font-weight: 600;
      color: var(--text-muted);
      width: 70px;
    }

    .title-cell {
      max-width: 320px;
    }

    .task-title-link {
      font-weight: 600;
      color: var(--text-main);
      cursor: pointer;
      display: block;
    }

    .task-title-link:hover {
      color: #0284c7;
      text-decoration: underline;
    }

    .task-desc-preview {
      display: block;
      font-size: 0.75rem;
      color: var(--text-muted);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      margin-top: 2px;
    }

    .assignee-item {
      display: flex;
      align-items: center;
      gap: 0.4rem;
      font-weight: 500;
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
      font-size: 0.7rem;
      font-weight: 700;
    }

    .unassigned-text {
      color: var(--text-muted);
      font-style: italic;
    }

    .actions-header, .actions-cell {
      width: 60px;
      text-align: right;
    }

    .delete-menu-text {
      color: #b91c1c;
    }

    .empty-table-cell {
      padding: 3rem !important;
      text-align: center;
      color: var(--text-muted);
    }

    .empty-table-cell mat-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
      margin-bottom: 0.5rem;
      display: block;
      margin-inline: auto;
      opacity: 0.5;
    }

    .table-paginator {
      background: var(--card-bg);
      border-top: 1px solid var(--border-color);
    }
  `]
})
export class TaskListComponent implements OnInit {
  private taskService = inject(TaskService);
  private authService = inject(AuthService);
  private dialog = inject(MatDialog);
  private notification = inject(NotificationService);

  displayedColumns: string[] = ['id', 'title', 'status', 'priority', 'dueDate', 'assignedUser', 'actions'];
  dataSource = new MatTableDataSource<TaskResponse>([]);

  isLoading = signal<boolean>(false);
  totalElements = signal<number>(0);
  pageSize = signal<number>(10);
  pageIndex = signal<number>(0);

  searchQuery = '';
  selectedStatus: TaskStatus | 'ALL' = 'ALL';
  selectedPriority: TaskPriority | 'ALL' = 'ALL';
  currentSort = 'createdAt,desc';

  ngOnInit(): void {
    this.loadTasks();
  }

  formatStatus(status: string): string {
    return status.replace('_', ' ');
  }

  loadTasks(): void {
    this.isLoading.set(true);
    const filter: TaskFilterParams = {
      page: this.pageIndex(),
      size: this.pageSize(),
      status: this.selectedStatus,
      priority: this.selectedPriority,
      search: this.searchQuery,
      sort: this.currentSort
    };

    this.taskService.getTasks(filter).subscribe({
      next: (page) => {
        this.dataSource.data = page.content;
        this.totalElements.set(page.totalElements);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  onSearchChange(): void {
    this.pageIndex.set(0);
    this.loadTasks();
  }

  onFilterChange(): void {
    this.pageIndex.set(0);
    this.loadTasks();
  }

  onSortChange(sort: Sort): void {
    if (sort.direction) {
      this.currentSort = `${sort.active},${sort.direction}`;
    } else {
      this.currentSort = 'createdAt,desc';
    }
    this.loadTasks();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadTasks();
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(TaskDialogComponent, {
      data: { mode: 'create' },
      width: '560px'
    });

    dialogRef.afterClosed().subscribe((res: CreateTaskRequest) => {
      if (res) {
        this.taskService.createTask(res).subscribe({
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

    dialogRef.afterClosed().subscribe((res: UpdateTaskRequest) => {
      if (res) {
        this.taskService.updateTask(task.id, res).subscribe({
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
