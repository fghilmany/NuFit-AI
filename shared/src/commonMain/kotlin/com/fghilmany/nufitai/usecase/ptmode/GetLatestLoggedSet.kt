package com.fghilmany.nufitai.usecase.ptmode

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSetLog
import com.fghilmany.nufitai.domain.ptmode.repository.PtModeRepository

/** Edge case #2: default weight/reps for a set fall back to the most recent logged actual for that exercise, if any. */
class GetLatestLoggedSet(private val repository: PtModeRepository) {
    suspend operator fun invoke(exerciseId: String): AppResult<WorkoutSetLog?> {
        return when (val result = repository.getSetLogsForExercise(exerciseId)) {
            is AppResult.Error -> result
            is AppResult.Success -> AppResult.Success(result.data.maxByOrNull { it.completedAt })
        }
    }
}
