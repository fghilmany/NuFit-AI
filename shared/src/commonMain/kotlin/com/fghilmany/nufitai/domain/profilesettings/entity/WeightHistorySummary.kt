package com.fghilmany.nufitai.domain.profilesettings.entity

import com.fghilmany.nufitai.domain.onboarding.entity.BodyMeasurement

/**
 * techspec profile-settings.md §5 -- presentation-facing projection over [BodyMeasurement]
 * history, not persisted itself. [points] is chronological (oldest first), capped at 6, and only
 * includes rows with a non-null [BodyMeasurement.weightKg].
 */
data class WeightHistorySummary(
    val points: List<BodyMeasurement>,
    val deltaKgVsThirtyDaysAgo: Double?,
)
