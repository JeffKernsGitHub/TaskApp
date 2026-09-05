import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatProgressBarModule
  ],
  template: `
    <div class="login-page">
      <div class="login-container">
        <div class="login-brand">
          <mat-icon class="brand-logo">task_alt</mat-icon>
          <h1 class="brand-title">TaskFlow</h1>
          <p class="brand-subtitle">Spring Boot 3.4 / 4.x & Angular Material</p>
        </div>

        <mat-card class="login-card">
          @if (isLoading()) {
            <mat-progress-bar mode="indeterminate"></mat-progress-bar>
          }
          <mat-card-header>
            <mat-card-title>Sign In</mat-card-title>
            <mat-card-subtitle>Enter your credentials to access your tasks</mat-card-subtitle>
          </mat-card-header>

          <mat-card-content>
            <form [formGroup]="loginForm" (ngSubmit)="onSubmit()" class="login-form">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Username</mat-label>
                <input matInput formControlName="username" placeholder="e.g. alice or admin" autocomplete="username">
                <mat-icon matPrefix>person</mat-icon>
                @if (loginForm.get('username')?.hasError('required') && loginForm.get('username')?.touched) {
                  <mat-error>Username is required</mat-error>
                }
              </mat-form-field>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Password</mat-label>
                <input matInput [type]="hidePassword() ? 'password' : 'text'" formControlName="password" placeholder="••••••••" autocomplete="current-password">
                <mat-icon matPrefix>lock</mat-icon>
                <button mat-icon-button matSuffix (click)="hidePassword.set(!hidePassword())" [attr.aria-label]="'Hide password'" [attr.aria-pressed]="hidePassword()" type="button">
                  <mat-icon>{{ hidePassword() ? 'visibility_off' : 'visibility' }}</mat-icon>
                </button>
                @if (loginForm.get('password')?.hasError('required') && loginForm.get('password')?.touched) {
                  <mat-error>Password is required</mat-error>
                }
              </mat-form-field>

              <button mat-flat-button color="primary" class="submit-btn" type="submit" [disabled]="loginForm.invalid || isLoading()">
                <mat-icon>login</mat-icon>
                <span>Sign In</span>
              </button>
            </form>

            <div class="demo-section">
              <div class="demo-header">
                <mat-divider></mat-divider>
                <span class="demo-badge">QUICK DEMO ACCESS</span>
                <mat-divider></mat-divider>
              </div>

              <div class="demo-buttons">
                <button mat-stroked-button class="demo-btn admin" (click)="quickLoginAdmin()" type="button">
                  <mat-icon>admin_panel_settings</mat-icon>
                  <span>Admin User</span>
                </button>
                <button mat-stroked-button class="demo-btn user" (click)="quickLoginUser()" type="button">
                  <mat-icon>person</mat-icon>
                  <span>Standard User</span>
                </button>
              </div>
            </div>
          </mat-card-content>

          <mat-card-actions class="card-footer">
            <span class="register-hint">Don't have an account?</span>
            <a mat-button color="primary" routerLink="/register">Register Here</a>
          </mat-card-actions>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .login-page {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: radial-gradient(circle at 50% 20%, rgba(2, 132, 199, 0.08) 0%, transparent 60%), var(--surface-bg);
      padding: 1.5rem;
    }

    .login-container {
      width: 100%;
      max-width: 440px;
      display: flex;
      flex-direction: column;
      align-items: center;
    }

    .login-brand {
      text-align: center;
      margin-bottom: 1.75rem;
    }

    .brand-logo {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: #0284c7;
      margin-bottom: 0.5rem;
    }

    .brand-title {
      font-size: 2rem;
      font-weight: 800;
      letter-spacing: -0.03em;
      margin: 0;
      color: var(--text-main);
    }

    .brand-subtitle {
      font-size: 0.875rem;
      color: var(--text-muted);
      margin: 0.25rem 0 0 0;
    }

    .login-card {
      width: 100%;
      border-radius: 16px;
      background: var(--card-bg);
      border: 1px solid var(--border-color);
      box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.08);
      overflow: hidden;
    }

    mat-card-header {
      padding: 1.5rem 1.5rem 0.5rem 1.5rem;
    }

    mat-card-title {
      font-size: 1.35rem;
      font-weight: 700;
      color: var(--text-main);
    }

    mat-card-subtitle {
      color: var(--text-muted);
      margin-top: 4px;
    }

    mat-card-content {
      padding: 1.5rem;
    }

    .login-form {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .full-width {
      width: 100%;
    }

    .submit-btn {
      height: 48px;
      font-weight: 600;
      font-size: 1rem;
      border-radius: 8px;
      margin-top: 0.5rem;
      background-color: #0284c7;
      color: #ffffff;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
    }

    .demo-section {
      margin-top: 1.75rem;
    }

    .demo-header {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      margin-bottom: 1rem;
    }

    .demo-header mat-divider {
      flex: 1;
    }

    .demo-badge {
      font-size: 0.65rem;
      font-weight: 700;
      color: var(--text-muted);
      letter-spacing: 0.08em;
      white-space: nowrap;
    }

    .demo-buttons {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 0.75rem;
    }

    .demo-btn {
      border-radius: 8px;
      height: 40px;
      font-size: 0.8rem;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.4rem;
    }

    .demo-btn.admin {
      color: #7c3aed;
      border-color: #ddd6fe;
    }

    .demo-btn.user {
      color: #0284c7;
      border-color: #bae6fd;
    }

    .card-footer {
      display: flex;
      justify-content: center;
      align-items: center;
      padding: 1rem 1.5rem 1.5rem 1.5rem;
      border-top: 1px solid var(--border-color);
      background: rgba(0, 0, 0, 0.01);
    }

    .register-hint {
      font-size: 0.875rem;
      color: var(--text-muted);
    }
  `]
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private notification = inject(NotificationService);

  loginForm: FormGroup = this.fb.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]]
  });

  hidePassword = signal<boolean>(true);
  isLoading = signal<boolean>(false);

  onSubmit(): void {
    if (this.loginForm.invalid) return;

    this.isLoading.set(true);
    const { username, password } = this.loginForm.value;

    this.authService.login({ username, password }).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.notification.showSuccess(`Welcome back, ${username}!`);
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/tasks';
        this.router.navigateByUrl(returnUrl);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  quickLoginAdmin(): void {
    this.authService.quickLoginAsAdmin();
  }

  quickLoginUser(): void {
    this.authService.quickLoginAsUser();
  }
}
