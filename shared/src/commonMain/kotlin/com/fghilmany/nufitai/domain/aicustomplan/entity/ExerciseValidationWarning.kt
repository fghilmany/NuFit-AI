package com.fghilmany.nufitai.domain.aicustomplan.entity

import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay

/** AC-4 -- an AI response `exerciseId` not found in the local library; the slot was dropped. */
data class ExerciseValidationWarning(val dayNumber: Int, val invalidExerciseId: String)

data class ValidatedPlanDraft(val days: List<PlanDay>, val warnings: List<ExerciseValidationWarning>)
