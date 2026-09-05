import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { LayoutComponent } from './layout/layout/layout.component';

/**
 * ============================================================================
 * Application Route Definitions (`app.routes.ts`)
 * ============================================================================
 * Angular Router maps browser URL paths to standalone view components.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * 1. Lazy Loading via `loadComponent()`:
 *    - Instead of bundling all pages into the initial download, `loadComponent: () => import(...)`
 *      splits each page into an independent JavaScript chunk loaded only when visited.
 *
 * 2. Layout Shell Pattern (Nested Child Routes):
 *    - The `LayoutComponent` acts as a common wrapper containing the header, sidebar, and `<router-outlet>`.
 *    - Child routes render inside the shell's `<router-outlet>`.
 *
 * 3. Functional Route Guards (`canActivate: [authGuard, adminGuard]`):
 *    - Guards protect routes before navigation completes.
 *    - `authGuard`: Redirects unauthenticated users to `/login`.
 *    - `adminGuard`: Restricts administrative routes to users possessing the `ADMIN` role.
 *
 * 4. Wildcard Route (`**`):
 *    - Catches undefined URLs and redirects to the default landing page.
 */
export const routes: Routes = [
  // --- Public Authentication Routes ---
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent),
    title: 'Sign In — Task Manager'
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent),
    title: 'Register Account — Task Manager'
  },

  // --- Authenticated Layout Shell & Child Features ---
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard], // Protects all child routes in this branch
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'tasks/board'
      },
      {
        path: 'tasks',
        redirectTo: 'tasks/board',
        pathMatch: 'full'
      },
      {
        path: 'tasks/board',
        loadComponent: () => import('./features/tasks/task-board/task-board.component').then(m => m.TaskBoardComponent),
        title: 'Kanban Board — Task Manager'
      },
      {
        path: 'tasks/list',
        loadComponent: () => import('./features/tasks/task-list/task-list.component').then(m => m.TaskListComponent),
        title: 'Task Directory — Task Manager'
      },
      {
        path: 'tasks/dashboard',
        loadComponent: () => import('./features/tasks/task-dashboard/task-dashboard.component').then(m => m.TaskDashboardComponent),
        title: 'Analytics Dashboard — Task Manager'
      },
      {
        path: 'admin/users',
        canActivate: [adminGuard], // Restricts access to users with ADMIN role
        loadComponent: () => import('./features/admin/user-list/user-list.component').then(m => m.UserListComponent),
        title: 'User Administration — Task Manager'
      },
      {
        path: 'system',
        loadComponent: () => import('./features/system/system-status/system-status.component').then(m => m.SystemStatusComponent),
        title: 'Spring Actuator & Health — Task Manager'
      }
    ]
  },

  // --- Wildcard Fallback Route ---
  {
    path: '**',
    redirectTo: 'tasks/board'
  }
];
