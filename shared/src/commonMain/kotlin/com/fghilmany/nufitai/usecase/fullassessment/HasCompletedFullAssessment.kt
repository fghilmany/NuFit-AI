package com.fghilmany.nufitai.usecase.fullassessment

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.fullassessment.repository.FullAssessmentRepository

class HasCompletedFullAssessment(private val repository: FullAssessmentRepository) {
    suspend operator fun invoke(): AppResult<Boolean> = repository.hasCompletedFullAssessment()
}
