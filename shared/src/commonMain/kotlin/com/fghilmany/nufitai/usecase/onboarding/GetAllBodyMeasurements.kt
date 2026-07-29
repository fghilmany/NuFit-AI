package com.fghilmany.nufitai.usecase.onboarding

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.onboarding.entity.BodyMeasurement
import com.fghilmany.nufitai.domain.onboarding.repository.OnboardingRepository

/** Used by `usecase/profilesettings/GetWeightHistorySummary` for P-15's weight history chart. */
class GetAllBodyMeasurements(private val repository: OnboardingRepository) {
    suspend operator fun invoke(): AppResult<List<BodyMeasurement>> = repository.getAllBodyMeasurements()
}
