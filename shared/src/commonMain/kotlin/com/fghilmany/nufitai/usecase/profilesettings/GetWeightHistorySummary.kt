package com.fghilmany.nufitai.usecase.profilesettings

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.profilesettings.entity.WeightHistorySummary
import com.fghilmany.nufitai.usecase.onboarding.GetAllBodyMeasurements
import kotlin.time.Duration.Companion.days

private const val CHART_POINT_LIMIT = 6
private val DELTA_WINDOW = 30.days

/**
 * techspec profile-settings.md §9 item 4 -- `points` is the last [CHART_POINT_LIMIT] raw
 * measurements (chronological, oldest first), no weekly aggregation. The delta badge compares
 * the latest weight against the most recent measurement that is at least [DELTA_WINDOW] old;
 * `null` when no such measurement exists (fresh users, or <2 weight-bearing records).
 */
class GetWeightHistorySummary(private val getAllBodyMeasurements: GetAllBodyMeasurements) {
    suspend operator fun invoke(): AppResult<WeightHistorySummary> {
        val result = getAllBodyMeasurements()
        val allNewestFirst = when (result) {
            is AppResult.Error -> return result
            is AppResult.Success -> result.data.filter { it.weightKg != null }
        }

        if (allNewestFirst.isEmpty()) {
            return AppResult.Success(WeightHistorySummary(points = emptyList(), deltaKgVsThirtyDaysAgo = null))
        }

        val latest = allNewestFirst.first()
        val points = allNewestFirst.take(CHART_POINT_LIMIT).reversed()

        val thirtyDaysAgoOrOlder = allNewestFirst.firstOrNull { it.recordedAt <= latest.recordedAt - DELTA_WINDOW }
        val delta = thirtyDaysAgoOrOlder?.let { latest.weightKg!! - it.weightKg!! }

        return AppResult.Success(WeightHistorySummary(points = points, deltaKgVsThirtyDaysAgo = delta))
    }
}
