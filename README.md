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
| **[Jenkins CI/CD Automation](deploy/jenkins/README.md)** | DevOps / SRE | Declarative CI/CD pipelines (`Jenkinsfile.frontend`, `Jenkinsfile.backend`), automated testing, and Trivy security scanning. |
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
│   ├── compose/              # Docker Compose orchestration
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
├── docker-compose.yaml       # Root full-stack orchestration
├── .dockerignore             # Root build context filter
├── .env.example              # Root environment variable template
└── README.md                 # Root architecture & onboarding guide
```

---

## Quick Start (Docker Compose)

### 1. Launch the Entire Stack
From the project root:
```bash
docker compose up -d --build
```

### 2. Access the Application
* **Frontend Web App (Angular):** [http://localhost](http://localhost)
* **Backend API (Direct):** [http://localhost:8081](http://localhost:8081)
* **Backend Actuator Health:** [http://localhost/actuator/health](http://localhost/actuator/health)
* **PostgreSQL Database:** `localhost:5432` (`app_db`)

### 3. Teardown
```bash
docker compose down
```
*(Omitting `-v` preserves the PostgreSQL `pgdata` volume and data integrity)*

---

## Kubernetes Deployment (K8s)

The `deploy/k8s/` directory contains Kustomize manifests ready for any Kubernetes cluster (Minikube, Kind, k3s, EKS, GKE, AKS):

```bash
# Preview rendered manifests for Dev environment
kubectl kustomize deploy/k8s/overlays/dev

# Deploy Dev environment
kubectl apply -k deploy/k8s/overlays/dev

# Deploy Prod environment (with scaled replicas)
kubectl apply -k deploy/k8s/overlays/prod
```
