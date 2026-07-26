package com.fghilmany.nufitai.domain.aicustomplan.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.aicustomplan.entity.AiGeneratedPlanDraft
import com.fghilmany.nufitai.domain.aicustomplan.entity.AiPlanGenerationRequest

interface AiCustomPlanRepository {
    /** AC-3: caller enforces the <=30s budget; implementation applies a client-side timeout. */
    suspend fun generatePlan(request: AiPlanGenerationRequest): AppResult<AiGeneratedPlanDraft>
}
