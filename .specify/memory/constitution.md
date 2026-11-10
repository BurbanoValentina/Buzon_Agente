<!--
Sync Impact Report
- Version change: 2.1.0 -> 2.1.2
- Modified principles: Stack Tecnológico and Gobernanza.
- Added sections: Clarification that plan quality objectives are binding quality
  obligations and not functional requirements.
- Removed sections: None.
- Follow-up TODOs: None.
-->

# Constitución del Proyecto: Formulario de Quejas Académico

## 1. Naturaleza del Proyecto

Este proyecto es una aplicación **web de quejas académicas** destinada a los
**estudiantes de la facultad** (que registran quejas desde su celular, de forma
anónima o identificada) y a **un único administrador** (el representante), que
las gestiona. Su propósito es **permitir registrar quejas con evidencia, y dar
al administrador herramientas para consultarlas, descargarlas, analizarlas con
apoyo de inteligencia artificial y darles seguimiento mensual hasta su
cumplimiento**.

En este proyecto existe un único tipo de registro, la «queja»: un reclamo se
trata exactamente igual. La IA es una herramienta de apoyo y no sustituye el
criterio del administrador.

## 2. Stack Tecnológico (Reglas de Implementación)

- **Frontend / UI:** React con HTML5, CSS3 y JavaScript. Diseño responsive,
  pensado primero para celular, y componentes reutilizables. Estilos con CSS3 /
  CSS Modules; no se añaden frameworks de estilos sin autorización. Gráficos
  con Recharts. Prohibido incluir rastreadores publicitarios o analítica de
  terceros.
- **Backend:** Java 21 (LTS), Spring Boot 3.x, API REST, Spring Data JPA,
  Spring Security y Bean Validation. Gestión de dependencias con Maven. API
  documentada con Swagger/OpenAPI.
- **Base de Datos:** PostgreSQL. Acceso a datos únicamente mediante Spring Data
  JPA; prohibido construir consultas concatenando texto de entrada del usuario.
  El esquema se versiona con migraciones (Flyway) y debe incluir restricciones
  de integridad.
- **Almacenamiento de evidencias:** Google Drive del administrador, mediante su
  API. Las imágenes no se guardan en la base de datos.
- **Generación de archivos:** los archivos de Excel y Word se generan en el
  backend con Apache POI.
- **Inteligencia Artificial:** se invoca únicamente desde el backend, detrás
  de una interfaz (`AiAnalysisProvider`) que permita cambiar de proveedor sin
  rehacer la aplicación. Debe existir un **modo de simulación** para
  desarrollo local. Las salidas se validan antes de usarse.
- **Integración con Telegram:** se usa la Telegram Bot API oficial en un módulo
  independiente del backend, mediante webhook con secreto validado o long
  polling únicamente en desarrollo. El bot es exclusivo del administrador y
  respeta las funciones y límites definidos en las reglas de dominio.
- **Lenguaje:** Java en el backend y JavaScript en el frontend. Los scripts SQL
  solo se usan en migraciones.
- **Herramientas complementarias:** Git y GitHub, JUnit y Mockito para pruebas.
  Docker es obligatorio para construir la imagen en CI y para ejecutar el
  `compose.yaml` local.
- **Secretos:** credenciales, el token y secreto del webhook de Telegram, claves
  de la IA y de Google Drive, y la respuesta a la pregunta clave solo se
  configuran mediante variables de entorno o un archivo `.env` excluido del
  repositorio. Nunca se escriben en el código, en la documentación ni en el
  repositorio.

## 3. Reglas de Dominio y Lógica de Negocio

Estas reglas son obligatorias. El agente de IA debe respetarlas estrictamente:

- **Anonimato opcional y respetado:** el estudiante elige entre enviar la queja
  como anónimo o identificado. Si elige anónimo, el sistema no solicita,
  almacena ni vincula nombre, semestre ni ningún dato de identidad con la
  queja. Si elige identificado, el nombre y el semestre son obligatorios y
  solo los ve el administrador. No se almacenan direcciones IP ni
  identificadores persistentes innecesarios.
- **Acceso público sin cuenta:** los estudiantes envían quejas sin registrarse
  ni iniciar sesión. La pantalla pública muestra solo el logo y el formulario.
- **Categorías fijas:** Infraestructura, Bienestar, Laboratorio, Profesores,
  Estudiantes, Horarios, Tecnología, Administrativos, Convivencia, Aulas y
  Otras situaciones. Laboratorio y Aulas exigen un número (texto de hasta 50
  caracteres).
- **Límites del formulario:** asunto de 5 a 200 caracteres y descripción de 20
  a 500 caracteres.
- **Evidencia:** solo imágenes JPG o PNG, máximo 5 por queja y 100 MB cada una.
  Los metadatos de las imágenes se eliminan o se reemplazan automáticamente por
  un valor anónimo al recibirlas. Las imágenes se guardan en el Google Drive
  del administrador, en carpetas nombradas con el formato ID-FECHA-CATEGORIA
  (fecha de registro), y solo el administrador accede a ellas.
- **Estados:** toda queja nueva comienza en Pendiente. El administrador la
  cambia a Completada o No completada, y una queja No completada puede volver a
  Pendiente o pasar a Completada. Cada cambio de estado se registra con usuario,
  fecha y hora.
- **Cumplimiento y reapertura:** para completar una queja es obligatorio un
  comentario de gestión, y la fecha de cumplimiento se registra
  automáticamente y no puede modificarse. Una queja completada puede reabrirse
  con una observación obligatoria del motivo, y al finalizarla de nuevo se
  exige una conclusión de lo que cambió. Las fechas anteriores se conservan en
  el historial.
- **Seguimiento mensual:** las quejas completadas se organizan por el mes en
  que se completan y se conserva el historial de meses anteriores.
- **La IA solo apoya:** no decide si una queja es verdadera, no sanciona, no
  acusa ni determina culpabilidades, no inventa datos y no presenta hipótesis
  como hechos. Los textos que genera son propuestas que el administrador
  revisa.
- **Protección de datos hacia la IA:** al servicio de IA nunca se envían
  nombres, semestres ni imágenes.
- **Estadísticas y predicciones reales:** las cifras se calculan solo con los
  datos almacenados. Las predicciones se identifican siempre como estimaciones
  de IA y se generan únicamente cuando hay al menos 5 quejas (en la categoría,
  para la predicción por categoría; en total, para la predicción del volumen
  total).
- **Acceso:** existe un único usuario administrador. No existe registro de
  nuevos usuarios ni otros roles. El acceso al panel es mediante un botón
  oculto en el logo, pero la autenticación siempre es obligatoria. Las
  funciones de Excel, Word, dashboard y configuración son exclusivas del
  administrador.
- **Seguridad de la cuenta:** tras 3 intentos fallidos consecutivos (inicio de
  sesión o pregunta clave) el acceso se bloquea 24 horas, y la sesión se
  cierra tras 10 horas de inactividad. La recuperación de contraseña solo es
  posible respondiendo la pregunta clave. Además, el inicio de sesión y todo
  cambio de contraseña exigen un código de verificación de un solo uso,
  enviado por el bot de Telegram vinculado, que caduca a los 15 segundos. Si
  con usuario y contraseña correctos el código se escribe mal 3 veces seguidas,
  se muestra la pregunta clave como entrada alternativa: responderla bien
  completa el inicio de sesión y 3 respuestas incorrectas bloquean el acceso
  24 horas. El uso de la entrada alternativa se registra. Mientras no haya
  una cuenta de Telegram vinculada, el inicio de sesión y el cambio de
  contraseña solo requieren usuario y contraseña. Los códigos de verificación
  y vinculación se guardan únicamente de forma protegida, nunca en claro, y
  no se escriben en los registros.
- **Telegram:** el bot es de uso exclusivo del administrador, mediante una
  cuenta vinculada con un código de un solo uso que caduca a los 10 minutos.
  Envía códigos de verificación de inicio de sesión y cambio de contraseña,
  avisa de quejas nuevas con identificador, categoría, número, tipo de usuario
  y fecha, y responde comandos de solo lectura: ayuda, resumen, pendientes y
  mes. Nunca envía asunto, descripción, nombre, semestre ni imágenes; no
  permite acciones de gestión ni descargas; no recibe ni solicita contraseñas;
  y no se usa con estudiantes. Ningún dato de Telegram se asocia con las
  quejas. Las demás cuentas reciben un mensaje genérico sin datos. Un fallo de
  Telegram no impide registrar la queja.
- **Trazabilidad:** se registran las descargas de Excel, la generación de
  reportes, los cambios de estado, los intentos de acceso, los bloqueos y los
  cambios de credenciales, sin guardar el contenido de las quejas ni datos de
  identidad en esos registros.
- **Privacidad:** antes del envío se muestra un aviso sobre el tratamiento de
  los datos y las evidencias, conforme a la Ley 1581 de 2012 y las políticas de
  la UCC. El anonimato puede verse limitado si el estudiante incluye datos
  personales o circunstancias que lo identifiquen en el texto o en las
  imágenes.

## 4. Estructura y Estilo de Código

- **Estructura:** monorepo con `/frontend` (React), `/backend` (Spring Boot),
  `/db` (migraciones y scripts) y `/docs`. En el backend, la organización es
  por capas: `controller`, `service`, `repository`, `model`, `dto`, `config`,
  `security`, `exception`, `ai` (proveedor de IA), `storage` (Google Drive) y
  `telegram` (integración con Telegram).
- **Estilo:** código simple y legible, adecuado a un proyecto académico. Cada
  capa tiene una única responsabilidad: los controladores no contienen lógica
  de negocio ni acceden a repositorios; las entidades no se exponen directamente
  en la API (se usan DTO). En el frontend, componentes funcionales con hooks y
  la comunicación con la API centralizada en una capa de servicios.
- **Nomenclatura:** identificadores de código en inglés; textos de la interfaz
  y documentación en español. Java: clases en `PascalCase`, métodos y
  variables en `camelCase`, constantes en `UPPER_SNAKE_CASE`. Base de datos:
  `snake_case`. React: componentes en `PascalCase.jsx`, hooks con prefijo `use`.
  Endpoints REST bajo `/api/v1`, en minúsculas y plural.

## 5. Manejo de Errores y Validaciones

- **UI:** mensajes claros, amables y accionables en español. Nunca se muestran
  trazas, rutas internas, credenciales ni detalles técnicos. Se contemplan
  estados de carga, error y ausencia de resultados (por ejemplo, gráficas
  vacías cuando no hay quejas).
- **Backend:** toda entrada se valida con Bean Validation, incluidas las
  longitudes, el tipo y el tamaño de las imágenes, y se sanea para prevenir
  inyección SQL y XSS. El manejo de excepciones es centralizado
  (`@RestControllerAdvice`) con respuestas JSON uniformes y los códigos HTTP
  adecuados: `400` datos inválidos, `401` sin autenticación, `403` sin permisos,
  `404` recurso inexistente, `409` conflicto de estado, `429` demasiadas
  solicitudes o cuenta bloqueada, y `500` error interno con mensaje genérico.
  Los registros (logs) no deben contener el contenido de las quejas,
  credenciales ni datos de identidad. Si la IA o Google Drive fallan, se
  informa al usuario sin exponer detalles técnicos y no se pierde la queja.
- **Objetivos de calidad:** los objetivos de rendimiento, accesibilidad y
  observabilidad definidos en `plan.md` son obligaciones de calidad para la
  implementación y su validación. No constituyen requisitos funcionales ni
  amplían por sí mismos el alcance funcional aprobado.

## 6. Comportamiento del Agente de IA (Reglas SDD)

- **Cero Código Sombra (Shadow Code):** Implementar únicamente lo definido en
  las especificaciones aprobadas (`spec.md`). No añadir funcionalidades,
  dependencias ni complejidad «por si acaso».
- **Fuente de la Verdad:** Si una solicitud contradice esta constitución o
  aparece una inconsistencia entre reglas y especificaciones, detener la
  implementación, explicar el conflicto y solicitar la actualización del
  documento correspondiente antes de modificar el código.
- **Cambios de stack:** no modificar las tecnologías autorizadas sin
  justificarlo y obtener aprobación previa.
- **Honestidad sobre integraciones:** no afirmar que Google Drive, la IA o
  cualquier servicio externo está conectado si no se ha configurado realmente.
  Documentar las variables de entorno necesarias y ofrecer el modo de
  simulación cuando sea posible.
- **Secretos:** nunca escribir credenciales, claves ni respuestas de seguridad
  en el código, en los documentos o en el repositorio.
- **Entregas completas:** entregar archivos completos, con su ruta, y mantener
  coherencia entre entidades, servicios, repositorios, controladores y
  componentes del frontend.
- **Privacidad ante todo:** ante la duda entre una funcionalidad y la
  protección de la identidad de los estudiantes, prevalece la protección y el
  agente debe consultar antes de continuar.

## Gobernanza

Esta constitución tiene prioridad sobre prácticas, especificaciones y
decisiones de implementación. Toda modificación debe documentar su motivo, los
principios afectados, el impacto de compatibilidad, las acciones de adopción y
la evidencia de revisión. El propietario del proyecto debe aprobar el cambio
antes de que rija nuevas implementaciones.

Las versiones usan semver: un cambio MAJOR redefine o elimina reglas
incompatibles; un cambio MINOR agrega o amplía reglas; un cambio PATCH aclara
redacción sin alterar el significado normativo. Cada revisión del proyecto y
pull request debe verificar privacidad, autorización, validación, auditoría,
secretos, integridad de datos, pruebas y coherencia con las especificaciones.

**Version**: 2.1.2 | **Ratified**: 2026-11-04 | **Last Amended**: 2026-11-04
