package com.fghilmany.nufitai.data.ptmode.datasource

import com.fghilmany.nufitai.db.NuFitDatabase
import com.fghilmany.nufitai.db.Workout_exercise_log as WorkoutExerciseLogRow
import com.fghilmany.nufitai.db.Workout_session as WorkoutSessionRow
import com.fghilmany.nufitai.db.Workout_set_log as WorkoutSetLogRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PtModeLocalDataSource(private val database: NuFitDatabase) {

    suspend fun getInProgressSessionForDay(planDayId: String): WorkoutSessionRow? = withContext(Dispatchers.Default) {
        database.workoutSessionQueries.getInProgressSessionForDay(planDayId).executeAsOneOrNull()
    }

    suspend fun getLatestSessionForDay(planDayId: String): WorkoutSessionRow? = withContext(Dispatchers.Default) {
        database.workoutSessionQueries.getLatestSessionForDay(planDayId).executeAsOneOrNull()
    }

    suspend fun upsertSession(
        id: String,
        planDayId: String,
        startedAt: Long,
        completedAt: Long?,
        status: String,
        currentExerciseIndex: Long,
        currentSetNumber: Long,
        restEndAt: Long?,
        updatedAt: Long,
    ) = withContext(Dispatchers.Default) {
        database.workoutSessionQueries.insertWorkoutSession(
            id = id,
            plan_day_id = planDayId,
            started_at = startedAt,
            completed_at = completedAt,
            status = status,
            current_exercise_index = currentExerciseIndex,
            current_set_number = currentSetNumber,
            rest_end_at = restEndAt,
            user_id = null,
            updated_at = updatedAt,
        )
    }

    suspend fun upsertExerciseLog(
        id: String,
        workoutSessionId: String,
        plannedExerciseIndex: Long,
        exerciseId: String,
        skippedAt: Long?,
        updatedAt: Long,
    ) = withContext(Dispatchers.Default) {
        database.workoutExerciseLogQueries.insertWorkoutExerciseLog(
            id = id,
            workout_session_id = workoutSessionId,
            planned_exercise_index = plannedExerciseIndex,
            exercise_id = exerciseId,
            skipped_at = skippedAt,
            user_id = null,
            updated_at = updatedAt,
        )
    }

    suspend fun getExerciseLogsForSession(workoutSessionId: String): List<WorkoutExerciseLogRow> = withContext(Dispatchers.Default) {
        database.workoutExerciseLogQueries.getExerciseLogsForSession(workoutSessionId).executeAsList()
    }

    suspend fun insertSetLog(
        id: String,
        workoutExerciseLogId: String,
        setNumber: Long,
        weightKg: Double,
        reps: Long,
        completedAt: Long,
        updatedAt: Long,
    ) = withContext(Dispatchers.Default) {
        database.workoutSetLogQueries.insertWorkoutSetLog(
            id = id,
            workout_exercise_log_id = workoutExerciseLogId,
            set_number = setNumber,
            weight_kg = weightKg,
            reps = reps,
            completed_at = completedAt,
            user_id = null,
            updated_at = updatedAt,
        )
    }

    suspend fun getSetLogsForSession(workoutSessionId: String): List<WorkoutSetLogRow> = withContext(Dispatchers.Default) {
        database.workoutSetLogQueries.getSetLogsForSession(workoutSessionId).executeAsList()
    }

    suspend fun getSetLogsForExercise(exerciseId: String): List<WorkoutSetLogRow> = withContext(Dispatchers.Default) {
        database.workoutSetLogQueries.getSetLogsForExercise(exerciseId).executeAsList()
    }

    /**
     * techspec chat-ai.md §7 -- sync engine cursor query for `workout_session` (the aggregate
     * parent). Scope note: nested `workout_exercise_log`/`workout_set_log`/`plan_day_session_log`
     * sync is a follow-up, same simplification `MonthlyPlanSyncParticipant` already carries for
     * its own nested `plan_day`/`planned_exercise` children.
     */
    suspend fun getChangedSessionsSince(updatedAfter: Long): List<WorkoutSessionRow> = withContext(Dispatchers.Default) {
        database.workoutSessionQueries.selectChangedSince(updatedAfter).executeAsList()
    }

    suspend fun getSessionById(id: String): WorkoutSessionRow? = withContext(Dispatchers.Default) {
        database.workoutSessionQueries.selectById(id).executeAsOneOrNull()
    }

    suspend fun backfillSessionUserId(userId: String) = withContext(Dispatchers.Default) {
        database.workoutSessionQueries.backfillUserId(userId)
    }

    /** techspec chat-ai.md §7 -- remote-sync upsert; unlike [upsertSession] (local writes, always `user_id = null`), this preserves the remote row's `user_id`. */
    suspend fun upsertSessionFromRemote(row: WorkoutSessionRow) = withContext(Dispatchers.Default) {
        database.workoutSessionQueries.insertWorkoutSession(
            id = row.id,
            plan_day_id = row.plan_day_id,
            started_at = row.started_at,
            completed_at = row.completed_at,
            status = row.status,
            current_exercise_index = row.current_exercise_index,
            current_set_number = row.current_set_number,
            rest_end_at = row.rest_end_at,
            user_id = row.user_id,
            updated_at = row.updated_at,
        )
    }
}
