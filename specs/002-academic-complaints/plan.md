# Implementation Plan: Formulario de Quejas Académicas

**Branch**: `002-academic-complaints` | **Date**: 2026-11-04 | **Spec**: [spec.md](./spec.md)

## Summary

Se construirá una aplicación web mobile-first para registrar una única clase
de queja, anónima o identificada, con evidencia opcional, y para que un único
administrador consulte, gestione, exporte y analice esos registros. El
frontend será React/JavaScript y el backend Java 21/Spring Boot 3.x con
PostgreSQL y Flyway. Google Drive almacenará evidencia sanitizada; Apache POI
generará Excel y Word editable; la IA será una interfaz intercambiable con
simulación local; y Telegram será un módulo independiente para OTP,
notificaciones mínimas y consultas de solo lectura.

## Technical Context

**Language/Version**: Java 21 LTS (backend), JavaScript/React (frontend)

**Primary Dependencies**: Spring Boot 3.x, Spring Web, Spring Data JPA,
Spring Security, Bean Validation, Flyway, PostgreSQL driver, Swagger/OpenAPI,
Apache POI, Google Drive API client, Telegram Bot API client, React, CSS
Modules, Recharts, JUnit 5, Mockito, MockMvc, Testcontainers, Vitest,
JaCoCo, Checkstyle, SpotBugs with Find Security Bugs, ESLint and OWASP
Dependency-Check

**Storage**: PostgreSQL for complaint metadata, status history, audit records,
administrator settings, Telegram link state and protected challenge metadata;
Google Drive for sanitized JPG/JPEG and PNG evidence. Image binaries are not
stored in PostgreSQL. Verification and linking codes are stored only as
protected hashes or encrypted values and never in logs.

**Testing**: JUnit 5, Mockito and MockMvc for unit/API tests; Testcontainers
for PostgreSQL and integration boundaries; Vitest for frontend tests; JaCoCo
with an 80% coverage threshold; contract, security, accessibility and
end-to-end scenarios. External Telegram, Google Drive and AI calls use mocks
in automated tests.

**Target Platform**: Mobile-first modern browsers and Java 21 server;
Docker es obligatorio para construir la imagen en CI y para ejecutar el
`compose.yaml` local con la aplicación, la base de datos y las pruebas

**Project Type**: Web application with React frontend and Spring Boot REST API

**Performance Goals**: On the academic deployment target, 95% of public form
validation responses complete within 500 ms excluding uploads; 95% of normal
list, filter and dashboard requests complete within 2 seconds; export
generation provides visible progress and completes within 30 seconds for the
planned academic dataset; evidence upload shows progress and never blocks the
student confirmation on a Telegram notification failure.

**Accessibility Goals**: Keyboard-operable workflows, visible focus, labels
associated with every form control, errors announced near the invalid field,
color not used as the sole status signal, responsive layout from 320 px width,
and WCAG 2.1 AA checks for the primary public form and administrator flows.

**Observability Goals**: Structured logs and metrics for request outcome,
validation failures, authentication/lockout events, export duration, external
service failures and Telegram delivery status. Logs MUST exclude complaint
content, identity, credentials and code values. Health checks MUST expose
readiness for PostgreSQL and required configuration without revealing secrets.

**Constraints**: Exactly one complaint type; one administrator and no other
roles; no student accounts; Spanish UI/errors; English code identifiers;
`/api/v1` lowercase plural endpoints; no secrets in source or documentation;
identity and images never sent to AI or Telegram; only approved image formats
and limits; audit records exclude complaint content and identity data; no
advertising trackers or third-party analytics.

**Scale/Scope**: One faculty/institution for this iteration; one administrator;
11 fixed categories; up to five evidence images per complaint; three complaint
states; weekly/monthly dashboard; predictions only at the specified minimum of
five complaints; Excel and editable-in-page Word exports; one linked Telegram
account. Student tracking, email notifications, other roles, native mobile
apps, public dashboards, conversational chatbot behavior and non-image
evidence are excluded.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Initial gate: PASS.** The selected stack and layered boundaries match
constitution v2.1.0. Optional anonymity, identified submissions, evidence,
Google Drive, Excel/Word, advisory AI, predictions, three states, one
administrator and Telegram are explicitly authorized. Telegram remains
administrator-only, read-only for complaint summaries, and cannot block
complaint registration. Secrets and challenge values remain outside source,
documentation and logs.

**Design risks to control:** External services and document generation can fail.
The complaint transaction must not depend on Telegram delivery; Drive failures
must be surfaced without silent data loss; AI receives no identity or images;
all protected routes require authentication; and audit records must not leak
complaint content or identity.

**Post-design gate: PASS.** The research, data model, contracts and quickstart
preserve the constitution's privacy, single-account, state, audit, Telegram,
secret-handling and AI rules. Remaining implementation choices are bounded by
the requested stack and quality gates.

## Project Structure

### Documentation

```text
specs/002-academic-complaints/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
└── tasks.md
```

### Source Code

```text
backend/
├── pom.xml
└── src/
    ├── main/java/com/ucc/complaints/
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── model/
    │   ├── dto/
    │   ├── config/
    │   ├── security/
    │   ├── exception/
    │   ├── ai/
    │   ├── storage/
    │   └── telegram/
    ├── main/resources/db/migration/
    └── test/java/com/ucc/complaints/

frontend/
├── package.json
└── src/
    ├── components/
    ├── pages/
    ├── services/
    ├── hooks/
    ├── utils/
    └── styles/

infra/
├── Dockerfile
├── compose.yaml
└── .env.example

.github/workflows/
├── ci.yml
└── deploy.yml
```

**Structure Decision**: Monorepo split into `/frontend` and `/backend`;
Flyway migrations live with backend resources; Google Drive, AI and Telegram
are isolated adapters; test and quality tooling runs in CI; deployment is a
separate workflow gated by build, tests, security checks and health verification.

## Complexity Tracking

No constitution violations require justification.
