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

