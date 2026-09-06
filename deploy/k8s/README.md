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

## Related Documentation & Glossary

* **[Technical & Architectural Glossary](../../docs/Glossary.md):** Definitions for CNCF Kustomize, StatefulSets, PersistentVolumeClaims, Actuator health probes, and cluster namespaces.
* **[System Design Document](../../docs/System%20Design%20Document.md):** Kubernetes deployment strategy, multi-tier network topologies, and horizontal autoscaling.
* **[Root Project Guide](../../README.md):** Repository layout and multi-container Docker Compose instructions.

