import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { TaskService } from './task.service';
import { AuthService } from './auth.service';

describe('TaskService (CRUD & Mock Store)', () => {
  let taskService: TaskService;
  let authService: AuthService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        AuthService,
        TaskService
      ]
    });
    authService = TestBed.inject(AuthService);
    taskService = TestBed.inject(TaskService);
    authService.setMockMode(true);
  });

  it('should retrieve mock tasks page', async () => {
    const page = await firstValueFrom(taskService.getTasks());
    expect(page.content.length).toBeGreaterThan(0);
    expect(page.totalElements).toBeGreaterThan(0);
  });

  it('should create and append new task', async () => {
    const created = await firstValueFrom(
      taskService.createTask({
        title: 'New Unit Test Task',
        description: 'Verifying signal reactivity',
        priority: 'HIGH',
        status: 'TODO'
      })
    );
    expect(created.id).toBeDefined();
    expect(created.title).toBe('New Unit Test Task');

    const found = await firstValueFrom(taskService.getTaskById(created.id));
    expect(found.id).toBe(created.id);
    expect(found.title).toBe('New Unit Test Task');
  });
});
