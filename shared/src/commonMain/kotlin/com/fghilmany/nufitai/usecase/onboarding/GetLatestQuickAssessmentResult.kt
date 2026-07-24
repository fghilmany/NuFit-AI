package com.fghilmany.nufitai.usecase.onboarding

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.onboarding.entity.QuickAssessmentResult
import com.fghilmany.nufitai.domain.onboarding.repository.OnboardingRepository

/** Used by `usecase/monthlyplan` to read the user's `Level` (Beginner/Intermediate) when generating a plan. */
class GetLatestQuickAssessmentResult(private val repository: OnboardingRepository) {
    suspend operator fun invoke(): AppResult<QuickAssessmentResult?> = repository.getLatestQuickAssessmentResult()
}
