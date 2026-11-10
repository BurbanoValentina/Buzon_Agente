# Quickstart Validation

## Prerequisites

- Java 21, Maven, Node.js/npm and Docker are installed.
- PostgreSQL can run through `compose.yaml` or a local service.
- Google Drive, AI and Telegram secrets are configured only through an
  excluded `.env`; `.env.example` contains names and safe placeholders only,
  including `TELEGRAM_BOT_TOKEN` and `TELEGRAM_WEBHOOK_SECRET`.
- Do not claim an integration works until its configured environment and
  validation command have actually succeeded.

## Local start

1. Copy `.env.example` to `.env` and provide local values outside version
   control.
2. Start dependencies with `docker compose up --build` or start PostgreSQL
   separately and run the backend migrations.
3. Start the backend from `/backend` and the frontend from `/frontend`.
4. Open the public form in a 320 px mobile viewport. Use the hidden logo
   control to open administrator login.
5. Inspect the `/api/v1` endpoints through Swagger/OpenAPI.

## Functional validation

1. Submit anonymous and identified complaints; verify identity visibility and
   absence of anonymous identity storage.
2. Validate all 11 categories, conditional number field, counters and subject
   and description bounds.
3. Upload valid JPG/JPEG and PNG evidence, reject another format, a sixth image
   and an image over 100 MB; verify metadata sanitization and Drive naming.
4. As administrator, filter and download complete and filtered Excel; verify
   columns, evidence links and audit records.
5. Generate, edit and download the seven-section Word draft; verify title,
   download date, no logo, evidence insertion and human-review wording.
6. Verify AI requests exclude names, semesters and images; use simulation mode
   for deterministic local tests.
7. Verify dashboard data, empty months, five-complaint prediction threshold,
   state transitions, immutable dates and monthly history.
8. Verify login lockout, inactivity logout, recovery question and credential
   format without using committed credentials.

## Telegram validation

1. Run the Telegram adapter tests with mocks and a webhook secret.
2. Link one account with a 10-minute single-use code and replace it with
   another account.
3. Verify 15-second single-use OTPs for login/password changes, three-code
   fallback to the question, protected storage and absent log values.
4. Verify notification enable/disable and minimal new-complaint payload.
5. Verify `Ayuda`, `Resumen`, `Pendientes` and `Mes` are read-only; unknown or
   unlinked accounts receive no complaint data.
6. Simulate Telegram outage and confirm complaint registration still succeeds.

## Quality and delivery checks

- Backend: `mvn test`, JaCoCo report with at least 80% coverage,
  Checkstyle, SpotBugs/Find Security Bugs and OWASP Dependency-Check.
- Frontend: Vitest and ESLint.
- Integration: Testcontainers PostgreSQL and MockMvc/API contract tests.
- Accessibility: keyboard, focus, labels, errors, responsive 320 px and
  WCAG 2.1 AA checks for primary flows.
- CI: run the complete quality gate and container build in GitHub Actions.
- Deployment: execute only from the separate deployment workflow after CI
  success, then verify the app health endpoint.
- Optional SonarCloud analysis may provide additional findings but is not a
  substitute for required checks.

## Última validación ejecutada

- Frontend disponible en `http://localhost:5173` con respuesta HTTP 200.
- El puerto 8080 está ocupado por `AgentService.exe`; por ello la comprobación
  HTTP de Actuator no pudo completarse y el backend no quedó confirmado como activo.
- Frontend: `npm ci`, ESLint, Vitest y Vite build completaron correctamente.
- Vitest: 4 archivos y 9 pruebas aprobadas.
- Docker Compose: `docker compose -f infra/compose.yaml config` completó correctamente.
- Backend: Maven `verify` compiló las fuentes principales y las pruebas tras una
  corrección de compilación; la ejecución en contenedor terminó con fallos de
  integración, contexto Spring, privacidad de metadatos, exportación y Telegram.
  No se debe afirmar que estos módulos están validados hasta corregir y repetir
  el build completo.
