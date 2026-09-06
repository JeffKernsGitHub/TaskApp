# TaskApp Documentation

This directory contains technical documentation, API specifications, and generated Javadocs for the TaskApp system.

---

## Contents

* **[System Design Document](System%20Design%20Document.md)**: Comprehensive Software & System Design Document (SDD) detailing architecture, design patterns, ADRs, security compliance (NIST SP 800-63B / SP 800-53), data persistence, containerization, and K8s/Jenkins deployment.
* **[Data Dictionary](Data%20Dictionary.md)**: Production-grade database data dictionary specifying PostgreSQL 18 schema (`tasks`), custom ENUM types, table/column catalogs, index justifications, referential integrity rules, and DBA operational guidelines.
* **[Glossary](Glossary.md)**: Comprehensive technical and architectural glossary cataloging domain models, security controls (NIST), backend/frontend patterns, PostgreSQL persistence, and DevOps/CI/CD terminology.
* **`javadoc/`**: Pre-generated HTML5 API documentation generated from the Spring Boot backend using `mvn javadoc:javadoc`.
  * Open `docs/javadoc/apidocs/index.html` in any browser to inspect full class diagrams, method contracts, and package structures.
