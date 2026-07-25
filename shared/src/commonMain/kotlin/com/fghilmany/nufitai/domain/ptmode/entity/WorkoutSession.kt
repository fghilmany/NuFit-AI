package com.fghilmany.nufitai.domain.ptmode.entity

import kotlin.time.Instant

enum class WorkoutSessionStatus { IN_PROGRESS, COMPLETED }

/**
 * issue #80 P-05/P-06 -- one row per guided-workout attempt at a [planDayId]. At most one
 * IN_PROGRESS row exists per plan day at a time (GetOrCreateWorkoutSession enforces this),
 * so re-pressing Start resumes rather than duplicates (AC-2/AC-7).
 */
data class WorkoutSession(
    val id: String,
    val planDayId: String,
    val startedAt: Instant,
    val completedAt: Instant?,
    val status: WorkoutSessionStatus,
    /** Index into PlanDay.mainExercises -- drives which exercise P-05 shows on resume. */
    val currentExerciseIndex: Int,
    val currentSetNumber: Int,
    /** Wall-clock target for the active rest countdown; null when not resting (AC-1). */
    val restEndAt: Instant?,
)
