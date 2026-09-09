package com.cuadernoestudiante.app.core.model

enum class EvaluationStatus {
    NOT_EVALUATED,
    GRADED,
}

data class GradeValue(
    val status: EvaluationStatus,
    val score: Double? = null,
) {
    init {
        require(status == EvaluationStatus.NOT_EVALUATED || score != null) {
            "A graded value must contain a score. Zero is a valid score."
        }
        require(status == EvaluationStatus.GRADED || score == null) {
            "A not-evaluated value must not contain a numeric score."
        }
    }

    companion object {
        fun notEvaluated() = GradeValue(EvaluationStatus.NOT_EVALUATED)
        fun graded(score: Double) = GradeValue(EvaluationStatus.GRADED, score)
    }
}
