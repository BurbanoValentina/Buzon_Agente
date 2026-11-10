# Documentación operativa

La guía reproducible está en
[`specs/002-academic-complaints/quickstart.md`](../specs/002-academic-complaints/quickstart.md).

## Configuración

- Copie `infra/.env.example` fuera del repositorio o use variables de entorno.
- No almacene tokens, contraseñas, claves de Google Drive ni secretos Telegram.
- `AI_PROVIDER=simulation` permite pruebas locales deterministas.
- Telegram permanece deshabilitado salvo que se configure `TELEGRAM_ENABLED=true`.

## Comandos principales

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

## Límites

La aplicación admite una cuenta administrativa, quejas académicas, imágenes JPG/JPEG/PNG
de hasta 100 MB, máximo cinco imágenes, exportaciones y módulo Telegram independiente.
