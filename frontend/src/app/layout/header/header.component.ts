import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../core/services/auth.service';
import { ThemeService } from '../../core/services/theme.service';

/**
 * ============================================================================
 * Top Navigation Header Component (`header.component.ts`)
 * ============================================================================
 * App toolbar displaying system identity, mock/live backend switcher,
 * theme toggle, and authenticated user profile controls.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * 1. `@Output()` Event Emitters:
 *    - `toggleSidebar = new EventEmitter<void>()` notifies the parent `LayoutComponent`
 *      to toggle the sidenav drawer.
 *
 * 2. Signal Data Binding:
 *    - Reads `authService.currentUser()`, `authService.isMockMode()`, and `themeService.isDarkMode()`
 *      directly in template expressions.
 *
 * 3. Material Menus (`mat-menu`):
 *    - Contextual dropdown menus for quick switching between Live Spring API and In-Memory Mock mode.
 */
@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDividerModule,
    MatSlideToggleModule,
    MatTooltipModule
  ],
  template: `
    <mat-toolbar class="app-toolbar">
      <div class="toolbar-left">
        <button mat-icon-button (click)="toggleSidebar.emit()" aria-label="Toggle navigation">
          <mat-icon>menu</mat-icon>
        </button>
        <a routerLink="/" class="brand-link">
          <mat-icon class="brand-icon">task_alt</mat-icon>
          <span class="brand-title">TaskManager</span>
          <span class="brand-badge">Spring & Angular</span>
        </a>
      </div>

      <div class="toolbar-right">
        <!-- Backend Mode Selector (Live Spring vs Mock Sandbox) -->
        <button mat-stroked-button [matMenuTriggerFor]="modeMenu" class="mode-btn" [class.live-mode]="!authService.isMockMode()">
          <span class="status-dot" [class.live]="!authService.isMockMode()"></span>
          <span>{{ authService.isMockMode() ? 'Mock Sandbox' : 'Live Spring Boot' }}</span>
          <mat-icon iconPositionEnd>arrow_drop_down</mat-icon>
        </button>
        <mat-menu #modeMenu="matMenu">
          <button mat-menu-item (click)="authService.setMockMode(false)">
            <mat-icon color="primary">dns</mat-icon>
            <span>Connect Live Backend (localhost:8080)</span>
          </button>
          <button mat-menu-item (click)="authService.setMockMode(true)">
            <mat-icon>science</mat-icon>
            <span>In-Memory Sandbox (Offline Mode)</span>
          </button>
        </mat-menu>

        <!-- Dark/Light Theme Toggle -->
        <button mat-icon-button (click)="themeService.toggleTheme()" [matTooltip]="themeService.isDarkMode() ? 'Switch to Light Mode' : 'Switch to Dark Mode'">
          <mat-icon>{{ themeService.isDarkMode() ? 'light_mode' : 'dark_mode' }}</mat-icon>
        </button>

        <!-- User Profile Dropdown -->
        @if (authService.currentUser(); as user) {
          <button mat-button [matMenuTriggerFor]="userMenu" class="user-menu-btn">
            <div class="avatar">{{ user.username.charAt(0).toUpperCase() }}</div>
            <div class="user-details-header">
              <span class="username-header">{{ user.username }}</span>
              <span class="role-chip" [ngClass]="user.role">{{ user.role }}</span>
            </div>
            <mat-icon>arrow_drop_down</mat-icon>
          </button>
          <mat-menu #userMenu="matMenu" xPosition="before">
            <div class="user-menu-info">
              <strong>{{ user.username }}</strong>
              <small>{{ user.email }}</small>
            </div>
            <mat-divider></mat-divider>
            <button mat-menu-item routerLink="/tasks/dashboard">
              <mat-icon>dashboard</mat-icon>
              <span>Dashboard</span>
            </button>
            @if (authService.isAdmin()) {
              <button mat-menu-item routerLink="/admin/users">
                <mat-icon>manage_accounts</mat-icon>
                <span>Admin Users</span>
              </button>
            }
            <button mat-menu-item routerLink="/system">
              <mat-icon>monitor_heart</mat-icon>
              <span>Actuator Health</span>
            </button>
            <mat-divider></mat-divider>
            <button mat-menu-item (click)="authService.logout()" class="logout-item">
              <mat-icon color="warn">logout</mat-icon>
              <span>Sign Out</span>
            </button>
          </mat-menu>
        }
      </div>
    </mat-toolbar>
  `,
  styles: [`
    .app-toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: var(--surface-bg);
      border-bottom: 1px solid var(--border-color);
      height: 64px;
      padding: 0 1rem;
      z-index: 1000;
    }

    .toolbar-left, .toolbar-right {
      display: flex;
      align-items: center;
      gap: 0.75rem;
    }

    .brand-link {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      text-decoration: none;
      color: var(--text-main);
    }

    .brand-icon {
      color: #0284c7;
      font-size: 28px;
      width: 28px;
      height: 28px;
    }

    .brand-title {
      font-size: 1.25rem;
      font-weight: 800;
      letter-spacing: -0.02em;
    }

    .brand-badge {
      font-size: 0.6875rem;
      background: rgba(2, 132, 199, 0.1);
      color: #0284c7;
      padding: 0.15rem 0.5rem;
      border-radius: 9999px;
      font-weight: 600;
      letter-spacing: 0.03em;
    }

    .mode-btn {
      height: 36px;
      border-radius: 20px;
      font-size: 0.8125rem;
      font-weight: 600;
      border: 1px solid var(--border-color);
    }

    .mode-btn.live-mode {
      border-color: #10b981;
      color: #059669;
      background: rgba(16, 185, 129, 0.08);
    }

    .status-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #f59e0b;
      display: inline-block;
      margin-right: 6px;
    }

    .status-dot.live {
      background: #10b981;
      box-shadow: 0 0 8px rgba(16, 185, 129, 0.6);
    }

    .user-menu-btn {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.25rem 0.5rem;
      border-radius: 20px;
    }

    .avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: #0284c7;
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
      font-size: 0.875rem;
    }

    .user-details-header {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      line-height: 1.2;
    }

    .username-header {
      font-size: 0.875rem;
      font-weight: 600;
      color: var(--text-main);
    }

    .role-chip {
      font-size: 0.625rem;
      font-weight: 700;
      padding: 0.05rem 0.35rem;
      border-radius: 4px;
      letter-spacing: 0.04em;
    }

    .user-menu-info {
      padding: 0.75rem 1rem;
      display: flex;
      flex-direction: column;
    }

    .user-menu-info strong {
      color: var(--text-main);
    }

    .user-menu-info small {
      color: var(--text-muted);
    }

    .logout-item {
      color: #b91c1c;
    }
  `]
})
export class HeaderComponent {
  @Output() toggleSidebar = new EventEmitter<void>();

  authService = inject(AuthService);
  themeService = inject(ThemeService);
}
