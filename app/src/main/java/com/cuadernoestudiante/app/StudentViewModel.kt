package com.cuadernoestudiante.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cuadernoestudiante.app.data.repository.StudentRepository

class StudentViewModel(
    private val repository: StudentRepository,
) : ViewModel() {
    val snapshot = repository.snapshot

    fun resetDemoData() = repository.resetDemoData()

    fun markAssessmentCompleted(localId: String, completed: Boolean) =
        repository.markAssessmentCompleted(localId, completed)

    class Factory(
        private val repository: StudentRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(StudentViewModel::class.java))
            return StudentViewModel(repository) as T
        }
    }
}
