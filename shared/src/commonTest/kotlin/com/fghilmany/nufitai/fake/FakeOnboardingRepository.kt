package com.fghilmany.nufitai.fake

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.onboarding.entity.BodyMeasurement
import com.fghilmany.nufitai.domain.onboarding.entity.ParQResult
import com.fghilmany.nufitai.domain.onboarding.entity.QuickAssessmentResult
import com.fghilmany.nufitai.domain.onboarding.repository.OnboardingRepository
import kotlin.time.Instant

class FakeOnboardingRepository : OnboardingRepository {
    var savedParQResult: ParQResult? = null
        private set
    var savedQuickAssessmentResult: QuickAssessmentResult? = null
        private set
    var savedBodyMeasurement: BodyMeasurement? = null
        private set
    var acknowledgedAt: Instant? = null
        private set

    var latestParQResult: ParQResult? = null
    var latestQuickAssessmentResult: QuickAssessmentResult? = null
    var latestBodyMeasurement: BodyMeasurement? = null
    var hasLocalProfileValue: Boolean = false

    override suspend fun saveParQResult(result: ParQResult): AppResult<Unit> {
        savedParQResult = result
        latestParQResult = result
        return AppResult.Success(Unit)
    }

    override suspend fun acknowledgeDoctorConsult(parQResultId: String, acknowledgedAt: Instant): AppResult<Unit> {
        this.acknowledgedAt = acknowledgedAt
        return AppResult.Success(Unit)
    }

    override suspend fun getLatestParQResult(): AppResult<ParQResult?> = AppResult.Success(latestParQResult)

    override suspend fun saveQuickAssessmentResult(result: QuickAssessmentResult): AppResult<Unit> {
        savedQuickAssessmentResult = result
        latestQuickAssessmentResult = result
        return AppResult.Success(Unit)
    }

    override suspend fun getLatestQuickAssessmentResult(): AppResult<QuickAssessmentResult?> =
        AppResult.Success(latestQuickAssessmentResult)

    override suspend fun saveBodyMeasurement(measurement: BodyMeasurement): AppResult<Unit> {
        savedBodyMeasurement = measurement
        latestBodyMeasurement = measurement
        return AppResult.Success(Unit)
    }

    override suspend fun getLatestBodyMeasurement(): AppResult<BodyMeasurement?> =
        AppResult.Success(latestBodyMeasurement)

    override suspend fun hasLocalProfile(): AppResult<Boolean> = AppResult.Success(hasLocalProfileValue)
}
