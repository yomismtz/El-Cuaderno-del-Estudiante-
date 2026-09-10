package com.cuadernoestudiante.app.data.demo

import com.cuadernoestudiante.app.core.model.AssessmentStatus
import com.cuadernoestudiante.app.core.model.StudentDataSnapshot
import com.cuadernoestudiante.app.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DemoStudentRepository : StudentRepository {
    private val _snapshot = MutableStateFlow(DemoDataFactory.create())
    override val snapshot: StateFlow<StudentDataSnapshot> = _snapshot.asStateFlow()

    override fun resetDemoData() {
        _snapshot.value = DemoDataFactory.create()
    }

    override fun markAssessmentCompleted(localId: String, completed: Boolean) {
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
}
