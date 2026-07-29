package com.fghilmany.nufitai.usecase.profilesettings

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.onboarding.entity.BodyMeasurement
import com.fghilmany.nufitai.fake.FakeOnboardingRepository
import com.fghilmany.nufitai.usecase.onboarding.GetAllBodyMeasurements
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class GetWeightHistorySummaryTest {
    private val now = Instant.fromEpochMilliseconds(1_000_000_000_000)

    private fun measurement(id: String, daysAgo: Int, weightKg: Double?) = BodyMeasurement(
        id = id, recordedAt = now - daysAgo.days, heightCm = null, weightKg = weightKg,
    )

    @Test
    fun `given no measurements when invoked then points and delta are empty`() = runTest {
        val repository = FakeOnboardingRepository()
        val usecase = GetWeightHistorySummary(GetAllBodyMeasurements(repository))

        val result = usecase()

        assertIs<AppResult.Success<*>>(result)
        assertEquals(emptyList(), result.data.points)
        assertNull(result.data.deltaKgVsThirtyDaysAgo)
    }

    @Test
    fun `given more than 6 measurements when invoked then points is capped at the 6 most recent, oldest first`() = runTest {
        val repository = FakeOnboardingRepository()
        // Fake returns newest-first, mirroring the real ORDER BY recorded_at DESC.
        repository.allBodyMeasurements = (0..7).map { measurement("m$it", daysAgo = it, weightKg = 70.0 + it) }
        val usecase = GetWeightHistorySummary(GetAllBodyMeasurements(repository))

        val result = usecase()

        assertIs<AppResult.Success<*>>(result)
        assertEquals(6, result.data.points.size)
        assertEquals("m5", result.data.points.first().id) // oldest of the 6 kept
        assertEquals("m0", result.data.points.last().id) // most recent
    }

    @Test
    fun `given a measurement exactly 30 days old when invoked then delta compares against it`() = runTest {
        val repository = FakeOnboardingRepository()
        repository.allBodyMeasurements = listOf(
            measurement("latest", daysAgo = 0, weightKg = 68.0),
            measurement("old", daysAgo = 30, weightKg = 70.0),
        )
        val usecase = GetWeightHistorySummary(GetAllBodyMeasurements(repository))

        val result = usecase()

        assertIs<AppResult.Success<*>>(result)
        assertEquals(-2.0, result.data.deltaKgVsThirtyDaysAgo)
    }

    @Test
    fun `given no measurement older than 30 days when invoked then delta is null`() = runTest {
        val repository = FakeOnboardingRepository()
        repository.allBodyMeasurements = listOf(
            measurement("latest", daysAgo = 0, weightKg = 68.0),
            measurement("recent", daysAgo = 5, weightKg = 69.0),
        )
        val usecase = GetWeightHistorySummary(GetAllBodyMeasurements(repository))

        val result = usecase()

        assertIs<AppResult.Success<*>>(result)
        assertNull(result.data.deltaKgVsThirtyDaysAgo)
    }

    @Test
    fun `given measurements with null weight when invoked then they are excluded from points`() = runTest {
        val repository = FakeOnboardingRepository()
        repository.allBodyMeasurements = listOf(
            measurement("withWeight", daysAgo = 0, weightKg = 68.0),
            measurement("heightOnly", daysAgo = 1, weightKg = null),
        )
        val usecase = GetWeightHistorySummary(GetAllBodyMeasurements(repository))

        val result = usecase()

        assertIs<AppResult.Success<*>>(result)
        assertEquals(1, result.data.points.size)
        assertEquals("withWeight", result.data.points.single().id)
    }
}
