package com.fghilmany.nufitai.usecase.onboarding

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.core.util.generateId
import com.fghilmany.nufitai.domain.onboarding.entity.BodyMeasurement
import com.fghilmany.nufitai.domain.onboarding.repository.OnboardingRepository

class SaveBodyMeasurement(private val repository: OnboardingRepository) {
    suspend operator fun invoke(heightCm: Double?, weightKg: Double?): AppResult<Unit> {
        val measurement = BodyMeasurement(
            id = generateId(),
            recordedAt = currentInstant(),
            heightCm = heightCm,
            weightKg = weightKg,
        )
        return repository.saveBodyMeasurement(measurement)
    }
}
