package com.fghilmany.nufitai.usecase.ptmode

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.generateId
import com.fghilmany.nufitai.domain.chatai.repository.ChatExerciseOverrideRepository
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutExerciseLog
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSession
import com.fghilmany.nufitai.domain.ptmode.repository.PtModeRepository

/**
 * Resolves the WorkoutExerciseLog row for the slot the session is currently on -- creating
 * it the first time that slot is reached. The id returned here is reused by
 * ConfirmSet/SkipExercise/SwapExercise so their writes upsert the same row instead of creating
 * duplicates for the same slot.
 *
 * techspec chat-ai.md §5/§7 -- before defaulting to the plan's own [defaultExerciseId], checks
 * for a pending "Terapkan ke Plan" override for this exact (planDayId, plannedExerciseIndex)
 * slot and consumes it (single-use, deleted once read) if present. `PlanDay.mainExercises`
 * itself is never touched -- this mirrors `SwapExercise`'s existing session-scoped-only semantics.
 */
class GetOrCreateExerciseLog(
    private val repository: PtModeRepository,
    private val overrideRepository: ChatExerciseOverrideRepository,
) {
    suspend operator fun invoke(session: WorkoutSession, plannedExerciseIndex: Int, defaultExerciseId: String): AppResult<WorkoutExerciseLog> {
        val existing = when (val result = repository.getExerciseLogsForSession(session.id)) {
            is AppResult.Error -> return result
            is AppResult.Success -> result.data.find { it.plannedExerciseIndex == plannedExerciseIndex }
        }
        if (existing != null) return AppResult.Success(existing)

        val resolvedExerciseId = when (val result = overrideRepository.consumeOverride(session.planDayId, plannedExerciseIndex)) {
            is AppResult.Error -> return result
            is AppResult.Success -> result.data ?: defaultExerciseId
        }

        val newLog = WorkoutExerciseLog(
            id = generateId(),
            workoutSessionId = session.id,
            plannedExerciseIndex = plannedExerciseIndex,
            exerciseId = resolvedExerciseId,
            skippedAt = null,
        )
        return when (val result = repository.upsertExerciseLog(newLog)) {
            is AppResult.Error -> result
            is AppResult.Success -> AppResult.Success(newLog)
        }
    }
}
