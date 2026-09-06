# Jenkins CI/CD Automation Architecture

This directory houses the Jenkins CI/CD infrastructure and pipeline definitions for the TaskApp system.

---

## Directory Layout

```
deploy/jenkins/
├── docker/                    # Jenkins controller & agent Docker Compose definitions
└── pipeline/                  # Declarative Jenkinsfile definitions
    ├── Jenkinsfile.frontend   # Angular test, lint, and NGINX image build pipeline
    └── Jenkinsfile.backend    # Maven test, package, and Spring Boot image build pipeline
```

---

## Planned Pipeline Stages

### Frontend Pipeline (`Jenkinsfile.frontend`)
1. **Checkout**: Pull latest code from repository.
2. **Lint & Test**: Run headless tests (`npm test -- --watch=false`).
3. **Build Image**: Multi-stage build producing `taskapp-web:${BUILD_NUMBER}` (`docker build frontend/`).
4. **Security Scan**: Vulnerability scan with Trivy / Hadolint.
5. **Publish / Deploy**: Push to container registry or trigger Kubernetes rolling update.

### Backend Pipeline (`Jenkinsfile.backend`)
1. **Checkout**: Pull latest code from repository.
2. **Unit & Integration Tests**: `./mvnw test` using H2 in-memory test database.
3. **Build Image**: Multi-stage build producing `taskapp-backend:${BUILD_NUMBER}` (`docker build backend/`).
4. **Security Scan**: Dependency-check and container image scan.
5. **Publish / Deploy**: Push to container registry or trigger Kubernetes deployment.

---

## Related Documentation & Glossary

* **[Technical & Architectural Glossary](../../docs/Glossary.md):** Definitions for Jenkins declarative pipelines, Trivy container security scans, multi-stage builds, and CI/CD stages.
* **[System Design Document](../../docs/System%20Design%20Document.md):** End-to-end CI/CD pipeline specifications and security gate policies.
* **[Root Project Guide](../../README.md):** Overall system architecture and quickstart instructions.

