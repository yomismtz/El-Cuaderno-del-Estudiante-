# El Cuaderno del Estudiante

Aplicación Android Student del ecosistema educativo formado por Maestro, Student y Dirección.

## Estado actual

Primera base funcional offline/demo:

- Jetpack Compose + Material 3.
- Navegación inferior: Inicio, Materias, Calendario, Progreso y Perfil.
- Rutas secundarias preparadas: Avisos, Pendientes, Asistencia y Horario.
- Pantalla Inicio funcional con datos demo.
- 6 materias, actividades, exámenes, avisos, asistencia, horario y calendario ficticios.
- Botón para restablecer datos demo.
- Sin permisos de red en esta etapa.
- Modelo preparado para sincronización futura con IDs locales y remotos separados.

## Invariantes de privacidad

1. Student solo representa al estudiante autenticado (`accountId` + `studentId`).
2. Calificaciones y asistencia son datos de solo lectura desde Student.
3. No existe ninguna operación para modificar calificaciones o asistencias.
4. Una futura solicitud de justificante será una entidad/acción separada; nunca editará directamente una falta.
5. No hay perfiles de compañeros, red social ni acceso cruzado a otros alumnos.

## Identidad y sincronización

Cada registro sincronizable incluye:

- `localId`: identificador estable creado localmente.
- `syncId`: identificador del servidor, nullable mientras el registro sea local.
- `accountId`.
- `studentId`.
- `createdAt`.
- `updatedAt`.
- `syncStatus`.

La UI depende de `StudentRepository`, no de la fuente demo. Esto permite sustituir `DemoStudentRepository` por almacenamiento persistente y/o sincronización remota sin reescribir pantallas.

## Regla de calificaciones

`Sin evaluar` nunca se representa como `0`.

- `GradeValue.notEvaluated()` = todavía no existe calificación.
- `GradeValue.graded(0.0)` = el estudiante obtuvo cero realmente.

Las pruebas unitarias verifican ambos casos.

## Roadmap inmediato

1. Materias y detalle de materia.
2. Mi progreso.
3. Asistencia.
4. Avisos.
5. Calendario y Pendientes.
6. Horario.
7. Perfil.
8. Persistencia local (Room) y capa de sincronización cuando exista backend.
