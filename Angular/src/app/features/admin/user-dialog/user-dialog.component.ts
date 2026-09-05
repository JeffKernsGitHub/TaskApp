import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CreateUserRequest, UpdateUserRequest, UserResponse } from '../../../core/models/user.models';

export interface UserDialogData {
  mode: 'create' | 'edit';
  user?: UserResponse;
}

@Component({
  selector: 'app-user-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <div class="user-dialog">
      <h2 mat-dialog-title class="dialog-title">
        <mat-icon>{{ data.mode === 'create' ? 'person_add' : 'manage_accounts' }}</mat-icon>
        <span>{{ data.mode === 'create' ? 'Provision User' : 'Edit User Profile' }}</span>
      </h2>

      <mat-dialog-content class="dialog-content">
        <form [formGroup]="userForm" class="user-form">
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Username</mat-label>
            <input matInput formControlName="username" placeholder="e.g. jsmith">
            <mat-icon matPrefix>person</mat-icon>
            @if (userForm.get('username')?.hasError('required') && userForm.get('username')?.touched) {
              <mat-error>Username is required</mat-error>
            }
            @if (userForm.get('username')?.hasError('minlength')) {
              <mat-error>Username must be at least 3 characters</mat-error>
            }
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Email Address</mat-label>
            <input matInput formControlName="email" type="email" placeholder="jsmith@example.com">
            <mat-icon matPrefix>email</mat-icon>
            @if (userForm.get('email')?.hasError('required') && userForm.get('email')?.touched) {
              <mat-error>Email is required</mat-error>
            }
            @if (userForm.get('email')?.hasError('email')) {
              <mat-error>Valid email is required</mat-error>
            }
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Role Authority</mat-label>
            <mat-select formControlName="role">
              <mat-option value="USER">USER (Standard Role)</mat-option>
              <mat-option value="ADMIN">ADMIN (Full Privileges)</mat-option>
            </mat-select>
          </mat-form-field>

          @if (data.mode === 'create') {
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Initial Password</mat-label>
              <input matInput formControlName="password" type="password" placeholder="••••••••">
              <mat-icon matPrefix>lock</mat-icon>
              <mat-hint>Minimum 8 characters</mat-hint>
              @if (userForm.get('password')?.hasError('minlength')) {
                <mat-error>Password must be at least 8 characters</mat-error>
              }
            </mat-form-field>
          }
        </form>
      </mat-dialog-content>

      <mat-dialog-actions align="end" class="dialog-actions">
        <button mat-button mat-dialog-close>Cancel</button>
        <button mat-flat-button color="primary" [disabled]="userForm.invalid" (click)="onSave()">
          <mat-icon>check</mat-icon>
          <span>{{ data.mode === 'create' ? 'Provision' : 'Save' }}</span>
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .user-dialog {
      min-width: 440px;
      max-width: 520px;
    }

    .dialog-title {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 1.25rem;
      font-weight: 700;
      color: var(--text-main);
      padding: 1.25rem 1.5rem 0.5rem 1.5rem;
    }

    .dialog-title mat-icon {
      color: #7c3aed;
    }

    .dialog-content {
      padding: 1rem 1.5rem !important;
    }

    .user-form {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .full-width {
      width: 100%;
    }

    .dialog-actions {
      padding: 1rem 1.5rem 1.25rem 1.5rem;
      border-top: 1px solid var(--border-color);
    }
  `]
})
export class UserDialogComponent implements OnInit {
  dialogRef = inject(MatDialogRef<UserDialogComponent>);
  data: UserDialogData = inject(MAT_DIALOG_DATA);
  private fb = inject(FormBuilder);

  userForm: FormGroup = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(250)]],
    role: ['USER', [Validators.required]],
    password: ['']
  });

  ngOnInit(): void {
    if (this.data.mode === 'create') {
      this.userForm.get('password')?.setValidators([Validators.minLength(8)]);
    }

    if (this.data.mode === 'edit' && this.data.user) {
      this.userForm.patchValue({
        username: this.data.user.username,
        email: this.data.user.email,
        role: this.data.user.role
      });
    }
  }

  onSave(): void {
    if (this.userForm.invalid) return;
    this.dialogRef.close(this.userForm.value);
  }
}
