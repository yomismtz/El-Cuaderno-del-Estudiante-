package com.cuadernoestudiante.app.domain

import com.cuadernoestudiante.app.core.model.EvaluationStatus
import com.cuadernoestudiante.app.core.model.GradeValue
import com.cuadernoestudiante.app.core.model.ProgressComponent

object GradeCalculator {
    fun meanEvaluated(values: List<GradeValue>): Double? {
        val evaluated = values
            .filter { it.status == EvaluationStatus.GRADED }
            .mapNotNull { it.score }

        return evaluated.takeIf { it.isNotEmpty() }?.average()
    }

    fun weightedAverage(components: List<ProgressComponent>): Double? {
        val evaluated = components.filter {
            it.value.status == EvaluationStatus.GRADED && it.value.score != null
        }
        val activeWeight = evaluated.sumOf { it.weightPercent }
        if (activeWeight == 0) return null

        val weightedPoints = evaluated.sumOf {
            requireNotNull(it.value.score) * it.weightPercent
        }
        return weightedPoints / activeWeight
    }
}
