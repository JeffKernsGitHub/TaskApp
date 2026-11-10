import { TestBed } from '@angular/core/testing';
import { Component, provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';

@Component({ standalone: true, template: '' })
class DummyComponent {}

describe('AuthService (Signals & Zoneless)', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([
          { path: 'tasks', component: DummyComponent },
          { path: 'login', component: DummyComponent }
        ]),
        provideHttpClient(),
        provideHttpClientTesting(),
        AuthService
      ]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should initialize with default unauthenticated signal state', () => {
    expect(service.currentUser()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
    expect(service.isAdmin()).toBe(false);
  });

  it('should update signals on admin quick login', () => {
    service.quickLoginAsAdmin().subscribe();
    const req = httpMock.expectOne('/api/v1/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush({
      accessToken: 'test-jwt-token',
      tokenType: 'Bearer',
      expiresIn: 3600,
      username: 'admin',
      role: 'ADMIN'
    });

    expect(service.currentUser()).not.toBeNull();
    expect(service.username()).toBe('admin');
    expect(service.role()).toBe('ADMIN');
    expect(service.isAdmin()).toBe(true);
    expect(service.isAuthenticated()).toBe(true);
  });

  it('should clear signals on logout', () => {
    service.quickLoginAsAdmin().subscribe();
    const req = httpMock.expectOne('/api/v1/auth/login');
    req.flush({
      accessToken: 'test-jwt-token',
      tokenType: 'Bearer',
      expiresIn: 3600,
      username: 'admin',
      role: 'ADMIN'
    });

    expect(service.isAuthenticated()).toBe(true);
    service.logout();
    expect(service.currentUser()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
  });
});
