package com.fghilmany.nufitai.usecase.ptmode

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.core.util.generateId
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutExerciseLog
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSession
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSessionStatus
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSetLog
import com.fghilmany.nufitai.domain.ptmode.repository.PtModeRepository
import kotlin.time.Duration.Companion.seconds

/**
 * AC-1: confirming a set logs it and starts the rest timer (wall-clock restEndAt). Finishing
 * the last set of the last exercise completes the session instead of resting (issue #80 §5).
 */
class ConfirmSet(private val repository: PtModeRepository) {
    suspend operator fun invoke(
        session: WorkoutSession,
        exerciseLog: WorkoutExerciseLog,
        weightKg: Double,
        reps: Int,
        totalSetsForExercise: Int,
        restSeconds: Int,
        isLastExercise: Boolean,
    ): AppResult<WorkoutSession> {
        val now = currentInstant()
        val setLog = WorkoutSetLog(
            id = generateId(),
            workoutExerciseLogId = exerciseLog.id,
            setNumber = session.currentSetNumber,
            weightKg = weightKg,
            reps = reps,
            completedAt = now,
        )
        when (val insertResult = repository.insertSetLog(setLog)) {
            is AppResult.Error -> return insertResult
            is AppResult.Success -> Unit
        }

        val finishedExercise = session.currentSetNumber >= totalSetsForExercise

        if (finishedExercise && isLastExercise) {
            val completedSession = session.copy(status = WorkoutSessionStatus.COMPLETED, completedAt = now, restEndAt = null)
            return when (val result = repository.completeSession(completedSession)) {
                is AppResult.Error -> result
                is AppResult.Success -> AppResult.Success(completedSession)
            }
        }

        val updatedSession = if (finishedExercise) {
            session.copy(currentExerciseIndex = session.currentExerciseIndex + 1, currentSetNumber = 1, restEndAt = now.plus(restSeconds.seconds))
        } else {
            session.copy(currentSetNumber = session.currentSetNumber + 1, restEndAt = now.plus(restSeconds.seconds))
        }
        return when (val result = repository.updateSessionProgress(updatedSession)) {
            is AppResult.Error -> result
            is AppResult.Success -> AppResult.Success(updatedSession)
        }
    }
}
