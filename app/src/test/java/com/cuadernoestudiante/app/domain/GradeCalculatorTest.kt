package com.cuadernoestudiante.app.domain

import com.cuadernoestudiante.app.core.model.GradeValue
import com.cuadernoestudiante.app.core.model.ProgressComponent
import org.junit.Assert.assertEquals
import org.junit.Test

class GradeCalculatorTest {
    @Test
    fun notEvaluated_isIgnoredInsteadOfBecomingZero() {
        val average = GradeCalculator.meanEvaluated(
            listOf(
                GradeValue.graded(10.0),
                GradeValue.notEvaluated(),
            )
        )

        assertEquals(10.0, average ?: error("Expected a grade"), 0.0001)
    }

    @Test
    fun realZero_countsAsARealGrade() {
        val average = GradeCalculator.meanEvaluated(
            listOf(
                GradeValue.graded(10.0),
                GradeValue.graded(0.0),
            )
        )

        assertEquals(5.0, average ?: error("Expected a grade"), 0.0001)
    }

    @Test
    fun weightedAverage_renormalizesOnlyEvaluatedComponents() {
        val average = GradeCalculator.weightedAverage(
            listOf(
                ProgressComponent("Actividades", 30, GradeValue.graded(9.0)),
                ProgressComponent("Exámenes", 40, GradeValue.notEvaluated()),
                ProgressComponent("Proyecto", 30, GradeValue.graded(6.0)),
            )
        )

        assertEquals(7.5, average ?: error("Expected a grade"), 0.0001)
    }
}
