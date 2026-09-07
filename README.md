# TaskApp - Full-Stack Enterprise Task Management Application

An enterprise-grade cloud-native portfolio project demonstrating modern full-stack architecture, microservice patterns, containerization, and DevOps engineering (Docker, Docker Compose, Kubernetes/K8s, and Jenkins CI/CD).

> 📄 **Core Reference Documentation:**
> * **[System Design Document](docs/System%20Design%20Document.md)** — Architectural principles, ADRs, NIST SP 800-63B / SP 800-53 session compliance, microservice topology, and sequence flows.
> * **[Database Data Dictionary](docs/Data%20Dictionary.md)** — PostgreSQL 18 schema catalog, custom ENUM types, indexing justifications, referential integrity, and DBA operational guidelines.
> * **[Technical Glossary](docs/Glossary.md)** — Comprehensive dictionary of domain terms, security controls, architectural patterns, and cloud-native concepts.

---

## Architecture Overview

```
                                    ┌──────────────────────────────────┐
                                    │         Client / Browser         │
                                    └─────────────────┬────────────────┘
                                                      │ http://localhost (Port 80)
                                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────────────────┐
│ Docker Network: taskapp-net / Kubernetes Cluster Namespace                                  │
│                                                                                             │
│  ┌────────────────────────┐         Proxy /api/*          ┌──────────────────────────────┐  │
│  │   NGINX Web Server     ├──────────────────────────────►│    Spring Boot Backend       │  │
│  │     (Angular SPA)      │                               │       (REST API)             │  │
│  │  Container: taskapp-web│◄──────────────────────────────┤  Container: taskmanager-api  │  │
│  │       Port: 80         │         Actuator / Health     │    Port: 8080 (Mapped 8081)  │  │
│  └────────────────────────┘                               └──────────────┬───────────────┘  │
│                                                                          │                  │
│                                                                          │ JDBC (app_db)    │
│                                                                          ▼                  │
│                                                           ┌──────────────────────────────┐  │
│                                                           │      PostgreSQL 18 DB        │  │
│                                                           │   Container: postgres_db     │  │
│                                                           │         Port: 5432           │  │
│                                                           │       Volume: pgdata         │  │
│                                                           └──────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────────────────────┘
```

### Technology Stack

* **Frontend (`frontend/`):** Angular 22 (Standalone Components, Signals, Material 3, HTML5 PushState routing)
* **Web Server & Reverse Proxy (`frontend/nginx/`):** NGINX 1.27 Alpine (Gzip compression, SPA fallback routing, dynamic upstream DNS)
* **Backend (`backend/`):** Spring Boot 4.1.1 on Java 25 (Project Loom Virtual Threads, Spring Security, JWT HMAC-SHA256, Spring Data JPA / Hibernate 7)
* **Database (`database/`):** PostgreSQL 18 Alpine (Custom `tasks` schema, ENUM types, custom sequences, HikariCP connection pooling)
* **Orchestration & DevOps (`deploy/`):**
  * **Docker & Compose:** Multi-stage builds, non-root runtime users, healthcheck probes
  * **Kubernetes (`deploy/k8s/`):** CNCF Kustomize manifests (Deployments, StatefulSets, Services, Ingress, Overlays)
  * **Jenkins (`deploy/jenkins/`):** Automation controller infrastructure and declarative pipelines

---

## Documentation & Reference Index

Comprehensive engineering specifications, architectural decision records, and operational guides are maintained in the repository:

| Document | Primary Audience | Description |
| :--- | :--- | :--- |
| **[System Design Document](docs/System%20Design%20Document.md)** | Senior Engineers / Architects | Three-tier architecture, ADRs, NIST SP 800-63B / SP 800-53 session security, sequence flows, HikariCP tuning, and scalability roadmap. |
| **[Database Data Dictionary](docs/Data%20Dictionary.md)** | DBAs / Data Engineers | PostgreSQL 18 schema (`tasks`), custom ENUM types, column catalogs, index justifications, cascading deletes, and autovacuum maintenance. |
| **[Technical Glossary](docs/Glossary.md)** | All Engineering Roles | Comprehensive reference of domain entities, NIST compliance standards, Angular/Spring paradigms, and DevOps terminology. |
| **[Javadoc API Reference](docs/README.md)** | Backend Engineers | HTML5 Javadoc specifications, class contracts, and package diagrams generated from Spring Boot sources (`docs/javadoc/`). |
| **[Database Architecture Guide](database/README.md)** | DBAs / DevOps | Schema DDL scripts, least-privilege role grants (`spring_boot_user`), and test data seed generation. |
| **[Kubernetes Deployment Guide](deploy/k8s/README.md)** | DevOps / Cloud Engineers | CNCF Kustomize architecture, base/overlay configurations (`dev`/`prod`), StatefulSet specs, and ingress routing. |
| **[Jenkins CI/CD Automation](deploy/jenkins/README.md)** | DevOps / SRE | Declarative CI/CD pipelines (`Jenkinsfile.docker`, `Jenkinsfile.k8s`), automated testing, container builds, and deployment. |
| **[REST API Test Suites (Insomnia)](insomnia/README.md)** | QA / API Developers | Insomnia REST collections for CRUD verification, JWT Bearer token authentication, and RBAC endpoint testing. |

---

## Project Structure

```
TaskApp/
├── frontend/                 # Angular 22 Single Page Application
│   ├── src/                  # Angular source code (Standalone, Signals, Material 3)
│   ├── nginx/                # NGINX reverse proxy template & configurations
│   ├── Dockerfile            # Multi-stage Angular build -> NGINX Alpine runtime
│   ├── build.sh / run.sh     # Local container automation scripts
│   └── package.json
│
├── backend/                  # Spring Boot 4 REST API Service (Java 25)
│   ├── src/                  # Java controllers, services, repositories, entities
│   ├── Dockerfile            # 3-stage Eclipse Temurin JDK 25 -> Custom Server JRE (jlink) on Alpine
│   ├── pom.xml / mvnw        # Maven project descriptor & wrapper (JDK 25)
│   └── build.sh              # Backend container build script
│
├── database/                 # PostgreSQL Database Assets
│   ├── ddl/                  # Schema definitions, enum types, table DDL
│   ├── permissions/          # Grants, roles, and privilege scripts
│   ├── seeds/                # Initial seed datasets
│   └── README.md             # Database architecture & initialization guide
│
├── deploy/                   # DevOps, CI/CD, and Cloud-Native Infrastructure
│   ├── Docker/               # Docker Compose full-stack orchestration
│   ├── jenkins/              # Jenkins CI/CD infrastructure & pipelines
│   └── k8s/                  # Kubernetes Manifests (CNCF Kustomize standard)
│       ├── base/             # Base deployments, services, statefulsets, ingress
│       └── overlays/         # Environment overlays (dev, prod)
│
├── docs/                     # Technical Documentation & Specifications
│   ├── System Design Document.md # Comprehensive system design & ADRs
│   ├── Data Dictionary.md    # DBA data dictionary & PostgreSQL catalog
│   ├── Glossary.md           # Engineering & architectural glossary
│   ├── javadoc/              # Pre-generated HTML5 API documentation
│   └── README.md             # Documentation directory overview
│
├── insomnia/                 # REST API test collections & environments
│   ├── insomnia-export.Tasks # Exported workspace test suites
│   └── README.md             # Insomnia import and execution guide
│
├── .dockerignore             # Root build context filter
└── README.md                 # Root architecture & onboarding guide
```

## Application Run Modes

TaskApp supports two containerized runtime modes:

1. **Docker Mode (Docker Compose):** Orchestrated multi-container local environment (`PostgreSQL` + `Spring Boot` + `NGINX/Angular`).
2. **Kubernetes Mode (K8s / Minikube):** Cloud-native declarative deployment using CNCF Kustomize overlays (`dev` and `prod`).

---

## Run Mode 1: Docker Compose

### 1. Provision Persistent Storage (First-Time Setup)
Create the persistent named volume for PostgreSQL data if not already present:
```bash
docker volume create pgdata
```

### 2. Launch the Stack
From the project root:
```bash
docker compose -f deploy/Docker/docker-compose.yaml up -d --build
```
Or from the `deploy/Docker` directory:
```bash
cd deploy/Docker
docker compose up -d --build
```

### 3. Access the Application
* **Frontend Web App (Angular):** [http://localhost](http://localhost)
* **Backend API (Direct):** [http://localhost:8081](http://localhost:8081)
* **Backend Actuator Health:** [http://localhost/actuator/health](http://localhost/actuator/health)
* **PostgreSQL Database:** `localhost:5432` (`app_db`)

### 4. Applying Code Changes (Redeploying)
When modifying frontend or backend source code, rebuild and restart only the affected container without restarting PostgreSQL:
```bash
# Redeploy Backend after Java/Spring changes:
docker compose -f deploy/Docker/docker-compose.yaml up -d --build backend

# Redeploy Frontend after Angular/NGINX changes:
docker compose -f deploy/Docker/docker-compose.yaml up -d --build frontend

# Redeploy Both:
docker compose -f deploy/Docker/docker-compose.yaml up -d --build
```

### 5. Teardown
```bash
docker compose -f deploy/Docker/docker-compose.yaml down
```
*(Omitting `-v` preserves the PostgreSQL `pgdata` volume and data integrity)*

---

## Run Mode 2: Kubernetes (K8s / Minikube)

TaskApp includes declarative CNCF Kustomize manifests designed for local development on **Minikube** (as well as Kind, k3s, EKS, GKE, and AKS). For detailed Minikube instructions including image loading, ingress addon setup, and minikube tunneling, see the **[Kubernetes Deployment Guide](deploy/k8s/README.md)**.

### Quick Start:
```bash
# 1. Build and load initial images into Minikube
docker build -t taskapp-backend:latest ./backend
docker build -t taskapp-web:latest ./frontend
minikube image load taskapp-backend:latest
minikube image load taskapp-web:latest

# 2. Enable Minikube Ingress addon
minikube addons enable ingress

# 3. Deploy Dev environment to cluster
kubectl create namespace taskapp-dev --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -k deploy/k8s/overlays/dev

# 4. Verify rollout
kubectl rollout status statefulset/dev-postgres -n taskapp-dev
kubectl rollout status deployment/dev-backend-deployment -n taskapp-dev
kubectl rollout status deployment/dev-frontend-deployment -n taskapp-dev
```

### Applying Code Changes (Redeploying to Minikube):
When you modify frontend or backend code while the cluster is running:

```bash
# 1. Rebuild the modified container image:
docker build -t taskapp-backend:latest ./backend   # for Backend changes
docker build -t taskapp-web:latest ./frontend       # for Frontend changes

# 2. Reload the updated image into Minikube runtime:
minikube image load taskapp-backend:latest          # for Backend
minikube image load taskapp-web:latest              # for Frontend

# 3. Trigger a rolling restart to pick up the updated image:
kubectl rollout restart deployment/dev-backend-deployment -n taskapp-dev
kubectl rollout restart deployment/dev-frontend-deployment -n taskapp-dev

# 4. Verify rollout completion:
kubectl rollout status deployment/dev-backend-deployment -n taskapp-dev
kubectl rollout status deployment/dev-frontend-deployment -n taskapp-dev
```
> [!TIP]
> **Minikube Docker Shortcut:** Run `eval $(minikube docker-env)` in your terminal once. Future `docker build` commands will build directly inside Minikube's Docker daemon, skipping the `minikube image load` step!

### Teardown:
```bash
kubectl delete -k deploy/k8s/overlays/dev
```

---

## Automated CI/CD Redeployment with Jenkins

Instead of manual container rebuilding and restarts, you can use Jenkins to automatically test, build, and redeploy TaskApp whenever you modify backend or frontend code.

The repository includes a containerized Jenkins controller under `deploy/jenkins/docker/` with pre-configured toolchains (JDK 25, Docker CLI, `kubectl`, `kustomize`, Node 22) and two declarative pipelines under `deploy/jenkins/pipeline/`:

| Run Mode | Jenkins Pipeline File | Automation Workflow |
| :--- | :--- | :--- |
| **Docker Compose** | [`Jenkinsfile.docker`](deploy/jenkins/pipeline/Jenkinsfile.docker) | Executes JDK 25 unit tests & Angular build, builds Docker images, spins up `deploy/Docker/docker-compose.yaml`, runs Actuator health smoke tests, and tears down test containers. |
| **Kubernetes / Minikube** | [`Jenkinsfile.k8s`](deploy/jenkins/pipeline/Jenkinsfile.k8s) | Parallel test execution, container image builds, dynamic Kustomize overlay configuration (`kustomize edit set image`), `kubectl apply -k deploy/k8s/overlays/${TARGET_ENV}`, and rollout verification. |

### 1. Start the Jenkins Controller
```bash
cd deploy/jenkins/docker
./init-volumes.sh
docker compose up -d
```
Access Jenkins at [http://localhost:8080](http://localhost:8080).

### 2. Session Run Mode Determination
Because TaskApp can be running in either Docker Compose or Minikube during any given session, Jenkins separates the run modes into **dedicated on-demand jobs**:
* **`taskapp-docker-deploy`:** Targets your active Docker Compose stack (`Jenkinsfile.docker`).
* **`taskapp-k8s-deploy`:** Targets your active Minikube/Kubernetes cluster (`Jenkinsfile.k8s`).

You trigger the job corresponding to the runtime you started for that development session.

### 3. On-Demand Deployment (Preferred)
* **Via Jenkins UI:** Open [http://localhost:8080](http://localhost:8080), select the job for your active session, and click **Build with Parameters** (or **Build Now**).
* **Via Terminal CLI (1-Click Trigger):**
  ```bash
  # Deploy to Docker Compose on demand:
  curl -X POST http://localhost:8080/job/taskapp-docker-deploy/build \
    --user admin:<JENKINS_API_TOKEN>

  # Deploy to Minikube on demand:
  curl -X POST http://localhost:8080/job/taskapp-k8s-deploy/buildWithParameters \
    --user admin:<JENKINS_API_TOKEN> \
    --data-urlencode TARGET_ENV=dev
  ```

### 4. Automated Builds (Optional)
If you prefer automatic builds on every commit instead of on-demand:
* **SCM Polling:** In the Jenkins job under **Build Triggers**, enable **Poll SCM** (e.g. `H/5 * * * *` checks every 5 minutes).
* **GitHub Webhook:** Check **GitHub hook trigger for GITScm polling** to immediately build and deploy upon `git push`.

For complete configuration instructions, see the **[Jenkins CI/CD Automation Guide](deploy/jenkins/README.md)**.



