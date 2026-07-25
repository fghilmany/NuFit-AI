package com.fghilmany.nufitai.usecase.ptmode

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.generateId
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutExerciseLog
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSession
import com.fghilmany.nufitai.domain.ptmode.repository.PtModeRepository

/**
 * Resolves the WorkoutExerciseLog row for the slot the session is currently on -- creating
 * it (defaulting to the plan's target exerciseId) the first time that slot is reached. The
 * id returned here is reused by ConfirmSet/SkipExercise/SwapExercise so their writes upsert
 * the same row instead of creating duplicates for the same slot.
 */
class GetOrCreateExerciseLog(private val repository: PtModeRepository) {
    suspend operator fun invoke(session: WorkoutSession, plannedExerciseIndex: Int, defaultExerciseId: String): AppResult<WorkoutExerciseLog> {
        val existing = when (val result = repository.getExerciseLogsForSession(session.id)) {
            is AppResult.Error -> return result
            is AppResult.Success -> result.data.find { it.plannedExerciseIndex == plannedExerciseIndex }
        }
        if (existing != null) return AppResult.Success(existing)

        val newLog = WorkoutExerciseLog(
            id = generateId(),
            workoutSessionId = session.id,
            plannedExerciseIndex = plannedExerciseIndex,
            exerciseId = defaultExerciseId,
            skippedAt = null,
        )
        return when (val result = repository.upsertExerciseLog(newLog)) {
            is AppResult.Error -> result
            is AppResult.Success -> AppResult.Success(newLog)
        }
    }
}
