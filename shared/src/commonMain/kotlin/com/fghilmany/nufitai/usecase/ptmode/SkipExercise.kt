package com.fghilmany.nufitai.usecase.ptmode

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutExerciseLog
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSession
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSessionStatus
import com.fghilmany.nufitai.domain.ptmode.repository.PtModeRepository

/**
 * AC-4/#7: abandons the rest of the current exercise (sets already logged for it stay
 * counted -- no deletion here) and advances to the next exercise. Skipping the last exercise
 * completes the session, mirroring ConfirmSet's isLastExercise handling -- otherwise the
 * session would never leave IN_PROGRESS.
 */
class SkipExercise(private val repository: PtModeRepository) {
    suspend operator fun invoke(session: WorkoutSession, exerciseLog: WorkoutExerciseLog, isLastExercise: Boolean): AppResult<WorkoutSession> {
        val now = currentInstant()
        val skippedLog = exerciseLog.copy(skippedAt = now)
        when (val logResult = repository.upsertExerciseLog(skippedLog)) {
            is AppResult.Error -> return logResult
            is AppResult.Success -> Unit
        }

        if (isLastExercise) {
            val completedSession = session.copy(status = WorkoutSessionStatus.COMPLETED, completedAt = now, restEndAt = null)
            return when (val result = repository.completeSession(completedSession)) {
                is AppResult.Error -> result
                is AppResult.Success -> AppResult.Success(completedSession)
            }
        }

        val updatedSession = session.copy(
            currentExerciseIndex = session.currentExerciseIndex + 1,
            currentSetNumber = 1,
            restEndAt = null,
        )
        return when (val result = repository.updateSessionProgress(updatedSession)) {
            is AppResult.Error -> result
            is AppResult.Success -> AppResult.Success(updatedSession)
        }
    }
}
