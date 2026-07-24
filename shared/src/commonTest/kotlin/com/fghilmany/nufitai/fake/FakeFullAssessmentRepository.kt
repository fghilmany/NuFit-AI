package com.fghilmany.nufitai.fake

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentResult
import com.fghilmany.nufitai.domain.fullassessment.repository.FullAssessmentRepository

class FakeFullAssessmentRepository : FullAssessmentRepository {
    var savedResult: FullAssessmentResult? = null
        private set
    var latestResult: FullAssessmentResult? = null

    override suspend fun saveFullAssessmentResult(result: FullAssessmentResult): AppResult<Unit> {
        savedResult = result
        latestResult = result
        return AppResult.Success(Unit)
    }

    override suspend fun getLatestFullAssessmentResult(): AppResult<FullAssessmentResult?> = AppResult.Success(latestResult)

    override suspend fun hasCompletedFullAssessment(): AppResult<Boolean> = AppResult.Success(latestResult != null)
}
