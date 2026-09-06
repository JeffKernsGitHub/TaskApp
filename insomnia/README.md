# Insomnia REST API Collections

This directory houses exported Insomnia workspaces and test suites for the TaskApp REST API.

---

## Workspaces

* **`insomnia-export.Tasks`**:
  * **Stage 4 (Persistence):** Unauthenticated and database CRUD requests for verifying schema and entity mapping.
  * **Stage 5 (Security & JWT):** Authentication endpoints (`/api/v1/auth/login`, `/api/v1/auth/register`), protected Task endpoints with Bearer token injection, and Role-Based Access Control (RBAC) tests.

---

## How to Import

1. Open **Insomnia REST Client**.
2. Go to **Settings / Preferences** ➔ **Data** ➔ **Import Data**.
3. Select **From File** and choose the desired YAML file inside `insomnia-export.Tasks/`.
4. Set the base URL environment variable to `http://localhost:80` (via NGINX reverse proxy) or `http://localhost:8081` (direct to Spring Boot container).

---

## Related Documentation & Glossary

* **[Technical & Architectural Glossary](../docs/Glossary.md):** Definitions for JWT tokens, Bearer authentication, RBAC, and REST API test suites.
* **[System Design Document](../docs/System%20Design%20Document.md):** Complete HTTP request lifecycles, security filter chain, and error contracts.
* **[Backend Service Guide](../backend/README.md):** Spring Boot REST API endpoints, request payloads, and response projections.

