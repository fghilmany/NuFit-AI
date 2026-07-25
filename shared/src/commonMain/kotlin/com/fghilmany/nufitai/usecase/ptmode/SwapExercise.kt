package com.fghilmany.nufitai.usecase.ptmode

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutExerciseLog
import com.fghilmany.nufitai.domain.ptmode.repository.PtModeRepository

/**
 * AC-3: swap applies for this session only -- writes the substitute exerciseId onto the
 * session's own WorkoutExerciseLog row; PlanDay/PlannedExercise are never touched. Candidate
 * alternatives are sourced by the caller via the existing GetExerciseAlternatives usecase.
 */
class SwapExercise(private val repository: PtModeRepository) {
    suspend operator fun invoke(exerciseLog: WorkoutExerciseLog, newExerciseId: String): AppResult<WorkoutExerciseLog> {
        val updated = exerciseLog.copy(exerciseId = newExerciseId)
        return when (val result = repository.upsertExerciseLog(updated)) {
            is AppResult.Error -> result
            is AppResult.Success -> AppResult.Success(updated)
        }
    }
}
