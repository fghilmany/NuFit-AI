package com.fghilmany.nufitai.domain.ptmode.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutExerciseLog
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSession
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSetLog

interface PtModeRepository {
    /** AC-2/AC-7/#14: at most one IN_PROGRESS session per planDayId. */
    suspend fun getInProgressSession(planDayId: String): AppResult<WorkoutSession?>

    /** Most recent session for a day regardless of status -- P-06 looks the finished session up by planDayId. */
    suspend fun getLatestSessionForDay(planDayId: String): AppResult<WorkoutSession?>
    suspend fun createSession(session: WorkoutSession): AppResult<Unit>
    suspend fun updateSessionProgress(session: WorkoutSession): AppResult<Unit>
    suspend fun completeSession(session: WorkoutSession): AppResult<Unit>

    /** Upsert keyed by (workoutSessionId, plannedExerciseIndex) -- covers first-touch, swap (AC-3), and skip (AC-4). */
    suspend fun upsertExerciseLog(log: WorkoutExerciseLog): AppResult<Unit>
    suspend fun getExerciseLogsForSession(workoutSessionId: String): AppResult<List<WorkoutExerciseLog>>

    suspend fun insertSetLog(log: WorkoutSetLog): AppResult<Unit>
    suspend fun getSetLogsForSession(workoutSessionId: String): AppResult<List<WorkoutSetLog>>

    /** Full history for one exercise across all sessions -- default-value fallback (#2) and PR comparison (#6). */
    suspend fun getSetLogsForExercise(exerciseId: String): AppResult<List<WorkoutSetLog>>
}
