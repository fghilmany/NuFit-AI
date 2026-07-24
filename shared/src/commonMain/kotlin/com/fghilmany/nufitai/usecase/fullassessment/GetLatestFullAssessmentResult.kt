package com.fghilmany.nufitai.usecase.fullassessment

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentResult
import com.fghilmany.nufitai.domain.fullassessment.repository.FullAssessmentRepository

class GetLatestFullAssessmentResult(private val repository: FullAssessmentRepository) {
    suspend operator fun invoke(): AppResult<FullAssessmentResult?> = repository.getLatestFullAssessmentResult()
}
