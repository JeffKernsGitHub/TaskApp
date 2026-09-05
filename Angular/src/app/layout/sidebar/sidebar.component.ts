import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTooltipModule } from '@angular/material/tooltip';
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
    MatBadgeModule,
    MatTooltipModule
  ],
  template: `
    <div class="sidebar-container">
      <div class="nav-section-title">TASKS & WORKFLOW</div>
      <mat-nav-list class="nav-list">
        <a mat-list-item routerLink="/tasks/board" routerLinkActive="active-nav-item" matTooltip="Interactive drag-and-drop workflow board" matTooltipPosition="right">
          <div matListItemIcon class="nav-icon-wrapper">
            <mat-icon>view_kanban</mat-icon>
          </div>
          <span matListItemTitle class="nav-item-title">Kanban Board</span>
          <span matListItemLine class="nav-item-desc">Visual drag-and-drop workflow</span>
        </a>

        <a mat-list-item routerLink="/tasks/list" routerLinkActive="active-nav-item" matTooltip="Detailed table with search, filter, and pagination" matTooltipPosition="right">
          <div matListItemIcon class="nav-icon-wrapper">
            <mat-icon>table_rows</mat-icon>
          </div>
          <span matListItemTitle class="nav-item-title">Task Table</span>
          <span matListItemLine class="nav-item-desc">Search, filter & bulk actions</span>
        </a>

        <a mat-list-item routerLink="/tasks/dashboard" routerLinkActive="active-nav-item" matTooltip="High-level statistics and distribution charts" matTooltipPosition="right">
          <div matListItemIcon class="nav-icon-wrapper">
            <mat-icon>analytics</mat-icon>
          </div>
          <span matListItemTitle class="nav-item-title">Task Metrics</span>
          <span matListItemLine class="nav-item-desc">Workload analytics & charts</span>
        </a>
      </mat-nav-list>

      @if (authService.isAdmin()) {
        <mat-divider class="nav-divider"></mat-divider>
        <div class="nav-section-title">ADMINISTRATION</div>
        <mat-nav-list class="nav-list">
          <a mat-list-item routerLink="/admin/users" routerLinkActive="active-nav-item" matTooltip="Manage user accounts and permissions" matTooltipPosition="right">
            <div matListItemIcon class="nav-icon-wrapper admin-icon">
              <mat-icon>group</mat-icon>
            </div>
            <span matListItemTitle class="nav-item-title">User Management</span>
            <span matListItemLine class="nav-item-desc">Manage accounts & roles</span>
          </a>
        </mat-nav-list>
      }

      <mat-divider class="nav-divider"></mat-divider>
      <div class="nav-section-title">SPRING BACKEND & OPS</div>
      <mat-nav-list class="nav-list">
        <a mat-list-item routerLink="/system" routerLinkActive="active-nav-item" matTooltip="Live Spring Boot Actuator monitoring" matTooltipPosition="right">
          <div matListItemIcon class="nav-icon-wrapper system-icon">
            <mat-icon>monitor_heart</mat-icon>
          </div>
          <span matListItemTitle class="nav-item-title">Actuator & Health</span>
          <span matListItemLine class="nav-item-desc">JVM memory, DB & endpoints</span>
        </a>
      </mat-nav-list>

      <div class="sidebar-footer">
        <div class="backend-spec">
          <div class="spec-header">
            <span class="status-indicator"></span>
            <span class="spec-label">Spring Boot 3.4 / 4.x</span>
          </div>
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
      box-sizing: border-box;
    }

    .nav-section-title {
      font-size: 0.65rem;
      font-weight: 700;
      color: var(--text-muted);
      letter-spacing: 0.08em;
      padding: 0.75rem 0.75rem 0.35rem 0.75rem;
      text-transform: uppercase;
    }

    .nav-list {
      padding: 0;
    }

    .nav-list a {
      border-radius: 10px;
      margin: 3px 4px;
      padding: 6px 8px;
      color: var(--text-main);
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
      height: auto !important;
      min-height: 52px;
    }

    .nav-icon-wrapper {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 34px;
      height: 34px;
      border-radius: 8px;
      background: rgba(2, 132, 199, 0.08);
      color: #0284c7;
      margin-right: 10px;
      transition: all 0.2s ease;
    }

    .nav-icon-wrapper mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .nav-icon-wrapper.admin-icon {
      background: rgba(126, 34, 206, 0.08);
      color: #7e22ce;
    }

    .nav-icon-wrapper.system-icon {
      background: rgba(16, 185, 129, 0.08);
      color: #059669;
    }

    .nav-item-title {
      font-size: 0.875rem !important;
      font-weight: 600 !important;
      color: var(--text-main) !important;
      line-height: 1.25 !important;
    }

    .nav-item-desc {
      font-size: 0.725rem !important;
      color: var(--text-muted) !important;
      line-height: 1.2 !important;
    }

    .nav-list a:hover {
      background: rgba(2, 132, 199, 0.06);
    }

    .nav-list a:hover .nav-icon-wrapper {
      transform: scale(1.05);
    }

    .active-nav-item {
      background: rgba(2, 132, 199, 0.12) !important;
      border-left: 3px solid #0284c7;
    }

    .active-nav-item .nav-item-title {
      color: #0284c7 !important;
      font-weight: 700 !important;
    }

    .active-nav-item .nav-icon-wrapper {
      background: #0284c7;
      color: #ffffff;
      box-shadow: 0 2px 6px rgba(2, 132, 199, 0.3);
    }

    .nav-divider {
      margin: 0.6rem 0.5rem;
      border-color: var(--border-color);
    }

    .sidebar-footer {
      margin-top: auto;
      padding: 0.75rem 0.875rem;
      background: var(--surface-bg);
      border-radius: 8px;
      border: 1px solid var(--border-color);
      margin-left: 0.25rem;
      margin-right: 0.25rem;
    }

    .backend-spec {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .spec-header {
      display: flex;
      align-items: center;
      gap: 6px;
    }

    .status-indicator {
      width: 7px;
      height: 7px;
      border-radius: 50%;
      background: #10b981;
      display: inline-block;
      box-shadow: 0 0 6px rgba(16, 185, 129, 0.6);
    }

    .spec-label {
      font-weight: 600;
      color: var(--text-main);
      font-size: 0.75rem;
    }

    .spec-sub {
      color: var(--text-muted);
      font-size: 0.7rem;
      padding-left: 13px;
    }
  `]
})
export class SidebarComponent {
  authService = inject(AuthService);
}
