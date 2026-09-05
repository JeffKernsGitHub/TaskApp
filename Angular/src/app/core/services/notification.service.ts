import { Injectable, inject } from '@angular/core';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';

/**
 * ============================================================================
 * Toast Notification Service (`notification.service.ts`)
 * ============================================================================
 * Wrapper service around Angular Material `MatSnackBar` for displaying
 * standardized alerts, error notifications, and success confirmations.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * 1. Material SnackBar Configuration:
 *    - Positions toast alerts at `bottom-right` or `top-center`.
 *    - Applies custom CSS classes (`success-snackbar`, `error-snackbar`, `info-snackbar`).
 *
 * 2. Service Encapsulation:
 *    - Components and interceptors do not need to repeat snackbar configuration code.
 */
@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private snackBar = inject(MatSnackBar);

  private defaultConfig: MatSnackBarConfig = {
    duration: 4000,
    horizontalPosition: 'end',
    verticalPosition: 'bottom'
  };

  /**
   * Displays a green success toast
   */
  showSuccess(message: string, action: string = 'Close'): void {
    this.snackBar.open(message, action, {
      ...this.defaultConfig,
      panelClass: ['success-snackbar']
    });
  }

  /**
   * Displays a red error toast with longer display duration
   */
  showError(message: string, action: string = 'Dismiss'): void {
    this.snackBar.open(message, action, {
      ...this.defaultConfig,
      duration: 6000,
      panelClass: ['error-snackbar']
    });
  }

  /**
   * Displays an informational toast
   */
  showInfo(message: string, action: string = 'OK'): void {
    this.snackBar.open(message, action, {
      ...this.defaultConfig,
      panelClass: ['info-snackbar']
    });
  }
}
