# Ecosistema El Cuaderno

Este repositorio es la aplicación **Student** del mismo producto que:

- Maestro: `com.profecuaderno.app`
- Student: `com.profecuaderno.student`
- Dirección: `com.profecuaderno.direction` (reservado)

## Identidad visual compartida

Las apps usan la misma familia visual:

- Material 3.
- fondo tipo cuaderno;
- tarjetas redondeadas de 24–28 dp;
- tema predeterminado Menta/Lavanda;
- morado `#7654A8`;
- menta `#50BDB3`;
- turquesa `#319DA5`;
- bloques pastel Menta, Lavanda, Rosa, Azul, Crema y Morado suave.

Student mantiene navegación y densidad de funciones más simples que Maestro.

## Contrato de datos v1

Entidades compartidas con el vocabulario ya existente en Maestro:

- STUDENT
- ATTENDANCE_SESSION
- ATTENDANCE_RECORD
- EVALUATION_CATEGORY
- ASSESSMENT_ITEM
- ASSESSMENT_SCORE
- RUBRIC_CRITERION
- GRADE
- RUBRIC_MARK
- EVENT

Student reserva además:

- NOTICE
- SCHEDULE
- JUSTIFICATION_REQUEST

Estados base de sincronización:

- LOCAL_ONLY
- SYNCED
- DIRTY
- DELETED
- CONFLICT

## Límites de Student

Student nunca debe editar calificaciones o asistencias, consultar datos de otro alumno ni cambiar directamente una falta a justificada.

La futura acción **Tengo justificante** creará una `JUSTIFICATION_REQUEST`; el registro de asistencia solo cambiará cuando Maestro o Dirección lo aprueben mediante el servidor.

## Seguridad futura

Las restricciones del cliente son UX, no autorización. El backend deberá validar la cuenta, resolver su `studentId`, filtrar toda consulta por ese estudiante, denegar escrituras académicas desde STUDENT y auditar cambios.
