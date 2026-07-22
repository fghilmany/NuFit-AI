package com.fghilmany.nufitai.usecase.onboarding

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.onboarding.entity.ParQResult
import com.fghilmany.nufitai.domain.onboarding.repository.OnboardingRepository

/** Used by the consult-doctor screen to load the flags/answers from the just-submitted PAR-Q result. */
class GetLatestParQResult(private val repository: OnboardingRepository) {
    suspend operator fun invoke(): AppResult<ParQResult?> = repository.getLatestParQResult()
}
