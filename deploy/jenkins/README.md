# Jenkins CI/CD Automation Architecture

This directory houses the Jenkins CI/CD container infrastructure and declarative pipeline definitions for the TaskApp system.

---

## Directory Layout

```
deploy/jenkins/
├── docker/
│   ├── Dockerfile             # Custom Jenkins LTS image with JDK 25, Docker CLI, kubectl, kustomize
│   ├── docker-compose.yaml    # Jenkins controller orchestration using persistent external volumes
│   ├── .env.example           # Environment template (ports, volume names)
│   ├── plugins.txt            # Pre-installed Jenkins plugins (Pipelines, Git, Docker, K8s CLI)
│   ├── entrypoint.sh          # Container wrapper managing host Docker socket GID permissions
│   └── init-volumes.sh        # Setup script to provision persistent external Docker volumes
└── pipeline/
    ├── Jenkinsfile.docker     # Pipeline 1: Docker build, test & Compose orchestration pipeline
    └── Jenkinsfile.k8s        # Pipeline 2: Kubernetes CNCF Kustomize overlay deployment pipeline
```

When configuring Jenkins jobs (Pipeline from SCM), set the **Script Path** to:
* `deploy/jenkins/pipeline/Jenkinsfile.docker`
* `deploy/jenkins/pipeline/Jenkinsfile.k8s`

---

## Container Architecture & JDK 25 Integration

* **Base Controller:** `jenkins/jenkins:lts-jdk21` provides a stable LTS Jenkins runtime.
* **JDK 25 Runtime:** **Eclipse Temurin JDK 25** is installed into `/opt/java/openjdk-25` (`JAVA25_HOME`).
  * Backend compilation and testing tasks explicitly invoke JDK 25 (`export JAVA_HOME=${JAVA25_HOME}`), fulfilling TaskApp's Java 25 requirement without conflicting with the Jenkins controller daemon.
* **Toolchain Bundled in Container:**
  * **Docker CLI & Plugins:** `docker-ce-cli`, `docker-compose-plugin`, `docker-buildx-plugin`.
  * **CNCF Kubernetes Tools:** `kubectl` and `kustomize`.
  * **Node.js 22 LTS & npm:** For Angular frontend compilation and linting checks.
  * **Security & Execution:** `entrypoint.sh` dynamically matches the container's Docker group GID with `/var/run/docker.sock`, eliminating socket permission errors.

---

## Mutable File Storage (External Volumes)

All mutable Jenkins state is isolated in persistent external Docker volumes:

1. **`jenkins_data`** (`/var/jenkins_home`):
   * Stores user accounts, pipeline job configurations, build history, workspace artifacts, and secrets.
   * Defined with `external: true` in `docker-compose.yaml` to ensure `docker compose down -v` never deletes mutable configuration.
2. **`jenkins_maven_cache`** (`/var/jenkins_home/.m2`):
   * Stores cached Maven dependencies across build executions, preventing repeated downloads of Spring Boot dependencies.

### Provisioning External Volumes

Run the initialization script from `deploy/jenkins/docker/`:

```bash
cd deploy/jenkins/docker
./init-volumes.sh
```

Or create them manually:

```bash
docker volume create jenkins_data
docker volume create jenkins_maven_cache
```

---

## Running Jenkins Controller

1. Ensure the host Docker daemon is running.
2. Navigate to the docker directory:
   ```bash
   cd deploy/jenkins/docker
   ```
3. Initialize the volumes and launch the service:
   ```bash
   ./init-volumes.sh
   docker compose up -d
   ```
4. Access the Jenkins Web UI at `http://localhost:8080`.
5. Retrieve the initial admin password (on first startup):
   ```bash
   docker exec -it taskapp-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
   ```

---

## Pipelines

### 1. Docker Pipeline (`Jenkinsfile.docker`)
Orchestrates testing, container packaging, and local Docker Compose stack verification:
1. **Checkout Source:** Pulls latest branch from Git repository.
2. **Environment Diagnostics:** Verifies JDK 25 (`$JAVA25_HOME/bin/java -version`), Docker CLI, and Docker Compose versions.
3. **Backend Automated Tests (JDK 25):** Executes Spring Boot tests with `./mvnw clean test` using the in-memory H2 database. Publishes JUnit surefire test reports.
4. **Frontend Lint & Test Build:** Executes `npm ci` and builds production Angular bundle.
5. **Build Docker Images:** Builds `taskapp-backend:${IMAGE_TAG}` and `taskapp-web:${IMAGE_TAG}` using the respective multi-stage Dockerfiles.
6. **Docker Compose Smoke Test:** Spins up `deploy/Docker/docker-compose.yaml` in background, polls the backend `/actuator/health` endpoint, and tears down gracefully.
7. **Push (Optional):** Pushes tagged images to the specified registry if enabled.

### 2. Kubernetes Pipeline (`Jenkinsfile.k8s`)
Orchestrates CNCF Kustomize overlay configuration, validation, cluster deployment, and rollout health monitoring:
1. **Checkout Source:** Pulls latest branch from Git repository.
2. **Diagnostics:** Checks JDK 25, Docker, `kubectl`, and `kustomize` availability.
3. **Automated Tests:** Parallel validation of Spring Boot backend (JDK 25) and Angular frontend.
4. **Build Container Images:** Builds `taskapp-backend` and `taskapp-web` images.
5. **Configure Kustomize Overlay:** Sets image references via `kustomize edit set image` in `deploy/k8s/overlays/${TARGET_ENV}` and renders the complete manifest.
6. **Validate Manifests:** Performs Kustomize bundle generation and validation with `kubectl kustomize`.
7. **Deploy to Cluster:** Applies rendered overlay to `taskapp-${TARGET_ENV}` namespace with `kubectl apply -k deploy/k8s/overlays/${TARGET_ENV}`.
8. **Rollout Health Verification:** Monitors `kubectl rollout status` for backend and frontend deployments until readiness probes pass.

---

## Continuous Deployment After Code Changes via Jenkins

### How Does Jenkins Know Which Run Mode Is Being Used?

At any given time, TaskApp may be running locally in **Docker Compose** or **Kubernetes (Minikube)**. Jenkins determines which environment to target using **dedicated on-demand pipelines**:

1. **Job Separation (Explicit & Deterministic):**
   * Jenkins hosts two distinct jobs:
     * `taskapp-docker-deploy` (points to `Jenkinsfile.docker`)
     * `taskapp-k8s-deploy` (points to `Jenkinsfile.k8s`)
   * Because you choose which runtime to start for your working session (`docker compose up` vs `minikube start`), you trigger the corresponding job on demand when ready to deploy.
2. **Environment Isolation:**
   * `Jenkinsfile.docker` operates on the host Docker daemon via `/var/run/docker.sock` and manages `deploy/Docker/docker-compose.yaml`.
   * `Jenkinsfile.k8s` invokes `kubectl` and `kustomize` against the Minikube/Kubernetes cluster (checking cluster availability and managing the `taskapp-dev` namespace).

---

### Primary Workflow: On-Demand Builds (Recommended)

On-demand builds ensure your workflow remains intentional, allowing you to code and iterate locally, and trigger Jenkins testing & deployment only when you reach a logical milestone.

#### 1. Via Jenkins Web UI
1. Open Jenkins at `http://localhost:8080`.
2. Click on the job matching your current session:
   * Working in Docker? ➔ Open **`taskapp-docker-deploy`** ➔ Click **Build Now** (or **Build with Parameters**).
   * Working in Minikube? ➔ Open **`taskapp-k8s-deploy`** ➔ Click **Build with Parameters** (select `TARGET_ENV: dev`) ➔ Click **Build**.
3. Watch real-time execution across the stage view and verify health probes.

#### 2. Via Terminal / CLI (1-Click On-Demand Trigger)
You can trigger on-demand deployments straight from your terminal without opening a browser:
```bash
# Deploy to Docker Compose on demand:
curl -X POST http://localhost:8080/job/taskapp-docker-deploy/build \
  --user admin:<JENKINS_API_TOKEN>

# Deploy to Kubernetes / Minikube on demand:
curl -X POST http://localhost:8080/job/taskapp-k8s-deploy/buildWithParameters \
  --user admin:<JENKINS_API_TOKEN> \
  --data-urlencode TARGET_ENV=dev
```

---

### Secondary Workflow: Automated Builds (Optional)

If you prefer Jenkins to automatically test and redeploy every time you commit and push code to Git:

#### Option A: SCM Polling (No Webhook Required)
1. Open the job configuration in Jenkins (`http://localhost:8080/job/<job-name>/configure`).
2. Under **Build Triggers**, check **Poll SCM**.
3. Set the schedule (e.g. `H/5 * * * *` polls the Git repository every 5 minutes).
4. Jenkins will automatically trigger a build whenever new commits are detected on the branch.

#### Option B: Git Push Webhook (Instant Trigger)
1. In Jenkins job configuration, check **GitHub hook trigger for GITScm polling**.
2. In your Git host (GitHub, GitLab, or Gitea), add a Webhook pointing to `http://<jenkins-ip>:8080/github-webhook/`.
3. Pushing code (`git push origin main`) will instantly trigger the build and deployment.

---

### Initial Job Setup in Jenkins UI

For each pipeline:
1. Open Jenkins at `http://localhost:8080` ➔ **New Item** ➔ Name it (e.g. `taskapp-docker-deploy` or `taskapp-k8s-deploy`) ➔ Select **Pipeline** ➔ Click **OK**.
2. Under **Pipeline**:
   * **Definition:** Select `Pipeline script from SCM`.
   * **SCM:** Select `Git` and enter repository URL (e.g. `https://github.com/JeffKernsGitHub/TaskApp.git` or local path).
   * **Branches to build:** `*/main`.
   * **Script Path:**
     * For Docker: `deploy/jenkins/pipeline/Jenkinsfile.docker`
     * For Kubernetes: `deploy/jenkins/pipeline/Jenkinsfile.k8s`
3. Click **Save**.

---

## Related Documentation & Glossary

* **[Technical & Architectural Glossary](../../docs/Glossary.md):** Definitions for Jenkins declarative pipelines, Kustomize overlays, and Actuator health probes.
* **[System Design Document](../../docs/System%20Design%20Document.md):** End-to-end CI/CD pipeline specifications and security gate policies.
* **[Kubernetes Deployment Guide](../k8s/README.md):** Cluster manifests and Kustomize overlays.
* **[Docker Compose Guide](../Docker/docker-compose.yaml):** Local multi-container orchestration.
