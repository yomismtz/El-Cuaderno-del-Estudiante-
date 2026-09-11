package com.cuadernoestudiante.app.data.repository

import com.cuadernoestudiante.app.core.model.GradeValue
import com.cuadernoestudiante.app.network.EvaluationPlanDto
import com.cuadernoestudiante.app.network.GradeDto

internal object StudentGradeCalculator {
    fun normalizedGrade(grade: GradeDto): Double =
        normalizedGrade(grade.score, grade.maxScore)

    fun normalizedGrade(score: Double, maxScore: Double): Double =
        if (maxScore > 0.0) ((score / maxScore) * 100.0).coerceIn(0.0, 100.0)
        else score.coerceIn(0.0, 100.0)

    fun calculatedCurrentGrade(grades: List<GradeDto>, plan: EvaluationPlanDto?): GradeValue {
        if (plan?.finalized == true && plan.categories.isNotEmpty()) {
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
}
