package com.fghilmany.nufitai.domain.fullassessment.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentResult

interface FullAssessmentRepository {
    suspend fun saveFullAssessmentResult(result: FullAssessmentResult): AppResult<Unit>
    suspend fun getLatestFullAssessmentResult(): AppResult<FullAssessmentResult?>
    suspend fun hasCompletedFullAssessment(): AppResult<Boolean>
}
