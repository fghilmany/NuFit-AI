package com.fghilmany.nufitai.domain.ptmode.entity

import kotlin.time.Instant

/**
 * issue #80 -- one row per exercise slot actually attempted this session. Carries the
 * substitution (AC-3: exerciseId may differ from the plan's target, PlannedExercise itself
 * is never written to) and the skip flag (AC-4).
 */
data class WorkoutExerciseLog(
    val id: String,
    val workoutSessionId: String,
    /** Stable position in PlanDay.mainExercises, independent of substitution. */
    val plannedExerciseIndex: Int,
    /** Actual exercise performed -- equals the plan's target unless swapped. */
    val exerciseId: String,
    val skippedAt: Instant?,
)
