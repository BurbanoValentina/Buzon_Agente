# Data Model: Formulario de Quejas Académicas

## Complaint

The single complaint record, treating reclamo and queja identically.

| Field | Constraints |
|---|---|
| id | Automatically generated, externally safe identifier |
| user_type | Required: `anonymous` or `identified` |
| name | Required only when identified; administrator-only; absent for anonymous complaints |
| semester | Required only when identified; administrator-only; absent for anonymous complaints |
| category | Required; one of 11 fixed categories |
| location_number | Required only for Laboratorio or Aulas; up to 50 characters; hidden otherwise |
| subject | Required; 5–200 characters; treated as text |
| description | Required; 20–500 characters; treated as text |
| registered_at | Automatic registration date/time |
| status | Starts `Pendiente`; `Completada` or `No completada` according to transition rules |
| completion_date | Automatic and immutable for each completion event |

## Evidence

Optional JPG/JPEG or PNG image, maximum five per complaint and 100 MB per
image. Metadata is removed or anonymized before upload. The Drive folder and
files use `ID-FECHA-CATEGORIA`, where `FECHA` is the registration date, and
only the administrator can access them.

## StatusHistory and MonthlyEvent

Immutable record of every status change, including administrator, date/time,
previous status, new status and required comment, reopening observation or
final conclusion. Completion and reopening dates remain immutable. Monthly
events classify first completion, reopening and re-completion by their actual
event month.

Valid behavior:

- New complaint: `Pendiente`.
- `Pendiente` -> `Completada` or `No completada`.
- `No completada` -> `Pendiente` or `Completada`.
- `Completada` -> reopened `Pendiente`, requiring a reason observation.
- Reopened complaint -> `Completada`, requiring a final conclusion.

## AdministratorAccount

The only panel account. It has username, password hash, security-question
answer protection, lockout counters/timestamps, last activity and audit-safe
configuration state. Initial credentials are injected separately and never
stored in source, documentation or repository.

## TelegramLink

At most one link to the administrator's Telegram account, with provider-side
account identifier, linked/unlinked timestamps, notification preference and
status. Telegram identifiers are not associated with complaints or student
identity.

## SecurityChallenge

A purpose-specific linking or verification challenge with protected value,
purpose, issued time, expiry, attempt/use state and audit reference. Linking
challenges expire after 10 minutes; login/password challenges expire after
15 seconds. Cleartext code values are never persisted or logged.

## AuditEntry

Immutable event for successful/failed access, lockout, inactivity logout,
credential changes, alternative-question use, Telegram linking/unlinking,
notification changes, challenge send/validation, status changes, Excel
downloads and Word report generation. It includes actor, timestamp, event type
and safe resource reference without complaint content, identity or secrets.

## WordReportDraft

Administrator-selected report draft containing portada, resumen, detalle,
estadísticas, completado, no completado and conclusiones. It is editable before
Word download. Names, semesters and evidence are inserted into the document
without being sent to AI.

## DashboardSnapshot

Stored-data aggregates for totals, category/status counts, leading categories,
anonymous/identified proportion, weekly/monthly charts and monthly work. It
may include clearly labeled AI estimates for next-week and next-month
category/total volumes only when the relevant count is at least five. Empty
months produce empty charts.
