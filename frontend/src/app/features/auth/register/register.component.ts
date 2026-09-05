import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-register',
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
    MatProgressBarModule
  ],
  template: `
    <div class="register-page">
      <div class="register-container">
        <div class="register-brand">
          <mat-icon class="brand-logo">task_alt</mat-icon>
          <h1 class="brand-title">TaskFlow</h1>
          <p class="brand-subtitle">Create a new account</p>
        </div>

        <mat-card class="register-card">
          @if (isLoading()) {
            <mat-progress-bar mode="indeterminate"></mat-progress-bar>
          }
          <mat-card-header>
            <mat-card-title>Create Account</mat-card-title>
            <mat-card-subtitle>Join and organize your engineering tasks</mat-card-subtitle>
          </mat-card-header>

          <mat-card-content>
            <form [formGroup]="registerForm" (ngSubmit)="onSubmit()" class="register-form">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Username</mat-label>
                <input matInput formControlName="username" placeholder="e.g. john_doe" autocomplete="username">
                <mat-icon matPrefix>person</mat-icon>
                <mat-hint>3 to 30 characters</mat-hint>
                @if (registerForm.get('username')?.hasError('required') && registerForm.get('username')?.touched) {
                  <mat-error>Username is required</mat-error>
                }
                @if (registerForm.get('username')?.hasError('minlength')) {
                  <mat-error>Username must be at least 3 characters</mat-error>
                }
                @if (registerForm.get('username')?.hasError('maxlength')) {
                  <mat-error>Username cannot exceed 30 characters</mat-error>
                }
              </mat-form-field>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Email Address</mat-label>
                <input matInput formControlName="email" type="email" placeholder="john@example.com" autocomplete="email">
                <mat-icon matPrefix>email</mat-icon>
                @if (registerForm.get('email')?.hasError('required') && registerForm.get('email')?.touched) {
                  <mat-error>Email is required</mat-error>
                }
                @if (registerForm.get('email')?.hasError('email')) {
                  <mat-error>Please enter a valid email address</mat-error>
                }
              </mat-form-field>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Password</mat-label>
                <input matInput [type]="hidePassword() ? 'password' : 'text'" formControlName="password" placeholder="••••••••" autocomplete="new-password">
                <mat-icon matPrefix>lock</mat-icon>
                <button mat-icon-button matSuffix (click)="hidePassword.set(!hidePassword())" [attr.aria-label]="'Hide password'" [attr.aria-pressed]="hidePassword()" type="button">
                  <mat-icon>{{ hidePassword() ? 'visibility_off' : 'visibility' }}</mat-icon>
                </button>
                <mat-hint>8 to 64 characters</mat-hint>
                @if (registerForm.get('password')?.hasError('required') && registerForm.get('password')?.touched) {
                  <mat-error>Password is required</mat-error>
                }
                @if (registerForm.get('password')?.hasError('minlength')) {
                  <mat-error>Password must be at least 8 characters</mat-error>
                }
              </mat-form-field>

              <button mat-flat-button color="primary" class="submit-btn" type="submit" [disabled]="registerForm.invalid || isLoading()">
                <mat-icon>person_add</mat-icon>
                <span>Register</span>
              </button>
            </form>
          </mat-card-content>

          <mat-card-actions class="card-footer">
            <span class="login-hint">Already have an account?</span>
            <a mat-button color="primary" routerLink="/login">Sign In</a>
          </mat-card-actions>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .register-page {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: radial-gradient(circle at 50% 20%, rgba(2, 132, 199, 0.08) 0%, transparent 60%), var(--surface-bg);
      padding: 1.5rem;
    }

    .register-container {
      width: 100%;
      max-width: 440px;
      display: flex;
      flex-direction: column;
      align-items: center;
    }

    .register-brand {
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

    .register-card {
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

    .register-form {
      display: flex;
      flex-direction: column;
      gap: 1.25rem;
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

    .card-footer {
      display: flex;
      justify-content: center;
      align-items: center;
      padding: 1rem 1.5rem 1.5rem 1.5rem;
      border-top: 1px solid var(--border-color);
      background: rgba(0, 0, 0, 0.01);
    }

    .login-hint {
      font-size: 0.875rem;
      color: var(--text-muted);
    }
  `]
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private notification = inject(NotificationService);

  registerForm: FormGroup = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(250)]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(64)]]
  });

  hidePassword = signal<boolean>(true);
  isLoading = signal<boolean>(false);

  onSubmit(): void {
    if (this.registerForm.invalid) return;

    this.isLoading.set(true);
    const { username, email, password } = this.registerForm.value;

    this.authService.register({ username, email, password }).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.notification.showSuccess('Registration successful! Please sign in.');
        this.router.navigate(['/login']);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }
}
