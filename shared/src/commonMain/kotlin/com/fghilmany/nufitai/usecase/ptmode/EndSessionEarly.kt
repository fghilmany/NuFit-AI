package com.fghilmany.nufitai.usecase.ptmode

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutExerciseLog
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSession
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSessionStatus
import com.fghilmany.nufitai.domain.ptmode.repository.PtModeRepository

/**
 * AC-8/edge-case#3: explicit "End Session Now" -- marks every not-yet-finished exercise slot
 * skipped, then finalizes the session as COMPLETED with partial data.
 */
class EndSessionEarly(private val repository: PtModeRepository) {
    suspend operator fun invoke(
        session: WorkoutSession,
        /**
         * The current slot's existing WorkoutExerciseLog (its id is reused so any sets already
         * logged against it stay attached -- decision #7) plus a freshly-built, not-yet-persisted
         * WorkoutExerciseLog for every slot after it that was never reached.
         */
        exerciseLogsToClose: List<WorkoutExerciseLog>,
    ): AppResult<WorkoutSession> {
        val now = currentInstant()
        for (log in exerciseLogsToClose) {
            val skipLog = log.copy(skippedAt = log.skippedAt ?: now)
            when (val result = repository.upsertExerciseLog(skipLog)) {
                is AppResult.Error -> return result
                is AppResult.Success -> Unit
            }
        }

        val completedSession = session.copy(status = WorkoutSessionStatus.COMPLETED, completedAt = now, restEndAt = null)
        return when (val result = repository.completeSession(completedSession)) {
            is AppResult.Error -> result
            is AppResult.Success -> AppResult.Success(completedSession)
        }
    }
}
