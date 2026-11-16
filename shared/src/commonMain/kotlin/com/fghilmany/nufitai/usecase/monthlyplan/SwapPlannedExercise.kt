package com.fghilmany.nufitai.usecase.monthlyplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository

/** Session Detail's "Ganti gerakan" (pre-session) -- applies to this one day only, mirrors
 * PT Mode's `SwapExercise` but writes onto the plan itself (no WorkoutExerciseLog exists yet). */
class SwapPlannedExercise(private val repository: MonthlyPlanRepository) {
    suspend operator fun invoke(planDayId: String, oldExerciseId: String, newExerciseId: String): AppResult<Unit> =
        repository.swapMainExercise(planDayId, oldExerciseId, newExerciseId)
}
