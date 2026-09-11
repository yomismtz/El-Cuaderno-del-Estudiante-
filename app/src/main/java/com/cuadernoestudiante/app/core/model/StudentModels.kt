package com.cuadernoestudiante.app.core.model

import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class StudentProfile(
    val meta: SyncMetadata,
    val name: String,
    val enrollmentId: String,
    val school: String,
    val group: String,
    val educationLevel: String,
    val schoolYear: String,
) : Serializable

data class Subject(
    val meta: SyncMetadata,
    val name: String,
    val teacherName: String,
    val currentGrade: GradeValue,
    val attendancePercent: Int,
) : Serializable

enum class AssessmentType { ACTIVITY, EXAM, PROJECT, PRESENTATION }
enum class AssessmentStatus { PENDING, SUBMITTED, GRADED, OVERDUE }

data class Assessment(
    val meta: SyncMetadata,
    val subjectLocalId: String,
    val title: String,
    val type: AssessmentType,
    val dueAt: LocalDateTime,
    val status: AssessmentStatus,
    val grade: GradeValue = GradeValue.notEvaluated(),
) : Serializable

enum class AttendanceStatus { PRESENT, ABSENT, LATE, EXCUSED }

data class AttendanceRecord(
    val meta: SyncMetadata,
    val subjectLocalId: String? = null,
    val date: LocalDate,
    val status: AttendanceStatus,
    val sessionTitle: String? = null,
) : Serializable

enum class NoticePublisherType { TEACHER, DIRECTION }

data class Notice(
    val meta: SyncMetadata,
    val title: String,
    val body: String,
    val publisherName: String,
    val publisherType: NoticePublisherType,
    val subjectLocalId: String? = null,
    val publishedAt: LocalDateTime,
    val isRead: Boolean = false,
) : Serializable

enum class CalendarEventType { TASK, EXAM, PROJECT, SCHOOL_EVENT, IMPORTANT_DATE }

data class CalendarEvent(
    val meta: SyncMetadata,
    val title: String,
    val type: CalendarEventType,
    val startAt: LocalDateTime,
    val subjectLocalId: String? = null,
    val details: String? = null,
) : Serializable

data class ScheduleEntry(
    val meta: SyncMetadata,
    val subjectLocalId: String,
    val dayOfWeek: Int,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val room: String? = null,
) : Serializable

data class ProgressComponent(val label: String, val weightPercent: Int, val value: GradeValue) : Serializable
data class SubjectProgress(val subjectLocalId: String, val components: List<ProgressComponent>) : Serializable

data class StudentDataSnapshot(
    val profile: StudentProfile,
    val subjects: List<Subject>,
    val assessments: List<Assessment>,
    val attendance: List<AttendanceRecord>,
    val notices: List<Notice>,
    val calendarEvents: List<CalendarEvent>,
    val schedule: List<ScheduleEntry>,
    val progress: List<SubjectProgress>,
) : Serializable
