package com.fghilmany.nufitai.usecase.onboarding

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.onboarding.entity.BodyMeasurement
import com.fghilmany.nufitai.domain.onboarding.repository.OnboardingRepository

/** Used by `usecase/assessmentdetail/GetAssessmentDetail` for the Full-tier BMI display. */
class GetLatestBodyMeasurement(private val repository: OnboardingRepository) {
    suspend operator fun invoke(): AppResult<BodyMeasurement?> = repository.getLatestBodyMeasurement()
}
