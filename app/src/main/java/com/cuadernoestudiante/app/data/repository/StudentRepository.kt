package com.cuadernoestudiante.app.data.repository

import com.cuadernoestudiante.app.core.model.StudentDataSnapshot
import kotlinx.coroutines.flow.StateFlow

interface StudentRepository {
    val snapshot: StateFlow<StudentDataSnapshot>

    fun resetDemoData()

    fun markAssessmentCompleted(localId: String, completed: Boolean)
}
