# Buzón UCC — Formulario de Quejas Académicas

Plataforma web mobile-first para recibir quejas académicas anónimas o
identificadas, gestionar su seguimiento y generar información administrativa
con privacidad y trazabilidad.

## Objetivo

Facilitar un canal institucional seguro para registrar quejas, proteger la
identidad y las evidencias, permitir la gestión de una cuenta administrativa y
conservar un historial verificable hasta el cierre mensual.

El objetivo se cumplió con el flujo Specify y sus artefactos en
[`specs/002-academic-complaints/`](specs/002-academic-complaints/). El detalle
de arquitectura, módulos, seguridad, datos y operación está en el
[`informe técnico`](docs/informe-tecnico.md).

## Funcionalidades principales

- Formulario público con 11 categorías, modalidad anónima o identificada y
  validaciones de asunto, descripción y número de ubicación.
- **Seguimiento público de radicado en tiempo real**: Los estudiantes consultan
  el estado (`Pendiente`, `Completada`, `No completada`) y la línea de tiempo
  oficial de hitos y observaciones con su código único UUID.
- **Almacenamiento en la nube en Supabase Storage (24/7)**: Evidencias fotográficas
  JPG/PNG sanitizadas de metadatos (EXIF) y guardadas en el bucket `evidencias`.
- **Base de datos PostgreSQL 17.11 en Supabase (24/7)**: Persistencia en la nube
  con migraciones automáticas versionadas Flyway.
- Panel administrativo con filtros, detalle, enlaces a evidencia en la nube y
  exportación Excel con auditoría.
- Estados `Pendiente`, `Completada` y `No completada`, historial inmutable y
  seguimiento mensual optimizado contra problemas de inicialización JPA (`@EntityGraph`).
- Interfaz moderna mobile-first con protección contra desbordamiento de texto
  (`overflow-wrap: anywhere; word-break: break-word`) y formularios compactos.
- Dashboard con estadísticas semanales/mensuales y predicciones claramente
  separadas de los datos reales.
- Reportes Word editables con siete secciones.
- Cuenta administrativa única con sesión, recuperación, lockout y auditoría.
- Telegram desacoplado con OTP, avisos mínimos y comandos de solo lectura.

## Arquitectura resumida

![Arquitectura de Despliegue en la Nube](docs/images/arquitectura-nube.png)

```mermaid
flowchart LR
    Usuario[Estudiante o administrador] --> Frontend[React + Vite]
    Frontend --> Backend[Spring Boot REST /api/v1]
    Backend --> SupabaseDB[(Supabase PostgreSQL 17.11)]
    Backend --> SupabaseStorage[Supabase Storage: evidencias]
    Backend --> IA[IA: proveedor de simulación]
    Backend --> Telegram[Telegram: OTP y consultas de lectura]
```

## Diagrama de clases

```mermaid
classDiagram
    Complaint "1" --> "0..*" Evidence
    Complaint "1" --> "0..*" StatusHistory
    Complaint "1" --> "0..*" MonthlyEvent
    AdministratorAccount "1" --> "0..*" AuditEntry
    AdministratorAccount "1" --> "0..1" TelegramLink
    AdministratorAccount "1" --> "0..*" WordReportDraft
    class Complaint {
      +publicId
      +anonymous
      +category
      +status
      +registeredAt
    }
    class Evidence {
      +fileName
      +mimeType
      +driveFileId
    }
    class StatusHistory {
      +newStatus
      +comment
      +changedAt
    }
    class MonthlyEvent {
      +eventType
      +eventMonth
    }
    class AdministratorAccount
    class AuditEntry
    class TelegramLink
    class WordReportDraft
```

Para el diagrama completo y la explicación de cada capa, consulte
[`docs/informe-tecnico.md`](docs/informe-tecnico.md).

## Prerrequisitos

- Java 21
- Maven 3.9+
- Node.js y npm
- Docker para Compose y validaciones de integración

## Configuración

Copie `infra/.env.example` a un archivo `.env` local y complete los valores
fuera del repositorio. Nunca escriba credenciales, tokens ni claves en el
código o la documentación.

Para desarrollo local, use `AI_PROVIDER=simulation`. Google Drive y Telegram
solo deben considerarse conectados después de configurar sus variables y
ejecutar la validación correspondiente.

## Ejecución

```powershell
cd backend
mvn spring-boot:run
cd ..\frontend
npm install
npm run dev
```

## Comprobaciones reproducibles

```powershell
cd backend
mvn verify
cd ..\frontend
npm ci
npm run lint
npm test
npm run build
cd ..
docker compose -f infra/compose.yaml --env-file infra/.env.example config
```

## Flujo Specify utilizado

Los comandos se ejecutaron en este orden, uno a uno:

1. `/speckit.specify` — crear la especificación funcional.
2. `/speckit.plan` — definir arquitectura y plan técnico.
3. `/speckit.tasks` — generar tareas y dependencias.
4. `/speckit.implement` — implementar las tareas por fases.
5. `/speckit.analyze` — analizar consistencia entre artefactos.
6. `/speckit.checklist` — verificar requisitos y cierre.

> Los nombres correctos son `/speckit.specify` y `/speckit.plan`; no
> `/specifi-plan`.

## Documentación

- [Informe técnico completo](docs/informe-tecnico.md)
- [Especificación](specs/002-academic-complaints/spec.md)
- [Plan técnico](specs/002-academic-complaints/plan.md)
- [Tareas completadas](specs/002-academic-complaints/tasks.md)
- [Validación operativa](specs/002-academic-complaints/quickstart.md)
- [Documentación operativa](docs/README.md)
