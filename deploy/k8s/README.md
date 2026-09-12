# Kubernetes (K8s) Deployment Architecture

This directory provides cloud-native Kubernetes manifests organized using the **CNCF Kustomize** standard.

---

## Directory Layout

```
deploy/k8s/
├── base/                      # Reusable, environment-agnostic resources
│   ├── postgres/postgres.yaml # PostgreSQL StatefulSet & Service
│   ├── backend/backend.yaml   # Spring Boot API Deployment, Service, ConfigMap
│   ├── frontend/frontend.yaml # Angular NGINX Deployment & Service
│   ├── ingress.yaml           # Ingress routing (/ and /api)
│   └── kustomization.yaml     # Base Kustomize bundle
└── overlays/                  # Environment-specific overlays
    ├── dev/                   # Dev overlay (taskapp-dev namespace, dev- prefix)
    └── prod/                  # Production overlay (taskapp-prod, 3 replicas)
```

---

## Quick Start & Verification

### 1. Dry Run / Inspect Rendered Manifests
```bash
# View rendered Dev manifests
kubectl kustomize deploy/k8s/overlays/dev

# View rendered Prod manifests
kubectl kustomize deploy/k8s/overlays/prod
```

### 2. Deploy to a Cluster (Minikube, k3s, Kind, or Cloud)
```bash
# Create namespace and apply dev stack
kubectl create namespace taskapp-dev --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -k deploy/k8s/overlays/dev
```

### 3. Teardown
```bash
kubectl delete -k deploy/k8s/overlays/dev
```

---

## Local Development on Minikube

To deploy TaskApp locally on Minikube using host-built images:

### 1. Build Host Images & Load into Minikube
Build the container images on your host with Podman and load them directly into the Minikube cluster:
```bash
# Build images from project root with Podman
podman build -t taskapp-backend:latest ./backend
podman build -t taskapp-web:latest ./frontend

# Load images into Minikube's internal container runtime
minikube image load taskapp-backend:latest
minikube image load taskapp-web:latest
```
*(Note: Manifests specify `imagePullPolicy: IfNotPresent`, so Kubernetes uses these loaded local images without attempting to pull from a remote registry).*

### 2. Enable Ingress Addon
Enable Minikube's built-in NGINX Ingress controller:
```bash
minikube addons enable ingress
```

### 3. Deploy Dev Overlay
Minikube's default `standard` hostpath StorageClass automatically satisfies the 5Gi PostgreSQL PersistentVolumeClaim:
```bash
# Create namespace and apply dev stack
kubectl create namespace taskapp-dev --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -k deploy/k8s/overlays/dev

# Verify rollout status
kubectl rollout status statefulset/dev-postgres -n taskapp-dev
kubectl rollout status deployment/dev-backend-deployment -n taskapp-dev
kubectl rollout status deployment/dev-frontend-deployment -n taskapp-dev
```

### 4. Access the Application
* **Via Ingress (Minikube Tunnel):**
  In a separate terminal, launch the tunnel:
  ```bash
  minikube tunnel
  ```
  Then access the frontend at `http://localhost` (or `http://$(minikube ip)`).

* **Via Port Forwarding (Direct Testing):**
  ```bash
  # Frontend Web App
  kubectl port-forward svc/dev-frontend-service 8080:80 -n taskapp-dev

  # Backend REST API
  kubectl port-forward svc/dev-backend-service 8081:8080 -n taskapp-dev
  ```

### 5. Applying Code Updates & Redeploying
When modifying backend (Java) or frontend (Angular/NGINX) source code while Minikube is active:

1. **Rebuild the modified container image:**
   ```bash
   # If backend changed:
   podman build -t taskapp-backend:latest ./backend

   # If frontend changed:
   podman build -t taskapp-web:latest ./frontend
   ```

2. **Reload the updated image into Minikube runtime:**
   ```bash
   minikube image load taskapp-backend:latest   # or taskapp-web:latest
   ```

3. **Trigger a rolling restart of the deployment:**
   ```bash
   # Restart backend:
   kubectl rollout restart deployment/dev-backend-deployment -n taskapp-dev
   kubectl rollout status deployment/dev-backend-deployment -n taskapp-dev

   # Restart frontend:
   kubectl rollout restart deployment/dev-frontend-deployment -n taskapp-dev
   kubectl rollout status deployment/dev-frontend-deployment -n taskapp-dev
   ```

> [!TIP]
> **Minikube Podman Integration:** If running Minikube with the Podman driver (`minikube start --driver=podman`), you can run `eval $(minikube podman-env)` in your terminal. Future `podman build` commands build directly inside Minikube's Podman storage, eliminating the need to execute `minikube image load`!

---

## Related Documentation & Glossary

* **[Technical & Architectural Glossary](../../docs/Glossary.md):** Definitions for CNCF Kustomize, StatefulSets, PersistentVolumeClaims, Actuator health probes, and cluster namespaces.
* **[System Design Document](../../docs/System%20Design%20Document.md):** Kubernetes deployment strategy, multi-tier network topologies, and horizontal autoscaling.
* **[Root Project Guide](../../README.md):** Repository layout and multi-container Podman Compose instructions.

