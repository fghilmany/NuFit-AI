package com.fghilmany.nufitai.data.ptmode.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.runCatchingDatabase
import com.fghilmany.nufitai.data.ptmode.datasource.PtModeLocalDataSource
import com.fghilmany.nufitai.db.Workout_exercise_log as WorkoutExerciseLogRow
import com.fghilmany.nufitai.db.Workout_session as WorkoutSessionRow
import com.fghilmany.nufitai.db.Workout_set_log as WorkoutSetLogRow
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutExerciseLog
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSession
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSessionStatus
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSetLog
import com.fghilmany.nufitai.domain.ptmode.repository.PtModeRepository
import kotlin.time.Instant

class PtModeRepositoryImpl(private val local: PtModeLocalDataSource) : PtModeRepository {

    override suspend fun getInProgressSession(planDayId: String): AppResult<WorkoutSession?> = runCatchingDatabase {
        local.getInProgressSessionForDay(planDayId)?.toEntity()
    }

    override suspend fun getLatestSessionForDay(planDayId: String): AppResult<WorkoutSession?> = runCatchingDatabase {
        local.getLatestSessionForDay(planDayId)?.toEntity()
    }

    override suspend fun createSession(session: WorkoutSession): AppResult<Unit> = runCatchingDatabase {
        local.upsertSession(
            id = session.id,
            planDayId = session.planDayId,
            startedAt = session.startedAt.toEpochMilliseconds(),
            completedAt = session.completedAt?.toEpochMilliseconds(),
            status = session.status.name,
            currentExerciseIndex = session.currentExerciseIndex.toLong(),
            currentSetNumber = session.currentSetNumber.toLong(),
            restEndAt = session.restEndAt?.toEpochMilliseconds(),
        )
    }

    override suspend fun updateSessionProgress(session: WorkoutSession): AppResult<Unit> = createSession(session)

    override suspend fun completeSession(session: WorkoutSession): AppResult<Unit> = createSession(session)

    override suspend fun upsertExerciseLog(log: WorkoutExerciseLog): AppResult<Unit> = runCatchingDatabase {
        local.upsertExerciseLog(
            id = log.id,
            workoutSessionId = log.workoutSessionId,
            plannedExerciseIndex = log.plannedExerciseIndex.toLong(),
            exerciseId = log.exerciseId,
            skippedAt = log.skippedAt?.toEpochMilliseconds(),
        )
    }

    override suspend fun getExerciseLogsForSession(workoutSessionId: String): AppResult<List<WorkoutExerciseLog>> = runCatchingDatabase {
        local.getExerciseLogsForSession(workoutSessionId).map { it.toEntity() }
    }

    override suspend fun insertSetLog(log: WorkoutSetLog): AppResult<Unit> = runCatchingDatabase {
        local.insertSetLog(
            id = log.id,
            workoutExerciseLogId = log.workoutExerciseLogId,
            setNumber = log.setNumber.toLong(),
            weightKg = log.weightKg,
            reps = log.reps.toLong(),
            completedAt = log.completedAt.toEpochMilliseconds(),
        )
    }

    override suspend fun getSetLogsForSession(workoutSessionId: String): AppResult<List<WorkoutSetLog>> = runCatchingDatabase {
        local.getSetLogsForSession(workoutSessionId).map { it.toEntity() }
    }

    override suspend fun getSetLogsForExercise(exerciseId: String): AppResult<List<WorkoutSetLog>> = runCatchingDatabase {
        local.getSetLogsForExercise(exerciseId).map { it.toEntity() }
    }

    private fun WorkoutSessionRow.toEntity(): WorkoutSession = WorkoutSession(
        id = id,
        planDayId = plan_day_id,
        startedAt = Instant.fromEpochMilliseconds(started_at),
        completedAt = completed_at?.let { Instant.fromEpochMilliseconds(it) },
        status = WorkoutSessionStatus.valueOf(status),
        currentExerciseIndex = current_exercise_index.toInt(),
        currentSetNumber = current_set_number.toInt(),
        restEndAt = rest_end_at?.let { Instant.fromEpochMilliseconds(it) },
    )

    private fun WorkoutExerciseLogRow.toEntity(): WorkoutExerciseLog = WorkoutExerciseLog(
        id = id,
        workoutSessionId = workout_session_id,
        plannedExerciseIndex = planned_exercise_index.toInt(),
        exerciseId = exercise_id,
        skippedAt = skipped_at?.let { Instant.fromEpochMilliseconds(it) },
    )

    private fun WorkoutSetLogRow.toEntity(): WorkoutSetLog = WorkoutSetLog(
        id = id,
        workoutExerciseLogId = workout_exercise_log_id,
        setNumber = set_number.toInt(),
        weightKg = weight_kg,
        reps = reps.toInt(),
        completedAt = Instant.fromEpochMilliseconds(completed_at),
    )
}
