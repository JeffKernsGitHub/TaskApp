import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatBadgeModule } from '@angular/material/badge';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    RouterLinkActive,
    MatListModule,
    MatIconModule,
    MatDividerModule,
    MatBadgeModule
  ],
  template: `
    <div class="sidebar-container">
      <div class="nav-section-title">TASKS & WORKFLOW</div>
      <mat-nav-list class="nav-list">
        <a mat-list-item routerLink="/tasks/board" routerLinkActive="active-nav-item">
          <mat-icon matListItemIcon>view_kanban</mat-icon>
          <span matListItemTitle>Kanban Board</span>
        </a>
        <a mat-list-item routerLink="/tasks/list" routerLinkActive="active-nav-item">
          <mat-icon matListItemIcon>table_rows</mat-icon>
          <span matListItemTitle>Task Table</span>
        </a>
        <a mat-list-item routerLink="/tasks/dashboard" routerLinkActive="active-nav-item">
          <mat-icon matListItemIcon>analytics</mat-icon>
          <span matListItemTitle>Task Metrics</span>
        </a>
      </mat-nav-list>

      @if (authService.isAdmin()) {
        <mat-divider class="nav-divider"></mat-divider>
        <div class="nav-section-title">ADMINISTRATION</div>
        <mat-nav-list class="nav-list">
          <a mat-list-item routerLink="/admin/users" routerLinkActive="active-nav-item">
            <mat-icon matListItemIcon>group</mat-icon>
            <span matListItemTitle>User Management</span>
          </a>
        </mat-nav-list>
      }

      <mat-divider class="nav-divider"></mat-divider>
      <div class="nav-section-title">SPRING BACKEND & OPS</div>
      <mat-nav-list class="nav-list">
        <a mat-list-item routerLink="/system" routerLinkActive="active-nav-item">
          <mat-icon matListItemIcon>monitor_heart</mat-icon>
          <span matListItemTitle>Actuator & Health</span>
        </a>
      </mat-nav-list>

      <div class="sidebar-footer">
        <div class="backend-spec">
          <span class="spec-label">Spring Boot 3.4 / 4.x</span>
          <span class="spec-sub">PostgreSQL 17 • JWT Auth</span>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .sidebar-container {
      display: flex;
      flex-direction: column;
      height: 100%;
      background: var(--card-bg);
      border-right: 1px solid var(--border-color);
      padding: 1rem 0.5rem;
    }

    .nav-section-title {
      font-size: 0.65rem;
      font-weight: 700;
      color: var(--text-muted);
      letter-spacing: 0.08em;
      padding: 0.75rem 1rem 0.25rem 1rem;
    }

    .nav-list {
      padding: 0;
    }

    .nav-list a {
      border-radius: 8px;
      margin: 2px 4px;
      color: var(--text-main);
      transition: all 0.2s ease;
    }

    .nav-list a:hover {
      background: rgba(2, 132, 199, 0.06);
    }

    .active-nav-item {
      background: rgba(2, 132, 199, 0.12) !important;
      color: #0284c7 !important;
      font-weight: 600;
    }

    .active-nav-item mat-icon {
      color: #0284c7;
    }

    .nav-divider {
      margin: 0.75rem 0.5rem;
    }

    .sidebar-footer {
      margin-top: auto;
      padding: 0.75rem;
      background: var(--surface-bg);
      border-radius: 8px;
      border: 1px solid var(--border-color);
      margin-left: 0.5rem;
      margin-right: 0.5rem;
    }

    .backend-spec {
      display: flex;
      flex-direction: column;
      font-size: 0.75rem;
    }

    .spec-label {
      font-weight: 600;
      color: var(--text-main);
    }

    .spec-sub {
      color: var(--text-muted);
      font-size: 0.7rem;
    }
  `]
})
export class SidebarComponent {
  authService = inject(AuthService);
}
