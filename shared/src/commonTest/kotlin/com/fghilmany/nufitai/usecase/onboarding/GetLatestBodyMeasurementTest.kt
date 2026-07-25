package com.fghilmany.nufitai.usecase.onboarding

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.onboarding.entity.BodyMeasurement
import com.fghilmany.nufitai.fake.FakeOnboardingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

class GetLatestBodyMeasurementTest {

    @Test
    fun `given a saved body measurement when invoked then returns it`() = runTest {
        val repository = FakeOnboardingRepository()
        val measurement = BodyMeasurement(id = "bm1", recordedAt = Instant.fromEpochMilliseconds(0), heightCm = 170.0, weightKg = 65.0)
        repository.latestBodyMeasurement = measurement
        val getLatestBodyMeasurement = GetLatestBodyMeasurement(repository)

        val result = getLatestBodyMeasurement()

        assertIs<AppResult.Success<BodyMeasurement?>>(result)
        assertEquals(measurement, result.data)
    }

    @Test
    fun `given no body measurement when invoked then returns null`() = runTest {
        val repository = FakeOnboardingRepository()
        val getLatestBodyMeasurement = GetLatestBodyMeasurement(repository)

        val result = getLatestBodyMeasurement()

        assertIs<AppResult.Success<BodyMeasurement?>>(result)
        assertEquals(null, result.data)
    }
}
