package com.fghilmany.nufitai.domain.ptmode.entity

/** issue #80 P-06 -- derived from WorkoutSetLog/WorkoutExerciseLog, never stored as-is. */
data class WorkoutSummary(
    val totalVolumeKg: Double,
    val durationMinutes: Int,
    val completedExerciseCount: Int,
    val totalExerciseCount: Int,
    val personalRecords: List<PersonalRecord>,
)
