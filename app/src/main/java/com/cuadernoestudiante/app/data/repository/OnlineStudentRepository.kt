package com.cuadernoestudiante.app.data.repository

import android.content.Context
import com.cuadernoestudiante.app.core.model.*
import com.cuadernoestudiante.app.network.AttendanceDto
import com.cuadernoestudiante.app.network.AttendanceSessionDto
import com.cuadernoestudiante.app.network.AttendanceSummaryDto
import com.cuadernoestudiante.app.network.CentralBackend
import com.cuadernoestudiante.app.network.ClassDto
import com.cuadernoestudiante.app.network.EvaluationPlanDto
import com.cuadernoestudiante.app.network.GradeDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import kotlin.math.roundToInt

class OnlineStudentRepository(
    context: Context,
    private val backend: CentralBackend,
) : StudentRepository {
    private val appContext = context.applicationContext
    private val offline = StudentOfflineStore(appContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _snapshot = MutableStateFlow(offline.loadSnapshot() ?: emptySnapshot())
    override val snapshot: StateFlow<StudentDataSnapshot> = _snapshot.asStateFlow()

    init {
        StudentSyncScheduler.ensurePeriodic(appContext)
        StudentSyncScheduler.enqueueNow(appContext)
        refreshFromServer()
    }

    override fun resetDemoData() = refreshFromServer()

    override fun markAssessmentCompleted(localId: String, completed: Boolean) {
        val current = _snapshot.value
        val updated = current.copy(
            assessments = current.assessments.map { assessment ->
                if (assessment.meta.localId == localId && assessment.status != AssessmentStatus.GRADED) {
                    assessment.copy(status = if (completed) AssessmentStatus.SUBMITTED else AssessmentStatus.PENDING)
                } else assessment
            }
        )
        _snapshot.value = updated
        offline.saveSnapshot(updated)
    }

    suspend fun joinClassOrQueue(code: String): OfflineWriteResult =
        offline.joinClassOrQueue(backend, code)

    fun pendingSyncCount(): Int = offline.pendingCount()

    fun refreshFromServer() {
        if (backend.tokenStore.accessToken.isNullOrBlank()) return
        scope.launch {
            offline.flush(backend)
            runCatching { loadSnapshot() }.onSuccess {
                offline.saveSnapshot(it)
                _snapshot.value = it
            }
        }
    }

    private suspend fun loadSnapshot(): StudentDataSnapshot {
        val me = backend.api.me()
        val classes = backend.api.classes()
        val institutionName = if (me.institutionId != null) {
            runCatching { backend.api.institution().name }.getOrElse { "Institución ${me.institutionId}" }
        } else ""
        val scheduleRows = runCatching { backend.api.schedule() }.getOrDefault(emptyList())
        val now = System.currentTimeMillis()
        val accountId = me.id.toString()
        val studentId = me.id.toString()

        fun meta(localId: String, syncId: String? = null) = SyncMetadata(
            localId = localId,
            syncId = syncId,
            accountId = accountId,
            studentId = studentId,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.SYNCED,
        )

        val gradesByClass = mutableMapOf<Int, List<GradeDto>>()
        val evaluationPlanByClass = mutableMapOf<Int, EvaluationPlanDto>()
        val attendanceByClass = mutableMapOf<Int, List<AttendanceDto>>()
        val attendanceSummaryByClass = mutableMapOf<Int, AttendanceSummaryDto>()
        val sessionsByClass = mutableMapOf<Int, Map<String, AttendanceSessionDto>>()
        val notices = mutableListOf<Notice>()

        classes.forEach { classroom ->
            val classGrades = backend.api.grades(classroom.id)
            val classAttendance = backend.api.attendance(classroom.id)
            gradesByClass[classroom.id] = classGrades
            attendanceByClass[classroom.id] = classAttendance
            runCatching { backend.api.evaluationPlan(classroom.id) }
                .getOrNull()?.let { evaluationPlanByClass[classroom.id] = it }
            runCatching { backend.api.attendanceSummary(classroom.id) }
                .getOrNull()?.let { attendanceSummaryByClass[classroom.id] = it }
            val sessions = runCatching { backend.api.attendanceSessions(classroom.id) }.getOrDefault(emptyList())
            sessionsByClass[classroom.id] = sessions.associateBy { it.date }

            backend.api.notices(classroom.id).forEach { notice ->
                notices += Notice(
                    meta = meta("notice-${classroom.id}-${notice.id}", notice.id.toString()),
                    title = notice.title,
                    body = notice.body,
                    publisherName = "Docente",
                    publisherType = NoticePublisherType.TEACHER,
                    subjectLocalId = subjectId(classroom),
                    publishedAt = parseDateTime(notice.createdAt),
                    isRead = false,
                )
            }
        }

        val subjects = classes.map { classroom ->
            val grades = gradesByClass[classroom.id].orEmpty()
            val plan = evaluationPlanByClass[classroom.id]
            val attendance = attendanceByClass[classroom.id].orEmpty()
            val serverPercent = attendanceSummaryByClass[classroom.id]?.attendancePercent?.roundToInt()?.coerceIn(0, 100)
            Subject(
                meta = meta(subjectId(classroom), classroom.id.toString()),
                name = classroom.subject.ifBlank { classroom.name },
                teacherName = "Docente",
                currentGrade = calculatedCurrentGrade(grades, plan),
                attendancePercent = serverPercent ?: attendancePercent(attendance),
            )
        }

        // Las filas category-* son el resumen de cada rubro del esquema del docente,
        // no tareas independientes. Solo otras filas (por ejemplo trabajo en equipo)
        // aparecen como evaluaciones concretas para evitar duplicar calificaciones.
        val assessments = classes.flatMap { classroom ->
            gradesByClass[classroom.id].orEmpty()
                .filterNot { it.activityKey.startsWith("category-") }
                .map { grade ->
                    Assessment(
                        meta = meta("grade-${classroom.id}-${grade.activityKey}", "${classroom.id}:${grade.activityKey}"),
                        subjectLocalId = subjectId(classroom),
                        title = grade.activityName,
                        type = AssessmentType.ACTIVITY,
                        dueAt = LocalDateTime.now(),
                        status = AssessmentStatus.GRADED,
                        grade = GradeValue.graded(normalizedGrade(grade)),
                    )
                }
        }

        val attendance = classes.flatMap { classroom ->
            val sessions = sessionsByClass[classroom.id].orEmpty()
            attendanceByClass[classroom.id].orEmpty().mapIndexedNotNull { index, item ->
                val session = sessions[item.date]
                if (session?.worked == false) return@mapIndexedNotNull null
                AttendanceRecord(
                    meta = meta("attendance-${classroom.id}-${item.date}-$index"),
                    subjectLocalId = subjectId(classroom),
                    date = runCatching { LocalDate.parse(item.date) }.getOrElse { LocalDate.now() },
                    status = attendanceStatus(item.status),
                    sessionTitle = session?.title ?: "Clase",
                )
            }
        }

        val progress = classes.mapNotNull { classroom ->
            val grades = gradesByClass[classroom.id].orEmpty()
            val plan = evaluationPlanByClass[classroom.id]
            val byKey = grades.associateBy { it.activityKey }
            val components = if (plan != null && plan.categories.isNotEmpty()) {
                plan.categories.sortedBy { it.position }.map { category ->
                    val grade = byKey[category.categoryKey]
                    ProgressComponent(
                        label = category.name,
                        weightPercent = category.weight.roundToInt().coerceIn(0, 100),
                        value = grade?.let { GradeValue.graded(normalizedGrade(it)) } ?: GradeValue.notEvaluated(),
                    )
                }
            } else {
                grades.filter { it.activityKey.startsWith("category-") }.map { grade ->
                    ProgressComponent(
                        label = grade.category.ifBlank { grade.activityName },
                        weightPercent = 0,
                        value = GradeValue.graded(normalizedGrade(grade)),
                    )
                }
            }
            components.takeIf { it.isNotEmpty() }?.let {
                SubjectProgress(subjectLocalId = subjectId(classroom), components = it)
            }
        }

        val classIds = classes.associateBy { it.id }
        val schedule = scheduleRows.mapNotNull { row ->
            val classId = row.classId ?: return@mapNotNull null
            val classroom = classIds[classId] ?: return@mapNotNull null
            val start = runCatching { LocalTime.parse(row.startTime) }.getOrNull() ?: return@mapNotNull null
            val end = runCatching { LocalTime.parse(row.endTime) }.getOrNull() ?: return@mapNotNull null
            ScheduleEntry(
                meta = meta("schedule-${row.id}", row.id.toString()),
                subjectLocalId = subjectId(classroom),
                dayOfWeek = row.weekday,
                startTime = start,
                endTime = end,
                room = row.room.ifBlank { null },
            )
        }

        return StudentDataSnapshot(
            profile = StudentProfile(
                meta = meta("profile-${me.id}", me.id.toString()),
                name = me.fullName,
                enrollmentId = me.email,
                school = institutionName,
                group = classes.joinToString(", ") { it.name }.ifBlank { "Sin clase vinculada" },
                educationLevel = "",
                schoolYear = classes.map { it.periodName }.filter { it.isNotBlank() }.distinct().joinToString(", "),
            ),
            subjects = subjects,
            assessments = assessments,
            attendance = attendance,
            notices = notices.sortedByDescending { it.publishedAt },
            calendarEvents = emptyList(),
            schedule = schedule,
            progress = progress,
        )
    }

    private fun subjectId(classroom: ClassDto) = "class-${classroom.id}"

    private fun normalizedGrade(grade: GradeDto): Double =
        if (grade.maxScore > 0.0) ((grade.score / grade.maxScore) * 100.0).coerceIn(0.0, 100.0)
        else grade.score.coerceIn(0.0, 100.0)

    private fun calculatedCurrentGrade(grades: List<GradeDto>, plan: EvaluationPlanDto?): GradeValue {
        if (plan != null && plan.categories.isNotEmpty()) {
            val byKey = grades.associateBy { it.activityKey }
            var weightedPoints = 0.0
            var evaluatedWeight = 0.0
            plan.categories.forEach { category ->
                val grade = byKey[category.categoryKey] ?: return@forEach
                if (category.weight <= 0.0) return@forEach
                weightedPoints += normalizedGrade(grade) * category.weight
                evaluatedWeight += category.weight
            }
            return if (evaluatedWeight <= 0.0) GradeValue.notEvaluated()
            else GradeValue.graded((weightedPoints / evaluatedWeight).coerceIn(0.0, 100.0))
        }

        val summaries = grades.filter { it.activityKey.startsWith("category-") }
        return averageGrade(summaries.ifEmpty { grades })
    }

    private fun averageGrade(grades: List<GradeDto>): GradeValue {
        if (grades.isEmpty()) return GradeValue.notEvaluated()
        return GradeValue.graded(grades.map(::normalizedGrade).average().coerceIn(0.0, 100.0))
    }

    private fun attendancePercent(items: List<AttendanceDto>): Int {
        if (items.isEmpty()) return 0
        val attended = items.count { item ->
            when (item.status.trim().lowercase()) {
                "present", "presente", "late", "retardo", "justified", "excused", "justificada", "justificado" -> true
                else -> false
            }
        }
        return ((attended.toDouble() / items.size) * 100.0).roundToInt()
    }

    private fun attendanceStatus(value: String): AttendanceStatus = when (value.trim().lowercase()) {
        "absent", "ausente", "falta" -> AttendanceStatus.ABSENT
        "late", "retardo", "tarde" -> AttendanceStatus.LATE
        "justified", "excused", "justificada", "justificado" -> AttendanceStatus.EXCUSED
        else -> AttendanceStatus.PRESENT
    }

    private fun parseDateTime(value: String?): LocalDateTime {
        if (value.isNullOrBlank()) return LocalDateTime.now()
        return runCatching { OffsetDateTime.parse(value).toLocalDateTime() }
            .recoverCatching { LocalDateTime.parse(value) }
            .getOrElse { LocalDateTime.now() }
    }

    private companion object {
        fun emptySnapshot(): StudentDataSnapshot {
            val now = System.currentTimeMillis()
            val meta = SyncMetadata(
                localId = "profile-loading",
                syncId = null,
                accountId = "online",
                studentId = "online",
                createdAt = now,
                updatedAt = now,
                syncStatus = SyncStatus.SYNCED,
            )
            return StudentDataSnapshot(
                profile = StudentProfile(meta, "Cargando…", "", "", "", "", ""),
                subjects = emptyList(),
                assessments = emptyList(),
                attendance = emptyList(),
                notices = emptyList(),
                calendarEvents = emptyList(),
                schedule = emptyList(),
                progress = emptyList(),
            )
        }
    }
}
