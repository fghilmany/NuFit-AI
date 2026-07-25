package com.fghilmany.nufitai.usecase.ptmode

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.ptmode.entity.PersonalRecord
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSession
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSummary
import com.fghilmany.nufitai.domain.ptmode.repository.PtModeRepository

/**
 * AC-4/#6: derives P-06's stats purely from the logs -- volume only ever sums logged sets
 * (skipped exercises simply contribute none/fewer), completed count is exercises never
 * skipped (structurally guaranteed by the state machine: a slot only stops being "current"
 * by finishing all its sets or by an explicit skip), PR = this session's max weight for an
 * exercise strictly exceeding the best weight logged for it before this session started.
 */
class GetWorkoutSummary(private val repository: PtModeRepository) {
    suspend operator fun invoke(session: WorkoutSession, exercisesById: Map<String, Exercise>, totalExerciseCount: Int): AppResult<WorkoutSummary> {
        val exerciseLogs = when (val result = repository.getExerciseLogsForSession(session.id)) {
            is AppResult.Error -> return result
            is AppResult.Success -> result.data
        }
        val setLogs = when (val result = repository.getSetLogsForSession(session.id)) {
            is AppResult.Error -> return result
            is AppResult.Success -> result.data
        }
        val setLogsByExerciseLogId = setLogs.groupBy { it.workoutExerciseLogId }

        val totalVolume = setLogs.sumOf { it.weightKg * it.reps }
        val durationMinutes = ((session.completedAt ?: currentInstant()) - session.startedAt).inWholeMinutes.toInt()
        val completedExerciseCount = exerciseLogs.count { it.skippedAt == null }

        val personalRecords = mutableListOf<PersonalRecord>()
        for (exerciseLog in exerciseLogs) {
            val setsThisSession = setLogsByExerciseLogId[exerciseLog.id].orEmpty()
            if (setsThisSession.isEmpty()) continue
            val maxThisSession = setsThisSession.maxOf { it.weightKg }

            val history = when (val result = repository.getSetLogsForExercise(exerciseLog.exerciseId)) {
                is AppResult.Error -> return result
                is AppResult.Success -> result.data
            }
            val priorBest = history.filter { it.completedAt < session.startedAt }.maxOfOrNull { it.weightKg }

            if (priorBest != null && maxThisSession > priorBest) {
                personalRecords += PersonalRecord(
                    exerciseId = exerciseLog.exerciseId,
                    exerciseName = exercisesById[exerciseLog.exerciseId]?.name ?: exerciseLog.exerciseId,
                    newWeightKg = maxThisSession,
                    previousBestKg = priorBest,
                )
            }
        }

        return AppResult.Success(
            WorkoutSummary(
                totalVolumeKg = totalVolume,
                durationMinutes = durationMinutes,
                completedExerciseCount = completedExerciseCount,
                totalExerciseCount = totalExerciseCount,
                personalRecords = personalRecords,
            ),
        )
    }
}
