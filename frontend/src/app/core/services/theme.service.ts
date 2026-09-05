import { Injectable, signal, effect } from '@angular/core';

/**
 * ============================================================================
 * Dark / Light Theme Service (`theme.service.ts`)
 * ============================================================================
 * Manages UI color themes (Dark Mode / Light Mode) using Angular Signals and Effects.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * 1. Angular `effect()`:
 *    - An `effect()` runs automatically whenever any signal read inside its body changes.
 *    - Here, the effect keeps the DOM (`document.body.classList`) and `localStorage` in sync
 *      with the `isDarkMode` signal.
 *
 * 2. Material 3 CSS Variable Integration:
 *    - Toggles `.dark-theme` class on the `<body>` element to switch CSS color palettes.
 */
@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly THEME_KEY = 'taskapp_theme_dark';

  /** Writable Signal indicating whether dark mode is active */
  isDarkMode = signal<boolean>(false);

  constructor() {
    // 1. Initialize from localStorage or user OS preference
    const saved = localStorage.getItem(this.THEME_KEY);
    if (saved !== null) {
      this.isDarkMode.set(saved === 'true');
    } else {
      const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
      this.isDarkMode.set(prefersDark);
    }

    // 2. React to theme signal changes with an Angular Effect
    effect(() => {
      const dark = this.isDarkMode();
      if (dark) {
        document.body.classList.add('dark-theme');
      } else {
        document.body.classList.remove('dark-theme');
      }
      localStorage.setItem(this.THEME_KEY, String(dark));
    });
  }

  /**
   * Toggles between Dark and Light mode
   */
  toggleTheme(): void {
    this.isDarkMode.update(curr => !curr);
  }
}
