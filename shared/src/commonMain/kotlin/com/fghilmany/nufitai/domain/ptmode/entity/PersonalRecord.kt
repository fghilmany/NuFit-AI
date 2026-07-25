package com.fghilmany.nufitai.domain.ptmode.entity

/**
 * issue #80 P-06 -- pure derived value, not persisted separately. PR = heaviest single set
 * logged for [exerciseId] this session, strictly exceeding [previousBestKg] (best set from
 * before this session; null means no prior history, not treated as a PR).
 */
data class PersonalRecord(
    val exerciseId: String,
    val exerciseName: String,
    val newWeightKg: Double,
    val previousBestKg: Double?,
)
