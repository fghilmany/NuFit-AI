package com.fghilmany.nufitai.domain.onboarding.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.onboarding.entity.BodyMeasurement
import com.fghilmany.nufitai.domain.onboarding.entity.ParQResult
import com.fghilmany.nufitai.domain.onboarding.entity.QuickAssessmentResult
import kotlin.time.Instant

interface OnboardingRepository {
    suspend fun saveParQResult(result: ParQResult): AppResult<Unit>
    suspend fun acknowledgeDoctorConsult(parQResultId: String, acknowledgedAt: Instant): AppResult<Unit>
    suspend fun getLatestParQResult(): AppResult<ParQResult?>

    suspend fun saveQuickAssessmentResult(result: QuickAssessmentResult): AppResult<Unit>
    suspend fun getLatestQuickAssessmentResult(): AppResult<QuickAssessmentResult?>

    suspend fun saveBodyMeasurement(measurement: BodyMeasurement): AppResult<Unit>
    suspend fun getLatestBodyMeasurement(): AppResult<BodyMeasurement?>

    suspend fun hasLocalProfile(): AppResult<Boolean>
}
