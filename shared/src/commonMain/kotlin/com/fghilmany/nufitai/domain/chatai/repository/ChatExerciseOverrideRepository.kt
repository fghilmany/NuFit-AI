package com.fghilmany.nufitai.domain.chatai.repository

import com.fghilmany.nufitai.core.error.AppResult

/**
 * techspec chat-ai.md §5/§7 -- "Terapkan ke Plan"'s pending, single-use substitution. Consumed by
 * `usecase/ptmode/GetOrCreateExerciseLog` the first time a session reaches that slot.
 */
interface ChatExerciseOverrideRepository {
    suspend fun createOverride(planDayId: String, plannedExerciseIndex: Int, substituteExerciseId: String): AppResult<Unit>

    /** Returns the substitute exerciseId and deletes the override row, or null if none exists for this slot. */
    suspend fun consumeOverride(planDayId: String, plannedExerciseIndex: Int): AppResult<String?>
}
