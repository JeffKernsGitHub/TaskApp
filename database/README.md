# PostgreSQL Database Architecture

This directory houses the PostgreSQL schema definitions, permissions, and seed datasets for the TaskApp system.

---

## Directory Layout

```
database/
├── ddl/
│   └── task-schema.sql        # Database schema, custom ENUM types, and tables
├── permissions/
│   └── permissions.sql        # Role-based grants for application users
└── seeds/
    └── gendata.sql            # Seed dataset for development and testing
```

---

## Database & Schema Specification

* **Database Name:** `app_db`
* **Isolated Schema:** `tasks`
* **Application User:** `spring_boot_user`

### Custom ENUM Types
* `tasks.task_priority`: `'LOW'`, `'MEDIUM'`, `'HIGH'`
* `tasks.task_status`: `'TODO'`, `'IN_PROGRESS'`, `'DONE'`
* `tasks.user_role`: `'USER'`, `'ADMIN'`

### Core Tables
1. **`tasks.users`**: Identity, username, email, password hash, role, and timestamps.
2. **`tasks.tasks`**: User task records with priority, status, due date check constraints, and foreign key to `tasks.users`.

---

## Initialization Instructions

To manually apply the schema and seed data against a running PostgreSQL container or local instance:

```bash
# 1. Apply DDL Schema
docker exec -i postgres_db psql -U pguser -d app_db < ddl/task-schema.sql

# 2. Grant Permissions
docker exec -i postgres_db psql -U pguser -d app_db < permissions/permissions.sql

# 3. Load Development Seed Data
docker exec -i postgres_db psql -U pguser -d app_db < seeds/gendata.sql
```
