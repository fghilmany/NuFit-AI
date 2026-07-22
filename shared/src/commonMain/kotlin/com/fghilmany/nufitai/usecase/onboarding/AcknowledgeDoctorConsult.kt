package com.fghilmany.nufitai.usecase.onboarding

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.domain.onboarding.repository.OnboardingRepository

class AcknowledgeDoctorConsult(private val repository: OnboardingRepository) {
    suspend operator fun invoke(parQResultId: String): AppResult<Unit> =
        repository.acknowledgeDoctorConsult(parQResultId, currentInstant())
}
