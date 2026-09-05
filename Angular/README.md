# Task Manager — Angular & Spring Boot Application

A modern, enterprise-grade task management client built with **Angular (Zoneless & Signals)** and **Angular Material 3**, designed as the frontend companion to the **Java EE / Spring Boot** enterprise backend curriculum.

---

## 🌟 Architecture & Key Features

- **⚡ Zoneless Change Detection (`provideZonelessChangeDetection`):**
  - High-performance zoneless execution without `zone.js` runtime overhead.
  - Granular reactivity driven by Angular Signals and native microtasks.
- **📡 Modern Reactivity with Angular Signals:**
  - `signal()`, `computed()`, and `effect()` primitives for fine-grained state management in services and components.
  - Zero RxJS subscription memory leaks in component views.
- **🧩 100% Standalone Component Architecture:**
  - Modern standalone components, directives, and pipes without legacy `NgModule`.
  - Functional router guards (`authGuard`, `adminGuard`) and functional HTTP interceptors (`authInterceptor`, `errorInterceptor`).
- **📋 Interactive Agile Kanban Board:**
  - Drag-and-drop workflow swimlanes (`TODO` ➔ `IN_PROGRESS` ➔ `DONE`) powered by `@angular/cdk/drag-drop`.
  - Synchronizes card transitions directly with the Spring REST API.
- **📊 Spring Actuator Telemetry & Observability:**
  - Real-time health probes (`/actuator/health`), PostgreSQL status, JVM version diagnostics, and Obsidian architecture milestone tracking.
- **🛡️ Spring Security JWT & RFC 9457 Problem Details:**
  - Automatic `Bearer` token injection for protected endpoints.
  - Global error handling compliant with RFC 9457 Problem Details from Spring's `@RestControllerAdvice`.
- **🔄 Dual Execution Engine (Live Backend + Mock Sandbox):**
  - **Live Mode:** Connects to Spring Boot on `http://localhost:8080` via Angular CLI reverse proxy.
  - **Mock Sandbox Mode:** In-memory reactive signal store with pre-seeded data for offline exploration and unit testing.
- **🎨 Angular Material 3 & Dark Mode:**
  - Azure-Blue Material 3 theme palette with custom CSS variable toggling and `localStorage` persistence.

---

## 📁 Project Directory Structure

```text
src/
├── app/
│   ├── core/                          # Core singleton services, models, guards, interceptors
│   │   ├── guards/                    # Functional Route Guards (authGuard, adminGuard)
│   │   ├── interceptors/              # Functional Interceptors (JWT Bearer, RFC 9457 errors)
│   │   ├── models/                    # TypeScript DTOs matching Spring backend contracts
│   │   └── services/                  # Signal-based stores (Auth, Task, User, Actuator, Theme)
│   ├── features/                      # Feature modules (Lazy-loaded standalone components)
│   │   ├── admin/                     # User administration & role provisioning
│   │   ├── auth/                      # Login & Register with Jakarta Validation rules
│   │   ├── system/                    # Spring Boot Actuator telemetry dashboard
│   │   └── tasks/                     # Kanban board, paginated table, dashboard, dialogs
│   ├── layout/                        # App shell, responsive header, and sidebar navigation
│   ├── app.config.ts                  # Zoneless application configuration & providers
│   ├── app.routes.ts                  # Lazy-loaded route definitions
│   └── app.ts                         # Root application component
├── proxy.conf.json                    # Dev proxy forwarding /api and /actuator to localhost:8080
└── styles.css                         # Material 3 theme variables, dark mode, CDK styles
```

---

## 🚀 Getting Started

### Prerequisites
- **Node.js:** v20.x or v22.x LTS
- **npm:** v10.x+
- **Spring Boot Backend (Optional for live mode):** Java 21 LTS running on port `8080`

### Installation

```bash
# Clone or navigate to the Angular directory
cd /home/jeffkerns/Development/TaskApp/Angular

# Install dependencies
npm install
```

### Starting Development Server

```bash
# Starts the development server with Spring Boot API proxy enabled
npm start
```

Navigate to `http://localhost:4200/`.

---

## 🔑 Quick Demo Logins

The application includes one-click demo login buttons on the login page:

| Role | Username | Permissions |
| :--- | :--- | :--- |
| **Administrator** | `admin` | Full CRUD access to all tasks, user administration (`/admin/users`), and system actuator telemetry. |
| **Standard User** | `alice` | Manage assigned tasks, Kanban board, personal dashboard, and task directory. |

> **Tip:** You can toggle between **Live Spring Boot** and **Mock Sandbox** at any time using the mode badge in the top navigation bar.

---

## 🛠️ Build & Test Commands

### Running Unit Tests (Vitest)
```bash
npm test -- --watch=false
```

### Production Build
```bash
npm run build
```
Build artifacts are generated into `dist/Angular/browser/`.

---

## 🔗 Spring Backend API Alignment

| Feature | Angular Endpoint | Spring Boot Controller |
| :--- | :--- | :--- |
| **Authentication** | `POST /api/v1/auth/login`<br>`POST /api/v1/auth/register` | `AuthController` |
| **Task Management** | `GET /api/v1/tasks`<br>`POST /api/v1/tasks`<br>`PUT /api/v1/tasks/{id}`<br>`DELETE /api/v1/tasks/{id}` | `TaskController` |
| **User Management** | `GET /api/v1/users`<br>`POST /api/v1/users`<br>`PUT /api/v1/users/{id}`<br>`DELETE /api/v1/users/{id}` | `UserController` |
| **Actuator Probes** | `GET /actuator/health`<br>`GET /actuator/info` | Spring Boot Actuator |
