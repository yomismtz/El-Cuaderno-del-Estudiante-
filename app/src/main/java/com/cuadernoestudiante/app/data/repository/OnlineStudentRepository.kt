package com.cuadernoestudiante.app.data.repository

import com.cuadernoestudiante.app.core.model.*
import com.cuadernoestudiante.app.network.AttendanceDto
import com.cuadernoestudiante.app.network.CentralBackend
import com.cuadernoestudiante.app.network.ClassDto
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
import java.time.OffsetDateTime
import kotlin.math.roundToInt

class OnlineStudentRepository(
    private val backend: CentralBackend,
) : StudentRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _snapshot = MutableStateFlow(emptySnapshot())
    override val snapshot: StateFlow<StudentDataSnapshot> = _snapshot.asStateFlow()

    init {
        refreshFromServer()
    }

    override fun resetDemoData() {
        refreshFromServer()
    }

    override fun markAssessmentCompleted(localId: String, completed: Boolean) {
        // Server-published grades and attendance are read-only for students.
        // Local completion remains available for future task endpoints without altering grades.
        val current = _snapshot.value
        _snapshot.value = current.copy(
            assessments = current.assessments.map { assessment ->
                if (assessment.meta.localId == localId && assessment.status != AssessmentStatus.GRADED) {
                    assessment.copy(status = if (completed) AssessmentStatus.SUBMITTED else AssessmentStatus.PENDING)
                } else {
                    assessment
                }
            }
        )
    }

    fun refreshFromServer() {
        if (backend.tokenStore.accessToken.isNullOrBlank()) return
        scope.launch {
            runCatching { loadSnapshot() }
                .onSuccess { _snapshot.value = it }
        }
    }

    private suspend fun loadSnapshot(): StudentDataSnapshot {
        val me = backend.api.me()
        val classes = backend.api.classes()
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
        val attendanceByClass = mutableMapOf<Int, List<AttendanceDto>>()
        val notices = mutableListOf<Notice>()

        classes.forEach { classroom ->
            val classGrades = backend.api.grades(classroom.id)
            val classAttendance = backend.api.attendance(classroom.id)
            gradesByClass[classroom.id] = classGrades
            attendanceByClass[classroom.id] = classAttendance

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
            val attendance = attendanceByClass[classroom.id].orEmpty()
            Subject(
                meta = meta(subjectId(classroom), classroom.id.toString()),
                name = classroom.subject.ifBlank { classroom.name },
                teacherName = "Docente",
                currentGrade = averageGrade(grades),
                attendancePercent = attendancePercent(attendance),
            )
        }

        val assessments = classes.flatMap { classroom ->
            gradesByClass[classroom.id].orEmpty().map { grade ->
                Assessment(
                    meta = meta(
                        "grade-${classroom.id}-${grade.activityKey}",
                        "${classroom.id}:${grade.activityKey}",
                    ),
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
            attendanceByClass[classroom.id].orEmpty().mapIndexed { index, item ->
                AttendanceRecord(
                    meta = meta("attendance-${classroom.id}-${item.date}-$index"),
                    subjectLocalId = subjectId(classroom),
                    date = runCatching { LocalDate.parse(item.date) }.getOrElse { LocalDate.now() },
                    status = attendanceStatus(item.status),
                )
            }
        }

        val progress = classes.mapNotNull { classroom ->
            val grades = gradesByClass[classroom.id].orEmpty()
            if (grades.isEmpty()) null else SubjectProgress(
                subjectLocalId = subjectId(classroom),
                components = grades.map { grade ->
                    ProgressComponent(
                        label = grade.category.ifBlank { grade.activityName },
                        weightPercent = 0,
                        value = GradeValue.graded(normalizedGrade(grade)),
                    )
                },
            )
        }

        return StudentDataSnapshot(
            profile = StudentProfile(
                meta = meta("profile-${me.id}", me.id.toString()),
                name = me.fullName,
                enrollmentId = me.email,
                school = me.institutionId?.let { "Institución $it" }.orEmpty(),
                group = classes.joinToString(", ") { it.name }.ifBlank { "Sin clase vinculada" },
                educationLevel = "",
                schoolYear = classes.map { it.periodName }.filter { it.isNotBlank() }.distinct().joinToString(", "),
            ),
            subjects = subjects,
            assessments = assessments,
            attendance = attendance,
            notices = notices.sortedByDescending { it.publishedAt },
            calendarEvents = emptyList(),
            schedule = emptyList(),
            progress = progress,
        )
    }

    private fun subjectId(classroom: ClassDto) = "class-${classroom.id}"

    private fun normalizedGrade(grade: GradeDto): Double =
        if (grade.maxScore > 0.0) (grade.score / grade.maxScore) * 10.0 else grade.score

    private fun averageGrade(grades: List<GradeDto>): GradeValue {
        if (grades.isEmpty()) return GradeValue.notEvaluated()
        return GradeValue.graded(grades.map(::normalizedGrade).average())
    }

    private fun attendancePercent(items: List<AttendanceDto>): Int {
        if (items.isEmpty()) return 0
        val attended = items.count { item ->
            when (item.status.trim().lowercase()) {
                "present", "presente", "late", "retardo", "excused", "justificada", "justificado" -> true
                else -> false
            }
        }
        return ((attended.toDouble() / items.size) * 100.0).roundToInt()
    }

    private fun attendanceStatus(value: String): AttendanceStatus = when (value.trim().lowercase()) {
        "absent", "ausente", "falta" -> AttendanceStatus.ABSENT
        "late", "retardo", "tarde" -> AttendanceStatus.LATE
        "excused", "justificada", "justificado" -> AttendanceStatus.EXCUSED
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
                profile = StudentProfile(
                    meta = meta,
                    name = "Cargando…",
                    enrollmentId = "",
                    school = "",
                    group = "",
                    educationLevel = "",
                    schoolYear = "",
                ),
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
