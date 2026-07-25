package com.fghilmany.nufitai.domain.ptmode.entity

import kotlin.time.Instant

/** issue #80 -- one row per confirmed set (DoD: "Log otomatis tersimpan per set"). */
data class WorkoutSetLog(
    val id: String,
    val workoutExerciseLogId: String,
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val completedAt: Instant,
)
