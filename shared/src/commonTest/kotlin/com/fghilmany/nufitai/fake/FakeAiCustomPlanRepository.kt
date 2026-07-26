package com.fghilmany.nufitai.fake

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.domain.aicustomplan.entity.AiGeneratedPlanDraft
import com.fghilmany.nufitai.domain.aicustomplan.entity.AiPlanGenerationRequest
import com.fghilmany.nufitai.domain.aicustomplan.repository.AiCustomPlanRepository

class FakeAiCustomPlanRepository : AiCustomPlanRepository {
    var draftToReturn: AiGeneratedPlanDraft? = null
    var failureToReturn: Failure? = null
    var lastRequest: AiPlanGenerationRequest? = null

    override suspend fun generatePlan(request: AiPlanGenerationRequest): AppResult<AiGeneratedPlanDraft> {
        lastRequest = request
        failureToReturn?.let { return AppResult.Error(it) }
        return AppResult.Success(draftToReturn ?: error("draftToReturn not set"))
    }
}
