-- =============================================================================
-- Permissions for Spring Boot Application User (spring_boot_user)
-- Database: app_db
-- Schema: tasks
-- =============================================================================

-- 1. Ensure usage on schema
GRANT USAGE ON SCHEMA tasks TO spring_boot_user;

-- 2. Grant DML permissions on all existing tables in schema
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA tasks TO spring_boot_user;

-- 3. Grant usage and selection on all existing sequences (for auto-increment / identity columns)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA tasks TO spring_boot_user;

-- 4. Set default privileges for tables created in the future within the schema
ALTER DEFAULT PRIVILEGES IN SCHEMA tasks 
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO spring_boot_user;

-- 5. Set default privileges for sequences created in the future within the schema
ALTER DEFAULT PRIVILEGES IN SCHEMA tasks 
    GRANT USAGE, SELECT ON SEQUENCES TO spring_boot_user;
