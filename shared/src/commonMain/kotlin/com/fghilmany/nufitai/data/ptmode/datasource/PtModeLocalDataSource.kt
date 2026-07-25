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
        )
    }

    suspend fun upsertExerciseLog(
        id: String,
        workoutSessionId: String,
        plannedExerciseIndex: Long,
        exerciseId: String,
        skippedAt: Long?,
    ) = withContext(Dispatchers.Default) {
        database.workoutExerciseLogQueries.insertWorkoutExerciseLog(
            id = id,
            workout_session_id = workoutSessionId,
            planned_exercise_index = plannedExerciseIndex,
            exercise_id = exerciseId,
            skipped_at = skippedAt,
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
    ) = withContext(Dispatchers.Default) {
        database.workoutSetLogQueries.insertWorkoutSetLog(
            id = id,
            workout_exercise_log_id = workoutExerciseLogId,
            set_number = setNumber,
            weight_kg = weightKg,
            reps = reps,
            completed_at = completedAt,
        )
    }

    suspend fun getSetLogsForSession(workoutSessionId: String): List<WorkoutSetLogRow> = withContext(Dispatchers.Default) {
        database.workoutSetLogQueries.getSetLogsForSession(workoutSessionId).executeAsList()
    }

    suspend fun getSetLogsForExercise(exerciseId: String): List<WorkoutSetLogRow> = withContext(Dispatchers.Default) {
        database.workoutSetLogQueries.getSetLogsForExercise(exerciseId).executeAsList()
    }
}
