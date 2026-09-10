package com.cuadernoestudiante.app.core.model

/**
 * Contrato local v2 del ecosistema El Cuaderno.
 * La autorización real deberá ser validada por el backend.
 */
object EcosystemContract {
    const val PROTOCOL_VERSION = 2

    const val TEACHER_APP_ID = "com.profecuaderno.app"
    const val STUDENT_APP_ID = "com.profecuaderno.student"
    const val DIRECTOR_APP_ID = "com.profecuaderno.director"

    enum class Role {
        TEACHER,
        STUDENT,
        DIRECTOR,
    }

    enum class SharedEntityType {
        STUDENT,
        ATTENDANCE_SESSION,
        ATTENDANCE_RECORD,
        EVALUATION_CATEGORY,
        ASSESSMENT_ITEM,
        ASSESSMENT_SCORE,
        RUBRIC_CRITERION,
        GRADE,
        RUBRIC_MARK,
        EVENT,
        NOTICE,
        SCHEDULE,
        TASK_COMPLETION,
        JUSTIFICATION_REQUEST,
    }

    enum class NoticeSource {
        TEACHER,
        DIRECTOR,
    }

    object StudentCapabilities {
        const val READ_OWN_PROFILE = true
        const val READ_OWN_GRADES = true
        const val READ_OWN_ATTENDANCE = true
        const val READ_OWN_SCHEDULE = true
        const val READ_OWN_NOTICES = true
        const val READ_OWN_CALENDAR = true
        const val MARK_OWN_TASK_COMPLETED = true

        const val EDIT_GRADES = false
        const val EDIT_ATTENDANCE = false
        const val EDIT_SCHEDULE = false
        const val PUBLISH_NOTICES = false
        const val READ_OTHER_STUDENTS = false
    }
}
