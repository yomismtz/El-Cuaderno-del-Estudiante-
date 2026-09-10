package com.cuadernoestudiante.app.core.model

object ClassLinkContract {
    const val SCHEMA_VERSION = 1

    data class ClassMembership(
        val classId: String,
        val classCode: String,
        val teacherId: String,
        val groupLabel: String,
        val subjectLabel: String,
        val academicCycleId: String
    )

    enum class SenderRole { DIRECTOR, TEACHER }
    enum class Audience { TEACHER_ONLY, CLASS_STUDENTS, TEACHER_AND_CLASS_STUDENTS }

    data class Announcement(
        val announcementId: String,
        val senderRole: SenderRole,
        val senderId: String,
        val title: String,
        val message: String,
        val audience: Audience,
        val classIds: Set<String>,
        val createdAt: Long
    )

    fun visibleToStudent(item: Announcement, memberships: Set<String>): Boolean =
        item.senderRole == SenderRole.TEACHER &&
            item.audience != Audience.TEACHER_ONLY &&
            item.classIds.any { it in memberships }
}
