package com.cuadernoestudiante.app.data.demo

import com.cuadernoestudiante.app.core.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object DemoDataFactory {
    private const val ACCOUNT_ID = "demo-account-sofia"
    private const val STUDENT_ID = "demo-student-sofia"
    private const val CREATED_AT = 1788991200000L

    private fun meta(localId: String, updatedAt: Long = CREATED_AT) = SyncMetadata(
        localId = localId,
        syncId = null,
        accountId = ACCOUNT_ID,
        studentId = STUDENT_ID,
        createdAt = CREATED_AT,
        updatedAt = updatedAt,
        syncStatus = SyncStatus.LOCAL_ONLY,
    )

    fun create(): StudentDataSnapshot {
        val subjects = listOf(
            Subject(meta("subject-biology"), "Biología", "Dra. Laura Méndez", GradeValue.graded(8.8), 94),
            Subject(meta("subject-math"), "Matemáticas", "Prof. Carlos Ruiz", GradeValue.graded(8.4), 96),
            Subject(meta("subject-history"), "Historia", "Prof. Elena Soto", GradeValue.graded(9.1), 92),
            Subject(meta("subject-spanish"), "Español", "Prof. Mariana León", GradeValue.graded(8.9), 95),
            Subject(meta("subject-english"), "Inglés", "Prof. Daniel Vega", GradeValue.graded(8.6), 94),
            Subject(meta("subject-chemistry"), "Química", "Dra. Alejandra Mora", GradeValue.graded(8.5), 93),
        )

        val assessments = listOf(
            Assessment(
                meta("assessment-cells"), "subject-biology", "Tarea de células",
                AssessmentType.ACTIVITY, LocalDateTime.of(2026, 9, 10, 23, 59), AssessmentStatus.PENDING,
            ),
            Assessment(
                meta("assessment-history-project"), "subject-history", "Proyecto de Historia",
                AssessmentType.PROJECT, LocalDateTime.of(2026, 9, 10, 18, 0), AssessmentStatus.PENDING,
            ),
            Assessment(
                meta("assessment-math-exam"), "subject-math", "Examen de Matemáticas",
                AssessmentType.EXAM, LocalDateTime.of(2026, 9, 12, 9, 0), AssessmentStatus.PENDING,
            ),
            Assessment(
                meta("assessment-bio-presentation"), "subject-biology", "Exposición de Biología",
                AssessmentType.PRESENTATION, LocalDateTime.of(2026, 9, 15, 11, 0), AssessmentStatus.PENDING,
            ),
            Assessment(
                meta("assessment-english-reading"), "subject-english", "Reading unit 2",
                AssessmentType.ACTIVITY, LocalDateTime.of(2026, 9, 16, 20, 0), AssessmentStatus.PENDING,
            ),
            Assessment(
                meta("assessment-chemistry-lab"), "subject-chemistry", "Reporte de laboratorio",
                AssessmentType.ACTIVITY, LocalDateTime.of(2026, 9, 18, 20, 0), AssessmentStatus.PENDING,
            ),
            Assessment(
                meta("assessment-bio-graded"), "subject-biology", "Cuestionario: célula animal",
                AssessmentType.ACTIVITY, LocalDateTime.of(2026, 9, 4, 18, 0), AssessmentStatus.GRADED,
                GradeValue.graded(9.2),
            ),
            Assessment(
                meta("assessment-math-zero-demo"), "subject-math", "Ejercicio diagnóstico",
                AssessmentType.ACTIVITY, LocalDateTime.of(2026, 9, 3, 18, 0), AssessmentStatus.GRADED,
                GradeValue.graded(0.0),
            ),
        )

        val attendance = listOf(
            AttendanceRecord(meta("att-2026-09-02"), date = LocalDate.of(2026, 9, 2), status = AttendanceStatus.PRESENT),
            AttendanceRecord(meta("att-2026-09-04"), date = LocalDate.of(2026, 9, 4), status = AttendanceStatus.ABSENT),
            AttendanceRecord(meta("att-2026-09-07"), date = LocalDate.of(2026, 9, 7), status = AttendanceStatus.EXCUSED),
            AttendanceRecord(meta("att-2026-09-09"), date = LocalDate.of(2026, 9, 9), status = AttendanceStatus.LATE),
        )

        val notices = listOf(
            Notice(
                meta("notice-lab-coat"), "Mañana traer bata",
                "La práctica de laboratorio requiere bata y libreta.",
                "Dra. Laura Méndez", NoticePublisherType.TEACHER, "subject-biology",
                LocalDateTime.of(2026, 9, 9, 8, 15), false,
            ),
            Notice(
                meta("notice-exam-change"), "El examen se cambia al viernes",
                "Revisen la guía actualizada antes de la clase.",
                "Prof. Carlos Ruiz", NoticePublisherType.TEACHER, "subject-math",
                LocalDateTime.of(2026, 9, 9, 10, 0), false,
            ),
            Notice(
                meta("notice-school-event"), "Actividad escolar el 21 de septiembre",
                "Consulta el calendario para ver el horario del evento.",
                "Dirección", NoticePublisherType.DIRECTION, null,
                LocalDateTime.of(2026, 9, 8, 14, 30), true,
            ),
        )

        val calendarEvents = listOf(
            CalendarEvent(meta("event-history-project"), "Proyecto de Historia", CalendarEventType.PROJECT, LocalDateTime.of(2026, 9, 10, 18, 0), "subject-history"),
            CalendarEvent(meta("event-math-exam"), "Examen de Matemáticas", CalendarEventType.EXAM, LocalDateTime.of(2026, 9, 12, 9, 0), "subject-math"),
            CalendarEvent(meta("event-bio-presentation"), "Exposición de Biología", CalendarEventType.PROJECT, LocalDateTime.of(2026, 9, 15, 11, 0), "subject-biology"),
            CalendarEvent(meta("event-independence"), "Conmemoración escolar", CalendarEventType.SCHOOL_EVENT, LocalDateTime.of(2026, 9, 16, 8, 0), details = "Evento escolar"),
            CalendarEvent(meta("event-school-21"), "Actividad escolar", CalendarEventType.IMPORTANT_DATE, LocalDateTime.of(2026, 9, 21, 9, 0), details = "Publicado por Dirección"),
        )

        val schedule = listOf(
            ScheduleEntry(meta("schedule-mon-bio"), "subject-biology", 1, LocalTime.of(7, 30), LocalTime.of(8, 20), "Lab 2"),
            ScheduleEntry(meta("schedule-mon-math"), "subject-math", 1, LocalTime.of(8, 20), LocalTime.of(9, 10), "Aula 12"),
            ScheduleEntry(meta("schedule-tue-history"), "subject-history", 2, LocalTime.of(7, 30), LocalTime.of(8, 20), "Aula 12"),
            ScheduleEntry(meta("schedule-tue-spanish"), "subject-spanish", 2, LocalTime.of(8, 20), LocalTime.of(9, 10), "Aula 12"),
            ScheduleEntry(meta("schedule-wed-english"), "subject-english", 3, LocalTime.of(7, 30), LocalTime.of(8, 20), "Idiomas"),
            ScheduleEntry(meta("schedule-wed-chem"), "subject-chemistry", 3, LocalTime.of(8, 20), LocalTime.of(9, 10), "Lab 1"),
        )

        val progress = listOf(
            SubjectProgress(
                subjectLocalId = "subject-biology",
                components = listOf(
                    ProgressComponent("Actividades", 30, GradeValue.graded(8.8)),
                    ProgressComponent("Exámenes", 40, GradeValue.graded(7.9)),
                    ProgressComponent("Asistencia", 10, GradeValue.graded(9.5)),
                    ProgressComponent("Proyecto", 20, GradeValue.graded(9.2)),
                )
            )
        )

        return StudentDataSnapshot(
            profile = StudentProfile(
                meta = meta("profile-sofia"),
                name = "Sofía Ramírez",
                enrollmentId = "EST-2026-0248",
                school = "Secundaria Horizonte",
                group = "2.º B",
                educationLevel = "Secundaria",
                schoolYear = "2026–2027",
            ),
            subjects = subjects,
            assessments = assessments,
            attendance = attendance,
            notices = notices,
            calendarEvents = calendarEvents,
            schedule = schedule,
            progress = progress,
        )
    }
}
