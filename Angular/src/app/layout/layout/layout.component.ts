import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { HeaderComponent } from '../header/header.component';
import { SidebarComponent } from '../sidebar/sidebar.component';

/**
 * ============================================================================
 * Layout Shell Component (`layout.component.ts`)
 * ============================================================================
 * Acts as the master application frame for authenticated routes.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * 1. Material Sidenav (`mat-sidenav-container`, `mat-sidenav`, `mat-sidenav-content`):
 *    - Provides a responsive side navigation drawer and scrollable main content viewport.
 *
 * 2. Nested `<router-outlet>`:
 *    - Child route components (Kanban Board, List, Dashboard, Admin, System) are
 *      rendered dynamically inside `<main class="main-content"><router-outlet></router-outlet></main>`.
 */
@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    MatSidenavModule,
    HeaderComponent,
    SidebarComponent
  ],
  template: `
    <div class="layout-container">
      <app-header (toggleSidebar)="sidenav.toggle()"></app-header>
      <mat-sidenav-container class="sidenav-container">
        <mat-sidenav #sidenav mode="side" opened class="app-sidebar">
          <app-sidebar></app-sidebar>
        </mat-sidenav>
        <mat-sidenav-content class="content-container">
          <main class="main-content">
            <router-outlet></router-outlet>
          </main>
        </mat-sidenav-content>
      </mat-sidenav-container>
    </div>
  `,
  styles: [`
    .layout-container {
      display: flex;
      flex-direction: column;
      height: 100vh;
      overflow: hidden;
    }

    .sidenav-container {
      flex: 1;
      height: calc(100vh - 64px);
    }

    .app-sidebar {
      width: 275px;
      border-right: 1px solid var(--border-color);
      background: var(--surface-bg);
    }

    .content-container {
      background-color: var(--app-bg);
      overflow-y: auto;
    }

    .main-content {
      padding: 1.5rem;
      max-width: 1400px;
      margin: 0 auto;
    }
  `]
})
export class LayoutComponent {}
