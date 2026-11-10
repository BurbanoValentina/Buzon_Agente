# API Contracts: Formulario de Quejas Académicas

The REST API is versioned under `/api/v1`, uses lowercase plural resource
names, JSON DTOs and Spanish user-facing errors. Entities are not exposed
directly.

## Public complaints

- `GET /api/v1/public/form-options` returns the public form configuration and
  11 fixed categories.
- `POST /api/v1/public/complaints` accepts anonymous/identified selection,
  conditional identity fields, category, conditional number, subject,
  description, optional images and privacy acknowledgement.
- The response confirms receipt without exposing administrator data.

## Administrator session and account

- `POST /api/v1/admin/session` authenticates the single administrator.
- `POST /api/v1/admin/session/verification` validates a Telegram OTP when
  Telegram is linked.
- `POST /api/v1/admin/security-question` handles the permitted alternative
  and password recovery question.
- `DELETE /api/v1/admin/session` closes the session.
- `GET/PATCH /api/v1/admin/account` reads or changes administrator settings.

## Complaints and exports

- `GET /api/v1/admin/complaints` lists all complaints and filters by date,
  category, status and user type.
- `GET /api/v1/admin/complaints/{id}` returns permitted details and evidence
  references.
- `PATCH /api/v1/admin/complaints/{id}/status` applies valid transitions and
  required comments, observations or conclusions.
- `GET /api/v1/admin/complaints/export.xlsx` downloads complete or filtered
  complaints with specified columns and evidence links.
- `POST /api/v1/admin/reports/word/draft` creates an editable seven-section
  draft from selected complaints or a category.
- `PATCH /api/v1/admin/reports/word/draft/{id}` saves administrator edits.
- `GET /api/v1/admin/reports/word/draft/{id}/download` downloads the edited
  Word document.

## Dashboard and monthly follow-up

- `GET /api/v1/admin/dashboard` returns stored totals, category/status counts,
  weekly/monthly charts, proportions, leading categories and monthly work.
- The response separates real data from labeled AI estimates and reports when
  the five-complaint minimum is not met.
- `GET /api/v1/admin/follow-up/months` returns completion, reopening and
  re-completion groupings.

## Telegram administration

- `POST /api/v1/admin/telegram/link-challenge` creates the 10-minute
  single-use linking challenge.
- `POST /api/v1/admin/telegram/link` completes or replaces the sole link.
- `DELETE /api/v1/admin/telegram/link` unlinks the current account.
- `PATCH /api/v1/admin/telegram/notifications` enables or disables new
  complaint notices.
- `POST /api/v1/telegram/webhook` accepts only requests with the configured
  webhook secret and routes allowed commands for the linked account.
- Allowed commands are `Ayuda`, `Resumen`, `Pendientes` and `Mes`; all are
  read-only. Unknown or unauthorized requests receive no complaint data.

