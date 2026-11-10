# Feature Specification: Formulario de Quejas Académicas

**Feature Branch**: `002-academic-complaints`

**Created**: 2026-11-04

**Status**: Draft

**Input**: Descripción íntegra del texto adjunto sobre el Formulario de
Quejas (contexto académico).

## User Scenarios & Testing

### User Story 1 - Registrar una queja (Priority: P1)

Como estudiante, quiero registrar una queja desde mi celular, de forma anónima
o identificada, para comunicar una situación académica con la información y
evidencia necesarias.

**Why this priority**: El registro de quejas es la capacidad central del
servicio.

**Independent Test**: Un estudiante puede abrir la URL pública, completar una
queja válida, adjuntar evidencia opcional, aceptar el aviso de privacidad y
recibir confirmación.

**Acceptance Scenarios**:

1. **Given** el formulario público, **When** el estudiante selecciona
   anónimo, **Then** no se muestran ni se almacenan nombre, semestre ni otros
   datos de identidad.
2. **Given** el formulario público, **When** el estudiante selecciona
   identificado, **Then** se muestran nombre y semestre, ambos son obligatorios
   y solo el administrador puede verlos.
3. **Given** una categoría distinta de Laboratorio o Aulas, **When** el
   estudiante completa el formulario, **Then** el campo número permanece oculto
   y no se solicita.
4. **Given** la categoría Laboratorio o Aulas, **When** el estudiante intenta
   enviar, **Then** el número es obligatorio y acepta como máximo 50 caracteres.
5. **Given** una queja válida con asunto de 5 a 200 caracteres y descripción de
   20 a 500 caracteres, **When** el estudiante acepta el aviso y envía,
   **Then** la queja se recibe y se muestra una confirmación.
6. **Given** imágenes de evidencia opcionales, **When** son JPG/JPEG o PNG,
   hasta cinco y de máximo 100 MB cada una, **Then** se reciben y se almacenan
   en la ubicación de evidencia del administrador.
7. **Given** un campo obligatorio inválido o evidencia no permitida, **When**
   el estudiante envía, **Then** el formulario rechaza el envío y muestra un
   mensaje claro.

---

### User Story 2 - Consultar y descargar quejas (Priority: P1)

Como administrador, quiero consultar todas las quejas, filtrarlas y
descargarlas, para revisar la información y la evidencia registrada.

**Why this priority**: La consulta controlada permite gestionar las quejas
recibidas.

**Independent Test**: El administrador autenticado puede listar, filtrar,
abrir y descargar todas las quejas o solo las filtradas en Excel.

**Acceptance Scenarios**:

1. **Given** el administrador autenticado, **When** abre el listado, **Then**
   puede consultar todas las quejas y filtrar por fecha, categoría, estado y
   tipo de usuario.
2. **Given** una queja abierta, **When** el administrador consulta su detalle,
   **Then** ve sus datos, la evidencia y, si corresponde, nombre y semestre.
3. **Given** filtros aplicados, **When** el administrador descarga Excel,
   **Then** el archivo contiene únicamente las quejas filtradas.
4. **Given** una descarga de Excel, **When** finaliza, **Then** queda registrada
   con usuario, fecha y hora.
5. **Given** una carpeta o archivo de evidencia, **When** se crea, **Then** su
   nombre usa `ID-FECHA-CATEGORIA`, donde la fecha es la de registro.

---

### User Story 3 - Generar reportes editables (Priority: P2)

Como administrador, quiero generar un reporte Word con ayuda de IA, editarlo
antes de descargarlo y conservar la evidencia, para comunicar resultados sin
delegar decisiones a la IA.

**Why this priority**: Los reportes reúnen información y análisis para la
gestión administrativa.

**Independent Test**: El administrador selecciona quejas, genera un reporte con
las siete secciones, edita el contenido y descarga un documento Word.

**Acceptance Scenarios**:

1. **Given** el administrador autenticado, **When** selecciona quejas
   individuales o por categoría, **Then** puede generar un reporte con portada,
   resumen, detalle, estadísticas, completado, no completado y conclusiones.
2. **Given** un reporte generado, **When** el administrador lo revisa, **Then**
   la portada dice “Reporte”, muestra la fecha de descarga y no contiene logo.
3. **Given** quejas identificadas y evidencias seleccionadas, **When** se
   genera el detalle, **Then** muestra nombre, semestre e imágenes sin enviarlos
   al servicio de IA.
4. **Given** contenido generado por IA, **When** el administrador lo edita y
   descarga, **Then** el documento contiene la versión editada.
5. **Given** una generación de reporte, **When** finaliza, **Then** queda
   registrada.
6. **Given** datos insuficientes o propuestas de IA, **When** se redactan
   conclusiones, **Then** se presentan como propuestas que requieren evaluación
   humana y no como hechos inventados.

---

### User Story 4 - Analizar estadísticas y predicciones (Priority: P2)

Como administrador, quiero consultar estadísticas reales y predicciones
identificadas como estimaciones, para analizar la evolución de las quejas.

**Why this priority**: El análisis permite conocer tendencias y revisar el
trabajo realizado.

**Independent Test**: El administrador consulta gráficos semanales y mensuales,
ve datos almacenados, predicciones cuando cumplen el mínimo requerido y el
resumen mensual.

**Acceptance Scenarios**:

1. **Given** quejas almacenadas, **When** el administrador abre el dashboard,
   **Then** ve totales, cantidades por categoría y estado, categorías con más
   quejas y proporción anónimas/identificadas.
2. **Given** un período semanal o mensual, **When** el administrador cambia el
   período, **Then** las gráficas muestran los datos de ese período.
3. **Given** al menos cinco quejas de una categoría, **When** se consulta la
   predicción, **Then** se muestra la estimación de IA de esa categoría para la
   próxima semana y el próximo mes, separada de datos reales.
4. **Given** menos de cinco quejas de una categoría o en total, **When** se
   consulta la predicción correspondiente, **Then** no se muestra y se indica
   que no se cumple el mínimo.
5. **Given** un mes sin quejas, **When** se consulta, **Then** sus gráficas
   aparecen vacías.

---

### User Story 5 - Gestionar cumplimiento y seguimiento mensual (Priority: P1)

Como administrador, quiero cambiar el estado de las quejas y consultar su
cumplimiento por mes, para dar seguimiento hasta su resolución.

**Why this priority**: El seguimiento es necesario para saber qué trabajo se
realizó y qué casos continúan pendientes.

**Independent Test**: El administrador cambia estados válidos con comentarios
obligatorios, reabre una queja y consulta el historial mensual.

**Acceptance Scenarios**:

1. **Given** una queja nueva, **When** se registra, **Then** inicia en
   `Pendiente`.
2. **Given** una queja pendiente, **When** el administrador la cambia,
   **Then** puede establecer `Completada` o `No completada`.
3. **Given** una queja que se marca como completada, **When** falta el
   comentario de gestión, **Then** el cambio se rechaza; con comentario, se
   registra automáticamente la fecha de cumplimiento inmodificable.
4. **Given** una queja no completada, **When** el administrador la actualiza,
   **Then** puede volver a `Pendiente` o pasar a `Completada`.
5. **Given** una queja completada, **When** el administrador la reabre, **Then**
   debe registrar una observación obligatoria y la fecha de reapertura.
6. **Given** una queja reabierta, **When** vuelve a completarse, **Then** debe
   registrar una conclusión obligatoria y una nueva fecha automática.
7. **Given** cambios de estado y reaperturas, **When** se consulta el historial,
   **Then** se conservan las fechas anteriores y cada cambio incluye usuario,
   fecha y hora.
8. **Given** el resumen mensual, **When** se consulta, **Then** una primera
   finalización se cuenta en su mes de cumplimiento, una reapertura en su mes
   de reapertura y una nueva finalización en su mes de finalización.

---

### User Story 6 - Acceder y administrar la cuenta (Priority: P1)

Como administrador único, quiero acceder de forma protegida y actualizar mis
credenciales, para gestionar exclusivamente el panel.

**Why this priority**: El panel contiene información y acciones exclusivas del
administrador.

**Independent Test**: El administrador abre el acceso desde el logo, inicia
sesión, recupera o cambia sus credenciales y verifica bloqueos y cierre por
inactividad.

**Acceptance Scenarios**:

1. **Given** la pantalla pública, **When** el administrador presiona el logo,
   **Then** se abre el inicio de sesión sin opción de registro.
2. **Given** una persona no autenticada, **When** intenta abrir el panel,
   **Then** el acceso es rechazado.
3. **Given** tres intentos fallidos consecutivos de inicio de sesión o pregunta
   clave, **When** se alcanza el límite, **Then** el acceso se bloquea por 24
   horas.
4. **Given** la recuperación de contraseña, **When** se responde correctamente
   “¿Qué representante eres?”, **Then** se permite cambiar la contraseña
   conforme al formato definido.
5. **Given** el administrador autenticado, **When** abre configuración, **Then**
   puede cambiar usuario, contraseña, pregunta clave y respuesta.
6. **Given** diez horas de inactividad, **When** transcurre el plazo, **Then**
   la sesión se cierra automáticamente.
7. **Given** accesos, bloqueos, cierres y cambios de credenciales, **When**
   ocurren, **Then** quedan registrados sin guardar contenido de quejas ni
   datos de identidad en esos registros.

---

### User Story 7 - Usar Telegram de forma segura (Priority: P1)

Como administrador único, quiero vincular una cuenta de Telegram para recibir
verificaciones y avisos mínimos, y consultar resúmenes sin exponer información
privada ni gestionar quejas desde el bot.

**Why this priority**: Telegram refuerza el acceso del administrador y permite
consultas operativas limitadas sin ampliar los roles ni el acceso de estudiantes.

**Independent Test**: El administrador vincula una cuenta, recibe un código y
un aviso permitido, ejecuta comandos de solo lectura y reemplaza la cuenta.

**Acceptance Scenarios**:

1. **Given** el administrador autenticado en configuración, **When** solicita
   vincular Telegram, **Then** recibe un código de un solo uso que vence en
   10 minutos y puede completar la vinculación desde el bot.
2. **Given** una cuenta vinculada, **When** el administrador vincula otra,
   **Then** la anterior deja de estar vinculada.
3. **Given** avisos activos, **When** se registra una queja, **Then** la cuenta
   vinculada recibe únicamente identificador, categoría, número si aplica,
   tipo de usuario y fecha.
4. **Given** la cuenta vinculada, **When** envía Ayuda, Resumen, Pendientes o
   Mes, **Then** recibe solo la información permitida para ese comando.
5. **Given** una cuenta no vinculada o un comando desconocido, **When** envía
   un mensaje, **Then** recibe una respuesta genérica o la lista de comandos
   sin datos de quejas.
6. **Given** usuario y contraseña correctos con Telegram vinculado, **When**
   el administrador introduce un código de verificación válido, **Then** se
   completa el inicio de sesión; el código vence a los 15 segundos y solo se
   usa una vez.
7. **Given** tres códigos de verificación incorrectos consecutivos, **When**
   ocurre el tercer fallo, **Then** se muestra la pregunta clave como entrada
   alternativa; tres respuestas incorrectas bloquean el acceso por 24 horas.
8. **Given** Telegram no disponible al registrar una queja, **When** finaliza
   el envío, **Then** la queja se registra y el estudiante no ve el fallo del
   aviso.

### Edge Cases

- Se rechaza una evidencia que no sea JPG/JPEG o PNG, supere 100 MB o exceda
  cinco imágenes.
- Se eliminan o anonimizan automáticamente los metadatos de cada imagen.
- Una queja anónima nunca queda vinculada con nombre, semestre o identidad.
- Laboratorio y Aulas requieren número; las demás categorías no lo muestran.
- Una queja puede permanecer Pendiente cuando es grave o de resolución lenta.
- Una fecha de cumplimiento no puede ser modificada manualmente.
- Una predicción no se muestra con menos de cinco quejas en su ámbito.
- La IA falla o no tiene datos suficientes: la queja y los datos almacenados se
  conservan y la propuesta no se presenta como un hecho.
- Telegram no está disponible al registrar una queja: el registro se confirma
  sin mostrar el error de notificación al estudiante.
- Un código de vinculación o verificación vencido o ya utilizado se rechaza y
  se puede solicitar otro.
- Telegram no recibe ni procesa contraseñas, respuestas de seguridad, cambios
  de estado, descargas ni solicitudes de reportes.
- Los datos enviados por Telegram no se asocian con las quejas ni modifican el
  anonimato de los estudiantes.

## Requirements

### Functional Requirements

- **FR-001**: El sistema MUST permitir acceder al formulario público sin cuenta
  ni inicio de sesión.
- **FR-002**: El formulario MUST mostrar únicamente el logo y sus campos.
- **FR-003**: El formulario MUST permitir seleccionar usuario anónimo o
  identificado.
- **FR-004**: Para usuario identificado, nombre y semestre MUST ser
  obligatorios y visibles solo para el administrador.
- **FR-005**: Para usuario anónimo, el sistema MUST no solicitar, almacenar ni
  vincular nombre, semestre u otros datos de identidad.
- **FR-006**: La categoría MUST ser obligatoria y ofrecer Infraestructura,
  Bienestar, Laboratorio, Profesores, Estudiantes, Horarios, Tecnología,
  Administrativos, Convivencia, Aulas y Otras situaciones.
- **FR-007**: Laboratorio y Aulas MUST mostrar un número obligatorio de máximo
  50 caracteres; las demás categorías MUST mantenerlo oculto.
- **FR-008**: El asunto MUST ser obligatorio y tener entre 5 y 200 caracteres.
- **FR-009**: La descripción MUST ser obligatoria y tener entre 20 y 500
  caracteres.
- **FR-010**: Los campos con límites MUST mostrar contador visible.
- **FR-011**: La evidencia MUST aceptar solo JPG/JPEG o PNG, máximo cinco
  imágenes y máximo 100 MB por imagen.
- **FR-012**: El sistema MUST eliminar o reemplazar por un valor anónimo los
  metadatos de cada imagen al recibirla.
- **FR-013**: La evidencia MUST almacenarse en Google Drive del administrador,
  en carpetas con formato `ID-FECHA-CATEGORIA`, y solo el administrador podrá
  acceder.
- **FR-014**: El sistema MUST mostrar aviso de privacidad sobre datos y
  evidencia antes del envío y confirmar la recepción.
- **FR-015**: El sistema MUST validar campos obligatorios, longitudes, tipos y
  tamaños, mostrando errores claros.
- **FR-016**: El sistema MUST permitir al administrador consultar todas las
  quejas y filtrar por fecha, categoría, estado y tipo de usuario.
- **FR-017**: El sistema MUST generar un identificador automático por queja.
- **FR-018**: Excel MUST incluir identificador, fecha, categoría, número si
  aplica, tipo de usuario, nombre y semestre solo si corresponde, asunto,
  descripción, estado, fecha de cumplimiento si existe y enlace a evidencia.
- **FR-019**: Solo el administrador MUST poder descargar Excel completo o
  filtrado, y cada descarga MUST quedar auditada con usuario, fecha y hora.
- **FR-020**: El sistema MUST permitir seleccionar quejas individuales o por
  categoría para generar reportes Word.
- **FR-021**: El reporte Word MUST contener portada, resumen, detalle,
  estadísticas, completado, no completado y conclusiones.
- **FR-022**: La portada MUST decir “Reporte”, mostrar fecha de descarga y no
  incluir logo.
- **FR-023**: El detalle MUST incluir información del formulario, nombre y
  semestre cuando corresponda e imágenes de evidencia.
- **FR-024**: El administrador MUST poder editar el reporte antes de
  descargarlo; cada generación MUST quedar auditada.
- **FR-025**: La IA MUST basarse en datos registrados, no inventar datos, no
  acusar ni determinar culpabilidades y presentar sugerencias como propuestas
  para evaluación humana.
- **FR-026**: Nombre, semestre e imágenes MUST no enviarse al servicio de IA.
- **FR-027**: El dashboard MUST mostrar totales, categorías, estados, categorías
  principales, proporción anónima/identificada, gráficos semanales y mensuales,
  y resumen mensual de trabajo.
- **FR-028**: Las estadísticas MUST derivarse de datos almacenados y mostrar
  gráficas vacías cuando no existan datos.
- **FR-029**: Las predicciones MUST identificarse como estimaciones de IA,
  separarse de datos reales y cubrir volumen total y categorías para próxima
  semana y próximo mes.
- **FR-030**: Solo se MUST generar predicción por categoría con al menos cinco
  quejas en ella y predicción total con al menos cinco quejas en total; si no,
  el sistema MUST indicarlo y no mostrarla.
- **FR-031**: El dashboard MUST ser visible solo para el administrador.
- **FR-032**: Toda queja MUST iniciar en `Pendiente` y permitir los estados
  `Completada` y `No completada` según las reglas descritas.
- **FR-033**: Completar MUST exigir comentario de gestión y registrar fecha de
  cumplimiento automática e inmodificable.
- **FR-034**: Reabrir MUST exigir observación; volver a completar MUST exigir
  conclusión final; fechas anteriores MUST conservarse en historial.
- **FR-035**: El seguimiento mensual MUST contar finalizaciones, reaperturas y
  nuevas finalizaciones en sus meses correspondientes.
- **FR-036**: Cada cambio de estado MUST registrar usuario, fecha y hora.
- **FR-037**: Debe existir un único administrador, sin registro de cuentas ni
  otros roles, y el panel MUST exigir autenticación.
- **FR-038**: El logo MUST abrir el acceso oculto del administrador, sin
  sustituir la autenticación.
- **FR-039**: La recuperación MUST usar únicamente la pregunta “¿Qué
  representante eres?”; el administrador MUST poder cambiar usuario,
  contraseña, pregunta y respuesta desde configuración.
- **FR-040**: El usuario administrativo MUST seguir el formato
  `administrator` más el período académico y la contraseña MUST seguir el
  formato `RepresentanteIng` más el año.
- **FR-041**: Tres intentos fallidos consecutivos de inicio de sesión o pregunta
  clave MUST bloquear el acceso durante 24 horas.
- **FR-042**: La sesión MUST cerrarse tras 10 horas de inactividad.
- **FR-043**: El sistema MUST auditar accesos exitosos y fallidos, bloqueos,
  cierres por inactividad, cambios de credenciales, cambios de estado,
  descargas Excel y generaciones de reportes, sin guardar contenido de quejas
  ni datos de identidad en esos registros.
- **FR-044**: Las credenciales iniciales completas del administrador MUST
  entregarse por separado y no escribirse en el código, esta especificación ni
  el repositorio.
- **FR-045**: El sistema MUST permitir vincular una sola cuenta de Telegram
  mediante un código de un solo uso que expire en 10 minutos, y MUST permitir
  desvincularla y vincular otra.
- **FR-046**: El sistema MUST permitir activar o desactivar los avisos de
  nuevas quejas para la cuenta de Telegram vinculada.
- **FR-047**: Los avisos de nuevas quejas MUST incluir únicamente identificador,
  categoría, número si aplica, tipo de usuario y fecha de registro.
- **FR-048**: El bot MUST responder únicamente a la cuenta vinculada los
  comandos de solo lectura Ayuda, Resumen, Pendientes y Mes.
- **FR-049**: El bot MUST responder a cuentas no vinculadas con un mensaje
  genérico sin datos de quejas y MUST indicar los comandos disponibles ante
  entradas desconocidas.
- **FR-050**: El bot MUST NOT recibir ni solicitar contraseñas o respuestas de
  seguridad, y MUST NOT permitir cambios de estado, descargas o reportes.
- **FR-051**: Con una cuenta de Telegram vinculada, el inicio de sesión y todo
  cambio de contraseña MUST requerir un código de verificación de un solo uso
  que expire en 15 segundos; sin cuenta vinculada requieren solo usuario y
  contraseña.
- **FR-052**: Tras tres códigos de verificación incorrectos consecutivos,
  el sistema MUST mostrar la pregunta clave como alternativa; una respuesta
  correcta completa el acceso y tres respuestas incorrectas bloquean 24 horas.
- **FR-053**: Los códigos de vinculación y verificación MUST almacenarse solo
  de forma protegida, nunca en claro ni en registros.
- **FR-054**: Una falla de Telegram MUST NOT impedir registrar una queja ni
  mostrar el error de integración al estudiante.
- **FR-055**: Telegram MUST NOT recibir asunto, descripción, nombre, semestre
  ni imágenes, y ningún dato de Telegram MUST asociarse con una queja.
- **FR-056**: El sistema MUST auditar vinculación, desvinculación, cambios de
  avisos y envío o validación de códigos sin almacenar los códigos.

### Key Entities

- **Queja**: Registro único que representa una queja o reclamo, con identificador,
  usuario anónimo/identificado, categoría, número opcional, asunto,
  descripción, evidencia, estado y fechas.
- **Evidencia**: Imagen JPG/JPEG o PNG asociada a una queja y almacenada en la
  ubicación del administrador con metadatos anonimizados.
- **Historial de seguimiento**: Cambios de estado, comentarios, observaciones,
  conclusiones y fechas de cumplimiento o reapertura.
- **Reporte descargable**: Documento Word generado para quejas seleccionadas,
  editable antes de descargar.
- **Registro de auditoría**: Evento de acceso, bloqueo, credenciales, estado,
  descarga de Excel o generación de reporte, sin contenido de quejas.
- **Cuenta del administrador**: Única cuenta con acceso al panel y a la
  configuración.
- **Cuenta de Telegram vinculada**: Única cuenta externa asociada al
  administrador, con preferencia de avisos y permisos de consulta restringidos.
- **Código de vinculación o verificación**: Desafío de un solo uso con
  expiración, cuyo valor no se conserva en claro.
- **Resumen de Telegram**: Datos agregados o campos mínimos de una queja que
  pueden enviarse a la cuenta vinculada, sin identidad, asunto, descripción ni
  imágenes.

## Success Criteria

### Measurable Outcomes

- **SC-001**: El 100% de los envíos anónimos de prueba no contiene nombre,
  semestre ni otro dato de identidad asociado.
- **SC-002**: El 100% de las quejas válidas de prueba inicia en `Pendiente` y
  conserva sus cambios de estado en el historial.
- **SC-003**: El 100% de las evidencias válidas de prueba cumple formato,
  cantidad y tamaño, queda en la ubicación del administrador y no conserva
  metadatos identificables.
- **SC-004**: El 100% de las descargas Excel de prueba contiene solo el conjunto
  completo o filtrado solicitado y queda auditado.
- **SC-005**: El 100% de los reportes Word de prueba contiene las siete
  secciones, permite edición previa y excluye identidad e imágenes del envío a
  IA.
- **SC-006**: El 100% de las cifras mostradas en pruebas del dashboard coincide
  con las quejas almacenadas y los meses sin datos muestran gráficas vacías.
- **SC-007**: En pruebas con menos de cinco quejas, no se muestra predicción; con
  cinco o más en el ámbito correspondiente, se muestra como estimación separada.
- **SC-008**: El 100% de las finalizaciones y reaperturas de prueba exige el
  texto obligatorio correspondiente, registra fecha automática y conserva fechas
  previas.
- **SC-009**: El 100% de los intentos de acceso, bloqueos, cambios de
  credenciales, cierres, estados, descargas y generaciones de prueba produce un
  registro sin contenido de quejas ni identidad.
- **SC-010**: Ninguna persona no autenticada puede acceder a las funciones del
  panel, dashboard, configuración, Excel o Word durante las pruebas.
- **SC-011**: El 100% de los avisos y respuestas de Telegram de prueba excluye
  asunto, descripción, nombre, semestre e imágenes.
- **SC-012**: El 100% de los comandos enviados desde cuentas no vinculadas
  devuelve cero datos de quejas, y ningún comando modifica o descarga quejas.
- **SC-013**: Una indisponibilidad de Telegram no impide completar ningún
  envío válido de queja en las pruebas de integración.
- **SC-014**: El 100% de los códigos de verificación y vinculación de prueba
  queda inutilizable al vencer o después de su primer uso y nunca aparece en
  registros.

## Assumptions

- El servicio se utiliza desde una URL web en celulares y no incluye una
  aplicación móvil nativa.
- Existe una sola facultad o institución en esta iteración.
- El administrador recibe por separado las credenciales iniciales completas.
- No se construyen seguimiento para estudiantes, notificaciones por correo u
  otros medios distintos de Telegram, cuentas de estudiantes, otros
  administradores, otros roles, otros formatos de evidencia, publicación
  pública, chatbot ni exportaciones distintas de Excel y Word.
- Un reclamo se trata exactamente igual que una queja; no existe un tipo
  separado de reclamo.
