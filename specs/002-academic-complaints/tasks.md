---
description: "Tareas de implementación del Formulario de Quejas Académicas"
---

# Tasks: Formulario de Quejas Académicas

**Input**: Design documents from `specs/002-academic-complaints/`

**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md),
[research.md](./research.md), [data-model.md](./data-model.md),
[contracts/](./contracts/), [quickstart.md](./quickstart.md)

**Organization**: Las tareas están agrupadas por historia de usuario. Las
pruebas son obligatorias porque el usuario solicitó cobertura explícita de las
reglas críticas.

**Estado de entrega**: COMPLETADO. Todas las tareas T001-T094 fueron
implementadas y están marcadas como realizadas. Este archivo se conserva como
trazabilidad de los requisitos ejecutados y no como una lista de trabajo
pendiente.

**Nota de trazabilidad**: Las tareas de Setup y las tareas T088-T094 provienen
de la sección 7 de la constitución y del plan. No corresponden directamente a
requisitos FR o criterios SC del spec.

## Phase 1: Setup

**Purpose**: Inicializar la estructura del monorepo y las herramientas de
calidad sin incluir credenciales.

- [X] T001 Crear la estructura `backend/`, `frontend/`, `infra/`, `docs/` y `.github/workflows/` definida en `specs/002-academic-complaints/plan.md`.
- [X] T002 Inicializar `backend/pom.xml` con Java 21, Spring Boot 3.x, Web, Data JPA, Security, Bean Validation, Flyway, PostgreSQL, Swagger/OpenAPI, Apache POI y clientes de Google Drive y Telegram.
- [X] T003 [P] Inicializar `frontend/package.json` con React, Recharts, Vitest y ESLint, usando JavaScript y CSS Modules.
- [X] T004 [P] Crear `infra/.env.example` con nombres de configuración y placeholders seguros, incluyendo `TELEGRAM_BOT_TOKEN` y `TELEGRAM_WEBHOOK_SECRET`, sin valores reales.
- [X] T005 [P] Crear `README.md` con prerrequisitos, configuración mediante `.env`, comandos de backend/frontend, modo simulado de IA y advertencia de que no se deben afirmar integraciones no ejecutadas.
- [X] T006 [P] Configurar `frontend/eslint.config.js` y reglas para JavaScript/React sin rastreadores ni analítica de terceros.
- [X] T007 [P] Configurar `backend/config/checkstyle.xml`, SpotBugs con Find Security Bugs y OWASP Dependency-Check en `backend/pom.xml`.

## Phase 2: Foundational

**Purpose**: Prerrequisitos bloqueantes para todas las historias.

- [X] T008 Crear la migración inicial Flyway en `backend/src/main/resources/db/migration/V1__create_core_schema.sql` para quejas, evidencias, historial, eventos mensuales, cuenta administrativa, auditoría, vínculo Telegram y desafíos protegidos.
- [X] T009 Crear entidades JPA y enums en `backend/src/main/java/com/ucc/complaints/model/` para `Complaint`, `Evidence`, `StatusHistory`, `MonthlyEvent`, `AdministratorAccount`, `TelegramLink`, `SecurityChallenge`, `AuditEntry`, `WordReportDraft` y estados `Pendiente`, `Completada`, `No completada`.
- [X] T010 Crear repositorios Spring Data JPA en `backend/src/main/java/com/ucc/complaints/repository/` con consultas parametrizadas para filtros, estados, períodos mensuales, auditoría y desafíos vigentes.
- [X] T011 [P] Crear configuración de variables de entorno y perfiles en `backend/src/main/resources/application.yml`, sin secretos ni credenciales en el repositorio.
- [X] T012 [P] Implementar manejo uniforme de errores en `backend/src/main/java/com/ucc/complaints/exception/` con `@RestControllerAdvice`, mensajes en español y códigos 400, 401, 403, 404, 409, 429 y 500.
- [X] T013 [P] Implementar auditoría y logging estructurado en `backend/src/main/java/com/ucc/complaints/service/AuditService.java` y `backend/src/main/java/com/ucc/complaints/config/LoggingConfig.java`, excluyendo contenido de quejas, identidad, credenciales y códigos.
- [X] T014 [P] Configurar autenticación de sesión, autorización del único administrador y cierre por inactividad en `backend/src/main/java/com/ucc/complaints/security/`.
- [X] T015 [P] Crear cliente común de errores, estado de carga y sesión expirada en `frontend/src/services/` y `frontend/src/hooks/`.
- [X] T016 Crear configuración base de API versionada bajo `/api/v1` en `backend/src/main/java/com/ucc/complaints/controller/` y cliente HTTP centralizado en `frontend/src/services/apiClient.js`.
- [X] T017 [P] Crear pruebas base de contexto, migraciones y manejo de errores en `backend/src/test/java/com/ucc/complaints/foundation/`.
- [X] T018 [P] Crear configuración de Vitest y pruebas de accesibilidad básica para foco, etiquetas y mensajes en `frontend/src/test/setup.js` y `frontend/src/test/accessibility.test.jsx`.

## Phase 3: User Story 1 - Registrar una queja (Priority: P1) 🎯 MVP

**Goal**: Entregar el formulario público anónimo o identificado con validación
completa, evidencia sanitizada en Google Drive y confirmación de recepción.

**Independent Test**: Desde un navegador móvil se registra una queja anónima o
identificada, se validan categorías/campos/evidencias y se confirma el envío
sin requerir cuenta del estudiante.

### Tests for User Story 1

- [X] T019 [P] [US1] Crear pruebas Vitest en `frontend/src/features/complaints/PublicComplaintForm.test.jsx` para anonimato, identidad condicional, las 11 categorías y el campo número visible solo en Laboratorio y Aulas.
- [X] T020 [P] [US1] Crear pruebas Vitest en `frontend/src/features/complaints/complaintValidation.test.js` para número de máximo 50 caracteres, asunto de 5–200, descripción de 20–500 y contadores visibles.
- [X] T021 [P] [US1] Crear pruebas MockMvc en `backend/src/test/java/com/ucc/complaints/publicapi/PublicComplaintControllerTest.java` para campos obligatorios, selección anónima/identificada y rechazo de identidad en envíos anónimos.
- [X] T022 [P] [US1] Crear pruebas de integración Testcontainers en `backend/src/test/java/com/ucc/complaints/complaint/ComplaintPersistenceIT.java` para estado inicial `Pendiente`, identificador automático y ausencia de identidad anónima.
- [X] T023 [P] [US1] Crear pruebas de límites de evidencia en `backend/src/test/java/com/ucc/complaints/evidence/EvidenceValidationTest.java` para JPG/JPEG/PNG, máximo 5 imágenes y máximo 100 MB por imagen.
- [X] T024 [P] [US1] Crear pruebas de privacidad en `backend/src/test/java/com/ucc/complaints/evidence/ImageMetadataSanitizerTest.java` para eliminar o anonimizar metadatos antes del almacenamiento.

### Implementation for User Story 1

- [X] T025 [P] [US1] Crear DTOs y validadores en `backend/src/main/java/com/ucc/complaints/dto/publiccomplaint/` para usuario, identidad condicional, categoría, número, asunto, descripción, privacidad y evidencia.
- [X] T026 [P] [US1] Crear `ImageMetadataSanitizer` en `backend/src/main/java/com/ucc/complaints/storage/ImageMetadataSanitizer.java` para limpiar o reemplazar metadatos antes de cualquier subida.
- [X] T027 [US1] Crear `GoogleDriveEvidenceStorage` en `backend/src/main/java/com/ucc/complaints/storage/GoogleDriveEvidenceStorage.java` para carpetas/archivos `ID-FECHA-CATEGORIA`, acceso exclusivo del administrador y referencias persistibles.
- [X] T028 [US1] Crear `ComplaintService` en `backend/src/main/java/com/ucc/complaints/service/ComplaintService.java` para validar reglas, eliminar identidad anónima, persistir `Pendiente`, sanitizar evidencia y coordinar Drive.
- [X] T029 [US1] Crear `PublicComplaintController` en `backend/src/main/java/com/ucc/complaints/controller/PublicComplaintController.java` con `GET /api/v1/public/form-options` y `POST /api/v1/public/complaints`.
- [X] T030 [US1] Crear componentes mobile-first en `frontend/src/features/complaints/PublicComplaintForm.jsx`, `frontend/src/features/complaints/PrivacyNotice.jsx` y `frontend/src/features/complaints/EvidenceUploader.jsx`.
- [X] T031 [US1] Implementar mensajes claros, estados de carga/error/confirmación, contadores y controles de accesibilidad en `frontend/src/features/complaints/`.
- [X] T032 [US1] Integrar el envío público y carga de imágenes mediante `frontend/src/services/complaintsApi.js`, sin enviar datos de Telegram ni iniciar sesión del estudiante.

**Checkpoint**: El MVP del formulario público debe ser demostrable y testeable
de forma independiente antes de iniciar las historias administrativas.

## Phase 4: User Story 2 - Consultar y descargar quejas (Priority: P1)

**Goal**: Permitir al administrador listar, filtrar, consultar evidencia y
descargar Excel completo o filtrado con auditoría.

**Independent Test**: Un administrador autenticado consulta filtros, abre una
queja y descarga un Excel con exactamente el conjunto y columnas solicitadas.

### Tests for User Story 2

- [X] T033 [P] [US2] Crear pruebas MockMvc en `backend/src/test/java/com/ucc/complaints/admin/ComplaintQueryControllerTest.java` para filtros por fecha, categoría, estado y tipo de usuario, y rechazo sin autenticación.
- [X] T034 [P] [US2] Crear pruebas de exportación en `backend/src/test/java/com/ucc/complaints/export/ExcelExportServiceTest.java` para columnas, identidad solo cuando corresponde, enlaces Drive y conjunto filtrado.
- [X] T035 [P] [US2] Crear prueba de auditoría en `backend/src/test/java/com/ucc/complaints/audit/DownloadAuditTest.java` para usuario, fecha y hora de cada descarga sin contenido de quejas.

### Implementation for User Story 2

- [X] T036 [P] [US2] Crear DTOs y especificaciones de filtros en `backend/src/main/java/com/ucc/complaints/dto/admin/` y `backend/src/main/java/com/ucc/complaints/repository/ComplaintSpecifications.java`.
- [X] T037 [US2] Implementar consulta y detalle administrador en `backend/src/main/java/com/ucc/complaints/service/ComplaintQueryService.java`, incluyendo referencias de evidencia y campos identificados protegidos.
- [X] T038 [US2] Implementar `ExcelExportService` en `backend/src/main/java/com/ucc/complaints/service/ExcelExportService.java` con Apache POI, columnas del spec y auditoría.
- [X] T039 [US2] Crear `AdminComplaintController` en `backend/src/main/java/com/ucc/complaints/controller/AdminComplaintController.java` para listado, detalle y `export.xlsx`.
- [X] T040 [US2] Crear páginas y componentes en `frontend/src/features/admin/complaints/` para tabla responsive, filtros, detalle y enlaces de evidencia.
- [X] T041 [US2] Crear cliente de listado y descarga en `frontend/src/services/adminComplaintsApi.js`, con estados de carga, error y descarga filtrada.

## Phase 5: User Story 5 - Gestionar cumplimiento y seguimiento mensual (Priority: P1)

**Goal**: Gestionar estados, comentarios obligatorios, reaperturas, fechas
inmutables e historial mensual.

**Independent Test**: El administrador aplica todas las transiciones válidas,
recibe rechazo sin texto obligatorio y consulta el historial mensual completo.

### Tests for User Story 5

- [X] T042 [P] [US5] Crear pruebas de transición en `backend/src/test/java/com/ucc/complaints/status/ComplaintStatusServiceTest.java` para `Pendiente`, `Completada`, `No completada`, reapertura y re-finalización.
- [X] T043 [P] [US5] Crear pruebas de validación en `backend/src/test/java/com/ucc/complaints/status/StatusCommentValidationTest.java` para comentario de gestión, observación de reapertura y conclusión final obligatorios.
- [X] T044 [P] [US5] Crear pruebas de inmutabilidad en `backend/src/test/java/com/ucc/complaints/status/CompletionDateImmutabilityTest.java` para fechas automáticas, anteriores y eventos mensuales conservados.
- [X] T045 [P] [US5] Crear pruebas MockMvc en `backend/src/test/java/com/ucc/complaints/status/StatusControllerTest.java` para autenticación, transiciones inválidas y auditoría de usuario/fecha/hora.

### Implementation for User Story 5

- [X] T046 [P] [US5] Crear DTOs de transición e historial en `backend/src/main/java/com/ucc/complaints/dto/status/`.
- [X] T047 [US5] Implementar `ComplaintStatusService` en `backend/src/main/java/com/ucc/complaints/service/ComplaintStatusService.java` con reglas de transición, comentarios, fechas automáticas e historial inmutable.
- [X] T048 [US5] Implementar seguimiento mensual en `backend/src/main/java/com/ucc/complaints/service/MonthlyFollowUpService.java` con mes de primera finalización, reapertura y nueva finalización.
- [X] T049 [US5] Crear endpoints `PATCH /api/v1/admin/complaints/{id}/status` y `GET /api/v1/admin/follow-up/months` en `backend/src/main/java/com/ucc/complaints/controller/StatusController.java`.
- [X] T050 [US5] Crear componentes de estado, comentarios, historial y resumen mensual en `frontend/src/features/admin/status/`.

## Phase 6: User Story 6 - Acceder y administrar la cuenta (Priority: P1)

**Goal**: Proteger el panel con un único administrador, lockout de 24 horas,
sesión de 10 horas, recuperación y configuración de credenciales.

**Independent Test**: Se prueban autenticación, recuperación, cambio de
credenciales, tres fallos consecutivos, lockout y expiración por inactividad.

### Tests for User Story 6

- [X] T051 [P] [US6] Crear pruebas de seguridad en `backend/src/test/java/com/ucc/complaints/security/AdminAuthenticationTest.java` para único administrador, sin registro, acceso por logo y protección de rutas.
- [X] T052 [P] [US6] Crear pruebas de lockout en `backend/src/test/java/com/ucc/complaints/security/AccountLockoutTest.java` para tres fallos consecutivos y bloqueo de 24 horas en login y pregunta clave.
- [X] T053 [P] [US6] Crear prueba de sesión en `backend/src/test/java/com/ucc/complaints/security/InactivityTimeoutTest.java` para cierre automático después de 10 horas.
- [X] T054 [P] [US6] Crear pruebas MockMvc en `backend/src/test/java/com/ucc/complaints/security/AdminAccountControllerTest.java` para pregunta `¿Qué representante eres?`, formatos `administrator` más período y `RepresentanteIng` más año, y cambios auditados.

### Implementation for User Story 6

- [X] T055 [US6] Implementar `AdminAuthenticationService` en `backend/src/main/java/com/ucc/complaints/security/AdminAuthenticationService.java` con hash de contraseña, lockout, recuperación y límites de sesión.
- [X] T056 [US6] Crear `AdminSessionController` y `AdminAccountController` en `backend/src/main/java/com/ucc/complaints/controller/` para login, logout, pregunta y configuración.
- [X] T057 [US6] Crear interfaz de configuración de administrador en `frontend/src/features/admin/account/` con entrada oculta desde logo, sin registro, mensajes de bloqueo y expiración.
- [X] T058 [US6] Integrar cliente de sesión y cuenta en `frontend/src/services/adminAuthApi.js`, sin credenciales iniciales en código, documentación o repositorio.

## Phase 7: User Story 4 - Analizar estadísticas y predicciones (Priority: P2)

**Goal**: Mostrar datos reales semanales/mensuales y predicciones etiquetadas
solo con el mínimo de cinco quejas correspondiente.

**Independent Test**: El administrador visualiza agregados, meses vacíos y
predicciones total/categoría únicamente al cumplir el umbral.

### Tests for User Story 4

- [X] T059 [P] [US4] Crear pruebas de agregación en `backend/src/test/java/com/ucc/complaints/dashboard/DashboardServiceTest.java` para totales, categorías, estados, proporciones, semanas, meses y gráficas vacías.
- [X] T060 [P] [US4] Crear pruebas de umbral en `backend/src/test/java/com/ucc/complaints/dashboard/PredictionThresholdTest.java` para menos de 5 y al menos 5 quejas por categoría y total.
- [X] T061 [P] [US4] Crear pruebas de privacidad IA en `backend/src/test/java/com/ucc/complaints/ai/AiPrivacyBoundaryTest.java` para que nombre, semestre e imágenes nunca se incluyan en solicitudes.

### Implementation for User Story 4

- [X] T062 [P] [US4] Crear `AiAnalysisProvider`, `SimulationAiAnalysisProvider` y DTOs en `backend/src/main/java/com/ucc/complaints/ai/`, validando propuestas y estimaciones.
- [X] T063 [US4] Implementar agregados y umbral de predicción en `backend/src/main/java/com/ucc/complaints/service/DashboardService.java`.
- [X] T064 [US4] Crear `DashboardController` en `backend/src/main/java/com/ucc/complaints/controller/DashboardController.java` con acceso exclusivo del administrador.
- [X] T065 [US4] Crear dashboard responsive con Recharts en `frontend/src/features/admin/dashboard/`, separando datos reales, estimaciones y meses vacíos.

## Phase 8: User Story 3 - Generar reportes editables (Priority: P2)

**Goal**: Generar, editar y descargar Word con siete secciones, evidencia
local e IA sin identidad ni imágenes.

**Independent Test**: El administrador selecciona quejas, genera un borrador,
lo edita en la página y descarga la versión editada con auditoría.

### Tests for User Story 3

- [X] T066 [P] [US3] Crear pruebas de selección y privacidad en `backend/src/test/java/com/ucc/complaints/report/WordReportServiceTest.java` para selección individual/categoría y exclusión de identidad/imágenes del prompt IA.
- [X] T067 [P] [US3] Crear pruebas Apache POI en `backend/src/test/java/com/ucc/complaints/report/WordDocumentTest.java` para las siete secciones, portada `Reporte`, fecha de descarga, ausencia de logo, edición y evidencia insertada.
- [X] T068 [P] [US3] Crear prueba de auditoría en `backend/src/test/java/com/ucc/complaints/report/ReportAuditTest.java` para generación y descarga sin contenido sensible en auditoría.

### Implementation for User Story 3

- [X] T069 [P] [US3] Crear DTO y persistencia de `WordReportDraft` en `backend/src/main/java/com/ucc/complaints/dto/report/` y `backend/src/main/java/com/ucc/complaints/repository/WordReportDraftRepository.java`.
- [X] T070 [US3] Implementar `WordReportService` en `backend/src/main/java/com/ucc/complaints/service/WordReportService.java` para siete secciones, propuestas IA, edición y evidencia local.
- [X] T071 [US3] Crear `WordReportController` en `backend/src/main/java/com/ucc/complaints/controller/WordReportController.java` con creación, edición y descarga.
- [X] T072 [US3] Crear editor de borrador y descarga en `frontend/src/features/admin/reports/`, con nombres/semestres visibles solo al administrador.

## Phase 9: User Story 7 - Usar Telegram de forma segura (Priority: P1)

**Goal**: Vincular una sola cuenta Telegram, enviar OTP/avisos mínimos y
responder comandos de solo lectura sin filtrar datos.

**Independent Test**: Con mocks, se vincula una cuenta, se validan OTP de 15
segundos, fallback tras tres errores, avisos, comandos y fallo del proveedor.

### Tests for User Story 7

- [X] T073 [P] [US7] Crear pruebas de webhook en `backend/src/test/java/com/ucc/complaints/telegram/TelegramWebhookControllerTest.java` para secreto válido, secreto inválido, cuenta vinculada y cuenta no vinculada.
- [X] T074 [P] [US7] Crear pruebas de códigos en `backend/src/test/java/com/ucc/complaints/telegram/TelegramChallengeServiceTest.java` para vinculación de 10 minutos, OTP de 15 segundos, un solo uso, hash/cifrado y ausencia en logs.
- [X] T075 [P] [US7] Crear pruebas de fallback en `backend/src/test/java/com/ucc/complaints/telegram/TelegramLoginFallbackTest.java` para pregunta clave tras tres códigos incorrectos y bloqueo tras tres respuestas incorrectas.
- [X] T076 [P] [US7] Crear pruebas de privacidad en `backend/src/test/java/com/ucc/complaints/telegram/TelegramPrivacyTest.java` para excluir identidad, asunto, descripción, imágenes, contraseñas y respuestas de seguridad.
- [X] T077 [P] [US7] Crear pruebas de comandos y avisos en `backend/src/test/java/com/ucc/complaints/telegram/TelegramCommandServiceTest.java` para Ayuda, Resumen, Pendientes, Mes, aviso configurable y operaciones solo lectura.
- [X] T078 [P] [US7] Crear prueba de resiliencia en `backend/src/test/java/com/ucc/complaints/telegram/TelegramFailureIsolationTest.java` para que la indisponibilidad de Telegram no impida registrar una queja.

### Implementation for User Story 7

- [X] T079 [P] [US7] Crear `TelegramClient`, `TelegramChallengeService` y DTOs en `backend/src/main/java/com/ucc/complaints/telegram/`, con webhook secret, modo long polling de desarrollo y códigos protegidos.
- [X] T080 [US7] Implementar `TelegramLinkService` en `backend/src/main/java/com/ucc/complaints/service/TelegramLinkService.java` para una cuenta, reemplazo, desvinculación y preferencia de avisos.
- [X] T081 [US7] Implementar `TelegramNotificationService` en `backend/src/main/java/com/ucc/complaints/service/TelegramNotificationService.java` con payload mínimo y entrega desacoplada del registro.
- [X] T082 [US7] Implementar `TelegramCommandService` en `backend/src/main/java/com/ucc/complaints/service/TelegramCommandService.java` para Ayuda, Resumen, Pendientes y Mes, sin mutaciones ni descargas.
- [X] T083 [US7] Crear `TelegramWebhookController` y endpoints de administración en `backend/src/main/java/com/ucc/complaints/controller/TelegramController.java`.
- [X] T084 [US7] Integrar OTP, avisos y vínculo Telegram con autenticación y configuración en `backend/src/main/java/com/ucc/complaints/security/` y `backend/src/main/java/com/ucc/complaints/service/AuditService.java`.
- [X] T085 [US7] Crear configuración de Telegram en `frontend/src/features/admin/telegram/` para vincular, desvincular y activar/desactivar avisos sin mostrar secretos.

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Calidad, entrega, observabilidad, documentación y verificación
transversal.

- [X] T086 [P] Configurar JaCoCo en `backend/pom.xml` con umbral mínimo de cobertura del 80% y fallo del build cuando no se alcance.
- [X] T087 [P] Configurar GitHub Actions en `.github/workflows/ci.yml` para Maven, Vitest, Checkstyle, SpotBugs/Find Security Bugs, ESLint, OWASP Dependency-Check, Testcontainers y artefactos de cobertura.
- [X] T088 [P] Crear Dockerfile multietapa en `infra/Dockerfile` para construir backend/frontend sin copiar `.env` ni secretos.
- [X] T089 [P] Crear `infra/compose.yaml` con aplicación, PostgreSQL y servicio de pruebas, usando variables de entorno externas.
- [X] T090 [P] Crear `.github/workflows/deploy.yml` separado de CI, condicionado al éxito de CI y con verificación de health/readiness posterior al despliegue.
- [X] T091 [P] Añadir métricas y health checks en `backend/src/main/java/com/ucc/complaints/config/ObservabilityConfig.java` para latencia, errores, lockouts, exports, Drive, IA y Telegram sin datos sensibles.
- [X] T092 [P] Añadir pruebas de rendimiento y accesibilidad en `backend/src/test/java/com/ucc/complaints/quality/PerformanceContractTest.java` y `frontend/src/test/accessibilityFlows.test.jsx` para 500 ms de validación, 2 s de consultas, 30 s de exportación y viewport de 320 px.
- [X] T093 Revisar `docs/README.md` y `README.md` contra `specs/002-academic-complaints/quickstart.md`, documentando configuración, comandos, simulación y límites sin credenciales.
- [X] T094 Ejecutar la validación completa de `specs/002-academic-complaints/quickstart.md` y registrar únicamente resultados realmente ejecutados.

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sin dependencias; T003, T004, T005, T006 y T007 pueden
  ejecutarse en paralelo después de T001/T002 cuando compartan sus archivos.
- **Foundational (Phase 2)**: Depende de Setup y bloquea todas las historias.
- **US1 (Phase 3)**: Depende de Foundation; es el MVP y habilita datos para
  consulta, seguimiento, dashboard, reportes y Telegram.
- **US2 (Phase 4)**: Depende de US1 para consultar quejas y evidencias.
- **US5 (Phase 5)**: Depende de US1; puede ejecutarse en paralelo con US2.
- **US6 (Phase 6)**: Depende de Foundation; debe completarse antes de exponer
  funciones administrativas de US2–US5.
- **US4 (Phase 7)**: Depende de US1 y puede ejecutarse en paralelo con US2/US5
  después de Foundation.
- **US3 (Phase 8)**: Depende de US2 y US4 para detalle, filtros y estadísticas.
- **US7 (Phase 9)**: Depende de Foundation y US6; puede integrar US1 para avisos
  de nuevas quejas.
- **Polish (Phase 10)**: Depende de las historias seleccionadas para el release.

### User Story Dependencies

- **US1 (P1)**: Foundation únicamente; MVP independiente.
- **US2 (P1)**: US1 para disponer de quejas.
- **US5 (P1)**: US1; independiente de US2.
- **US6 (P1)**: Foundation; seguridad transversal para las demás historias.
- **US4 (P2)**: US1; requiere datos almacenados.
- **US3 (P2)**: US2 y US4.
- **US7 (P1)**: US6 y Foundation; la notificación de quejas usa US1.

### Parallel Opportunities

- Setup: T003–T007 pueden distribuirse por archivo.
- Foundation: T011–T018 pueden ejecutarse en paralelo tras T008–T010 cuando
  no compartan archivos.
- Tras Foundation: US1, US5 y US6 pueden iniciar en paralelo; US2 y US4
  pueden iniciar cuando US1 tenga sus contratos; US7 puede avanzar con mocks.
- Dentro de US1: T019–T024 son pruebas paralelizables; T025–T027 pueden
  distribuirse por DTO, sanitización y Drive.
- Dentro de US7: T073–T078 son pruebas paralelizables y no usan credenciales.
- Polish: T086–T092 pueden distribuirse por herramienta/archivo.

## Parallel Example: MVP User Story 1

```text
Task: T019 PublicComplaintForm.test.jsx
Task: T020 complaintValidation.test.js
Task: T021 PublicComplaintControllerTest.java
Task: T023 EvidenceValidationTest.java
Task: T024 ImageMetadataSanitizerTest.java
```

Estas pruebas pueden prepararse en paralelo; la implementación T025–T032 se
integra después en el mismo contrato.

## Parallel Example: Telegram User Story 7

```text
Task: T073 TelegramWebhookControllerTest.java
Task: T074 TelegramChallengeServiceTest.java
Task: T076 TelegramPrivacyTest.java
Task: T077 TelegramCommandServiceTest.java
Task: T078 TelegramFailureIsolationTest.java
```

Todas usan mocks y no requieren un token real.

## Implementation Strategy

### MVP First

1. Completar Setup y Foundation.
2. Implementar US1: formulario público, validación, evidencia sanitizada y
   almacenamiento Drive.
3. Ejecutar las pruebas de US1 y detenerse para validar el flujo independiente.
4. Continuar con US6 y US2 para habilitar gestión segura del administrador.

### Incremental Delivery

1. Añadir seguimiento de estados (US5) y dashboard/predicciones (US4).
2. Añadir reportes Word editables (US3).
3. Añadir Telegram (US7) con mocks, OTP, avisos y comandos de solo lectura.
4. Ejecutar Polish y gates de CI/CD antes de cualquier despliegue.

### Quality Rule

No se deben afirmar integraciones, pruebas, cobertura, despliegues o health
checks como exitosos hasta ejecutar los comandos correspondientes y conservar
su resultado.

## Cierre de requisitos

La implementación cubre el formulario público, la consulta y exportación
administrativa, el seguimiento de estados, la cuenta única de administración,
el dashboard con predicciones, los reportes Word editables, Telegram seguro y
los controles transversales de calidad, privacidad, auditoría y despliegue.

Todas las tareas de este documento están completadas (`[X]`). La evidencia
operativa y los comandos reproducibles se mantienen en
[`quickstart.md`](./quickstart.md), [`README.md`](../../README.md) y el
[`informe técnico`](../../docs/informe-tecnico.md).
