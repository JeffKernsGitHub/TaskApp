import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { UserResponse, CreateUserRequest, UpdateUserRequest } from '../../../core/models/user.models';
import { UserService } from '../../../core/services/user.service';
import { NotificationService } from '../../../core/services/notification.service';
import { UserDialogComponent } from '../user-dialog/user-dialog.component';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressBarModule,
    MatTooltipModule
  ],
  template: `
    <div class="user-page">
      <div class="page-header">
        <div>
          <h1 class="page-title">User Directory</h1>
          <p class="page-subtitle">Administrative account control and RBAC management</p>
        </div>
        <button mat-flat-button color="primary" class="create-btn" (click)="openCreateDialog()">
          <mat-icon>person_add</mat-icon>
          <span>Provision User</span>
        </button>
      </div>

      @if (isLoading()) {
        <mat-progress-bar mode="indeterminate" class="loading-bar"></mat-progress-bar>
      }

      <div class="table-container">
        <table mat-table [dataSource]="users()" class="users-table">
          <!-- ID Column -->
          <ng-container matColumnDef="id">
            <th mat-header-cell *matHeaderCellDef>ID</th>
            <td mat-cell *matCellDef="let user" class="id-cell">#{{ user.id }}</td>
          </ng-container>

          <!-- Username Column -->
          <ng-container matColumnDef="username">
            <th mat-header-cell *matHeaderCellDef>Username</th>
            <td mat-cell *matCellDef="let user" class="username-cell">
              <div class="user-item">
                <div class="avatar">{{ user.username.charAt(0).toUpperCase() }}</div>
                <span class="user-name-text">{{ user.username }}</span>
              </div>
            </td>
          </ng-container>

          <!-- Email Column -->
          <ng-container matColumnDef="email">
            <th mat-header-cell *matHeaderCellDef>Email</th>
            <td mat-cell *matCellDef="let user">{{ user.email }}</td>
          </ng-container>

          <!-- Role Column -->
          <ng-container matColumnDef="role">
            <th mat-header-cell *matHeaderCellDef>Role</th>
            <td mat-cell *matCellDef="let user">
              <span class="role-badge" [ngClass]="user.role">{{ user.role }}</span>
            </td>
          </ng-container>

          <!-- Created At Column -->
          <ng-container matColumnDef="createdAt">
            <th mat-header-cell *matHeaderCellDef>Created</th>
            <td mat-cell *matCellDef="let user">{{ user.createdAt | date:'medium' }}</td>
          </ng-container>

          <!-- Actions Column -->
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef class="actions-header">Actions</th>
            <td mat-cell *matCellDef="let user" class="actions-cell">
              <button mat-icon-button (click)="openEditDialog(user)" matTooltip="Edit User">
                <mat-icon>edit</mat-icon>
              </button>
              <button mat-icon-button color="warn" (click)="deleteUser(user)" matTooltip="Delete User">
                <mat-icon>delete</mat-icon>
              </button>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;" class="table-row"></tr>
        </table>

        <mat-paginator
          [length]="totalElements()"
          [pageSize]="pageSize()"
          [pageSizeOptions]="[5, 10, 25]"
          [pageIndex]="pageIndex()"
          (page)="onPageChange($event)"
          class="table-paginator"
        >
        </mat-paginator>
      </div>
    </div>
  `,
  styles: [`
    .user-page {
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

    .users-table {
      width: 100%;
      background: transparent;
    }

    .users-table th {
      font-weight: 700;
      color: var(--text-muted);
      font-size: 0.75rem;
      letter-spacing: 0.05em;
      text-transform: uppercase;
      border-bottom: 1px solid var(--border-color);
      background: var(--surface-bg);
    }

    .users-table td {
      border-bottom: 1px solid var(--border-color);
      color: var(--text-main);
      font-size: 0.875rem;
    }

    .id-cell {
      font-family: 'JetBrains Mono', monospace;
      font-weight: 600;
      color: var(--text-muted);
      width: 80px;
    }

    .user-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .avatar {
      width: 28px;
      height: 28px;
      border-radius: 50%;
      background: linear-gradient(135deg, #7c3aed, #a855f7);
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.8rem;
      font-weight: 700;
    }

    .user-name-text {
      font-weight: 600;
      color: var(--text-main);
    }

    .actions-header, .actions-cell {
      width: 100px;
      text-align: right;
    }

    .table-paginator {
      background: var(--card-bg);
      border-top: 1px solid var(--border-color);
    }
  `]
})
export class UserListComponent implements OnInit {
  private userService = inject(UserService);
  private dialog = inject(MatDialog);
  private notification = inject(NotificationService);

  displayedColumns: string[] = ['id', 'username', 'email', 'role', 'createdAt', 'actions'];
  users = signal<UserResponse[]>([]);
  isLoading = signal<boolean>(false);
  totalElements = signal<number>(0);
  pageSize = signal<number>(10);
  pageIndex = signal<number>(0);

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading.set(true);
    this.userService.getUsers(this.pageIndex(), this.pageSize()).subscribe({
      next: (page) => {
        this.users.set(page.content);
        this.totalElements.set(page.totalElements);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadUsers();
  }

  openCreateDialog(): void {
    const ref = this.dialog.open(UserDialogComponent, {
      data: { mode: 'create' },
      width: '500px'
    });

    ref.afterClosed().subscribe((req: CreateUserRequest) => {
      if (req) {
        this.userService.createUser(req).subscribe({
          next: () => {
            this.notification.showSuccess('User provisioned successfully');
            this.loadUsers();
          }
        });
      }
    });
  }

  openEditDialog(user: UserResponse): void {
    const ref = this.dialog.open(UserDialogComponent, {
      data: { mode: 'edit', user },
      width: '500px'
    });

    ref.afterClosed().subscribe((req: UpdateUserRequest) => {
      if (req) {
        this.userService.updateUser(user.id, req).subscribe({
          next: () => {
            this.notification.showSuccess('User updated successfully');
            this.loadUsers();
          }
        });
      }
    });
  }

  deleteUser(user: UserResponse): void {
    if (confirm(`Are you sure you want to delete user "${user.username}"?`)) {
      this.userService.deleteUser(user.id).subscribe({
        next: () => {
          this.notification.showSuccess('User removed successfully');
          this.loadUsers();
        }
      });
    }
  }
}
