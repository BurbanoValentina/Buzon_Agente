# Research: Formulario de Quejas Académicas

## Decision: Use a layered React and Spring Boot monorepo

**Rationale:** The constitution fixes React/JavaScript, Java 21, Spring Boot
3.x, Spring Data JPA, Spring Security, Bean Validation, PostgreSQL, Flyway,
Swagger/OpenAPI, JUnit and Mockito. Separate controllers, services,
repositories, DTOs, security, storage, AI and Telegram adapters keep privacy
boundaries testable and prevent external integrations from entering domain
logic.

**Alternatives considered:** Server-rendered UI, a Node backend or a different
database would violate the approved stack and layering rules.

## Decision: Treat complaint creation as independent from Telegram delivery

**Rationale:** A new complaint must be persisted and confirmed even when
Telegram is unavailable. Notification delivery is asynchronous or best-effort,
has bounded retries, and emits an operational audit/metric without exposing
failure details to the student.

**Alternatives considered:** Making notification success part of the complaint
transaction could lose valid complaints and contradict the specification.

## Decision: Use protected, short-lived Telegram challenges

**Rationale:** Linking challenges expire after 10 minutes; verification
challenges expire after 15 seconds and are single-use. The stored representation
is a hash where one-way validation is sufficient, or encrypted at rest where
replay coordination requires recovery, with expiry, purpose and attempt state.
Neither values nor secrets enter logs.

**Alternatives considered:** Plaintext database values, reusable codes and
long-lived sessions increase replay and disclosure risk.

## Decision: Isolate the Telegram bot and restrict its data surface

**Rationale:** A dedicated adapter validates the webhook secret, identifies the
single linked account, handles linking and OTP delivery, and exposes only
Ayuda, Resumen, Pendientes and Mes. Messages contain no identity, subject,
description, images or credentials. Unknown accounts receive a generic response.

**Alternatives considered:** Student-facing bot access, management commands and
full complaint details are explicitly out of scope and violate privacy rules.

## Decision: Store complaint metadata in PostgreSQL and evidence in Drive

**Rationale:** PostgreSQL stores complaint metadata, status history, monthly
events, administrator settings, audit entries and Drive references. Evidence
is validated, metadata-sanitized before upload, and stored only in the
administrator's Drive folder using `ID-FECHA-CATEGORIA`.

**Alternatives considered:** Database binary storage or public object storage
would contradict the constitution's evidence rule.

## Decision: Generate exports in the backend with an editable report draft

**Rationale:** Administrator-only Excel and Word generation provides consistent
columns, seven sections, evidence links and audit events. A Word draft is
editable in the page before download; identity and images are inserted locally
after AI-generated text is received.

**Alternatives considered:** Client-only export and immutable reports cannot
meet the backend generation and editing requirements.

## Decision: Use advisory AI with simulation mode

**Rationale:** `AiAnalysisProvider` accepts only permitted de-identified text
and aggregates. Predictions require five complaints in the relevant scope,
are labeled estimates, and never decide truth, culpability, sanctions or
completion. Simulation mode makes local and CI tests deterministic.

**Alternatives considered:** Sending full complaint records or coupling domain
decisions to an AI provider violates privacy and governance.

## Decision: Use quality gates and separated deployment

**Rationale:** CI runs backend/frontend tests, JaCoCo 80% threshold, Checkstyle,
SpotBugs/Find Security Bugs, ESLint, OWASP Dependency-Check, container checks
and contract tests. Deployment runs separately, requires CI success, uses a
multi-stage image and verifies health before reporting success. SonarCloud is
optional and cannot replace required local gates.

**Alternatives considered:** A single unverified deployment workflow or
coverage-only quality check would leave security and integration regressions
undetected.
