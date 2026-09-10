package com.cuadernoestudiante.app.core.model

/**
 * Contrato local v1 del ecosistema El Cuaderno.
 * La autorización real deberá ser validada por el backend.
 */
object EcosystemContract {
    const val PROTOCOL_VERSION = 1

    const val TEACHER_APP_ID = "com.profecuaderno.app"
    const val STUDENT_APP_ID = "com.profecuaderno.student"
    const val DIRECTION_APP_ID = "com.profecuaderno.direction"

    enum class Role {
        TEACHER,
        STUDENT,
        DIRECTION,
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
        JUSTIFICATION_REQUEST,
    }

    object StudentCapabilities {
        const val READ_OWN_PROFILE = true
        const val READ_OWN_GRADES = true
        const val READ_OWN_ATTENDANCE = true
        const val READ_OWN_SCHEDULE = true
        const val READ_OWN_NOTICES = true

        const val EDIT_GRADES = false
        const val EDIT_ATTENDANCE = false
        const val READ_OTHER_STUDENTS = false
    }
}
