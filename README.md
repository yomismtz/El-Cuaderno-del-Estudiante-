# El Cuaderno del Estudiante 📓

Aplicación Android Student del ecosistema **El Cuaderno**, diseñada como compañera de **El Cuaderno del Maestro**.

## Familia de apps

- **El Cuaderno del Maestro** — `com.profecuaderno.app`
- **El Cuaderno del Estudiante** — `com.profecuaderno.student`
- **El Cuaderno de Dirección** — `com.profecuaderno.direction` (reservado)

Student comparte con Maestro la misma identidad visual: fondo tipo cuaderno, tarjetas redondeadas, Material 3 y la misma familia de temas.

Student sigue siendo deliberadamente más sencilla y de solo consulta para datos académicos protegidos.

## Estado actual

- Jetpack Compose + Material 3.
- Navegación inferior: Inicio, Materias, Calendario, Progreso y Perfil.
- Rutas secundarias: Avisos, Pendientes, Asistencia y Horario.
- Pantalla Inicio adaptada a la familia visual de Maestro.
- 6 materias y datos ficticios de actividades, exámenes, avisos, asistencia, horario y calendario.
- Botón para restablecer datos demo.
- Funcionamiento offline.
- Sin permiso de Internet en esta etapa.
- Modelo preparado para sincronización futura.

## Privacidad

1. Student solo representa al estudiante autenticado (`accountId` + `studentId`).
2. Calificaciones y asistencia son de solo lectura desde Student.
3. Student no altera calificaciones ni asistencias.
4. Una futura solicitud de justificante será una entidad separada.
5. No hay acceso a datos de otros alumnos.
6. La autorización definitiva se aplicará en el servidor.

## Identidad y sincronización

Cada registro sincronizable incluye `localId`, `syncId`, `accountId`, `studentId`, `createdAt`, `updatedAt` y `syncStatus`.

El contrato común del ecosistema está documentado en `docs/ecosystem-pack.md`.

## Regla crítica de calificaciones

`Sin evaluar` nunca se representa como `0`.

- `GradeValue.notEvaluated()` = todavía no existe calificación.
- `GradeValue.graded(0.0)` = el estudiante obtuvo cero realmente.

Las pruebas unitarias cubren ambos casos.

## Roadmap inmediato

1. Materias y detalle de materia.
2. Mi progreso.
3. Asistencia.
4. Avisos.
5. Calendario y Pendientes.
6. Horario.
7. Perfil y selector de apariencia.
8. Persistencia local.
9. Autenticación y sincronización con Maestro/Dirección cuando exista backend.
