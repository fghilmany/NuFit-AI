package com.fghilmany.nufitai.domain.aicustomplan.entity

import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentResult

data class AiPlanGenerationRequest(
    val formInput: AiPlanFormInput,
    val fullAssessment: FullAssessmentResult,
    val workoutHistorySummary: WorkoutHistorySummary,
)
