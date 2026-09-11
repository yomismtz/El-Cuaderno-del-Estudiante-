package com.cuadernoestudiante.app.data.repository

import com.cuadernoestudiante.app.core.model.EvaluationStatus
import com.cuadernoestudiante.app.network.EvaluationPlanCategoryDto
import com.cuadernoestudiante.app.network.EvaluationPlanDto
import com.cuadernoestudiante.app.network.GradeDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StudentGradeCalculatorTest {
    private fun grade(key: String, score: Double, max: Double = 100.0) = GradeDto(
        category = key,
        activityKey = key,
        activityName = key,
        score = score,
        maxScore = max,
        source = "teacher_app",
    )

    private fun plan() = EvaluationPlanDto(
        classId = 1,
        finalized = true,
        categories = listOf(
            EvaluationPlanCategoryDto("category-1", "Exámenes", 60.0, "exams", 0),
            EvaluationPlanCategoryDto("category-2", "Asistencia", 40.0, "attendance", 1),
        ),
    )

    @Test
    fun normalizesToHundredPointScaleAndPreservesRealZero() {
        assertEquals(50.0, StudentGradeCalculator.normalizedGrade(5.0, 10.0), 0.0001)
        assertEquals(0.0, StudentGradeCalculator.normalizedGrade(0.0, 100.0), 0.0001)
    }

    @Test
    fun weightedCurrentGradeMatchesTeacherCalculation() {
        val result = StudentGradeCalculator.calculatedCurrentGrade(
            listOf(grade("category-1", 80.0), grade("category-2", 100.0)),
            plan(),
        )
        assertEquals(EvaluationStatus.GRADED, result.status)
        assertEquals(88.0, result.score ?: -1.0, 0.0001)
    }

    @Test
    fun unevaluatedCategoryIsNotTreatedAsZero() {
        val result = StudentGradeCalculator.calculatedCurrentGrade(
            listOf(grade("category-1", 80.0)),
            plan(),
        )
        assertEquals(EvaluationStatus.GRADED, result.status)
        assertEquals(80.0, result.score ?: -1.0, 0.0001)
    }

    @Test
    fun noGradesMeansNotEvaluated() {
        val result = StudentGradeCalculator.calculatedCurrentGrade(emptyList(), plan())
        assertEquals(EvaluationStatus.NOT_EVALUATED, result.status)
        assertNull(result.score)
    }
}
