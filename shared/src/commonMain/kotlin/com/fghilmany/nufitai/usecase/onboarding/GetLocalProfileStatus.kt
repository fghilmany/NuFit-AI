package com.fghilmany.nufitai.usecase.onboarding

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.onboarding.repository.OnboardingRepository

/** Used by Splash routing (AC-1: new user -> PAR-Q, AC-8: returning user -> Home). */
class GetLocalProfileStatus(private val repository: OnboardingRepository) {
    suspend operator fun invoke(): AppResult<Boolean> = repository.hasLocalProfile()
}
