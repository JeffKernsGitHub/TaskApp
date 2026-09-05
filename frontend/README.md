# Frontend Service — Angular & NGINX Web Server

A high-performance, enterprise-grade frontend client built with **Angular 22 (Zoneless & Signals)** and **Angular Material 3**, served in production via a hardened **NGINX 1.27 Alpine** reverse proxy container.

---

## Architecture & Key Features

* **⚡ Zoneless Change Detection (`provideZonelessChangeDetection`):** High-performance reactivity powered by Angular Signals and microtasks without `zone.js` runtime overhead.
* **📡 Signals State Management:** `signal()`, `computed()`, and `effect()` primitives for fine-grained reactivity across components and services with zero RxJS view memory leaks.
* **🧩 100% Standalone Component Architecture:** Functional router guards (`authGuard`, `adminGuard`) and functional HTTP interceptors (`authInterceptor`, `errorInterceptor`).
* **📋 Interactive Agile Kanban Board:** Drag-and-drop workflow swimlanes (`TODO` ➔ `IN_PROGRESS` ➔ `DONE`) using `@angular/cdk/drag-drop` synced with the REST API.
* **📊 Spring Actuator Telemetry Dashboard:** Real-time health probes (`/actuator/health`), JVM metrics, and database connectivity.
* **🛡️ Spring Security JWT & RFC 9457:** Automatic `Bearer` token injection and RFC 9457 Problem Details error handling.
* **🐳 Production NGINX Reverse Proxy Container:**
  * Multi-stage build (`node:22-alpine` build $\to$ `nginx:1.27-alpine` runtime). Total image size ~21MB.
  * Native HTML5 pushState routing (`try_files $uri $uri/ /index.html;`) avoiding 404s on deep links.
  * Dynamic upstream API proxying to Spring Boot (`/api/*` and `/actuator/*`) via `BACKEND_HOST` and `BACKEND_PORT`.
  * Hardened HTTP security headers (`X-Frame-Options`, `X-Content-Type-Options`, `Referrer-Policy`, `X-XSS-Protection`).
  * Optimized gzip compression and 1-year immutable caching on hashed assets.

---

## Directory Structure

```text
frontend/
├── src/
│   ├── app/
│   │   ├── core/                      # Singleton services, models, guards, interceptors
│   │   ├── features/                  # Lazy-loaded standalone feature components (tasks, auth, admin)
│   │   ├── layout/                    # App shell, responsive header, and navigation
│   │   ├── app.config.ts              # Zoneless application configuration & providers
│   │   └── app.routes.ts              # Route definitions
│   ├── index.html
│   ├── main.ts
│   └── styles.css                     # Material 3 Azure theme & dark mode tokens
├── nginx/
│   ├── default.conf.template          # Dynamic virtual host template with envsubst
│   └── nginx.conf                     # Core NGINX configuration (gzip, logging, worker limits)
├── Dockerfile                         # Production multi-stage container build
├── build.sh                           # Container build helper
├── run.sh                             # Container run helper
├── stop.sh                            # Container stop helper
├── proxy.conf.json                    # Angular dev server proxy to localhost:8080
├── package.json
└── angular.json
```

---

## Local Development (Dev Server)

### Prerequisites
* **Node.js:** v22.x or newer
* **npm:** v10.x or newer

### Installation & Run
```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server (with Spring Boot API proxy on localhost:8080)
npm start
```
Navigate to `http://localhost:4200/`.

### Unit Testing
```bash
npm test -- --watch=false
```

---

## Production Docker Container

The frontend image can be built and run standalone or orchestrated via the root `docker-compose.yaml`.

### 1. Build Container Image
```bash
./build.sh
# Options: ./build.sh --tag custom-tag:1.0 --no-cache
```

### 2. Run Container Standalone
```bash
# Standard run (maps port 80, connects to backend on host machine)
./run.sh

# Run on port 8085 with custom backend host/port
./run.sh --port 8085 --backend-host host.docker.internal --backend-port 8080
```

### 3. Stop Container
```bash
./stop.sh
```

---

## Environment Variables (Container Runtime)

| Variable | Default | Description |
|---|---|---|
| `BACKEND_HOST` | `host.docker.internal` | Hostname or IP of the Spring Boot backend service |
| `BACKEND_PORT` | `8080` | Port of the Spring Boot backend service |

---

## Backend REST API Alignment

| Feature | Angular Endpoint | Spring Boot Controller |
| :--- | :--- | :--- |
| **Authentication** | `POST /api/v1/auth/login`<br>`POST /api/v1/auth/register` | `AuthController` |
| **Task Management** | `GET /api/v1/tasks`<br>`POST /api/v1/tasks`<br>`PUT /api/v1/tasks/{id}`<br>`DELETE /api/v1/tasks/{id}` | `TaskController` |
| **User Management** | `GET /api/v1/users`<br>`POST /api/v1/users`<br>`PUT /api/v1/users/{id}`<br>`DELETE /api/v1/users/{id}` | `UserController` |
| **Actuator Probes** | `GET /actuator/health`<br>`GET /actuator/info` | Spring Boot Actuator |
