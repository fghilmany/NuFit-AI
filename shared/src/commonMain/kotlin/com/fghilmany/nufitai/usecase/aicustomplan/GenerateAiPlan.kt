package com.fghilmany.nufitai.usecase.aicustomplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.aicustomplan.entity.AiGeneratedPlanDraft
import com.fghilmany.nufitai.domain.aicustomplan.entity.AiPlanGenerationRequest
import com.fghilmany.nufitai.domain.aicustomplan.repository.AiCustomPlanRepository

class GenerateAiPlan(private val repository: AiCustomPlanRepository) {
    suspend operator fun invoke(request: AiPlanGenerationRequest): AppResult<AiGeneratedPlanDraft> = repository.generatePlan(request)
}
