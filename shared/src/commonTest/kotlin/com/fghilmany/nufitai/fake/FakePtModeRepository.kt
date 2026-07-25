package com.fghilmany.nufitai.fake

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutExerciseLog
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSession
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSessionStatus
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSetLog
import com.fghilmany.nufitai.domain.ptmode.repository.PtModeRepository

class FakePtModeRepository : PtModeRepository {
    val sessions: MutableMap<String, WorkoutSession> = mutableMapOf()
    val exerciseLogs: MutableMap<String, WorkoutExerciseLog> = mutableMapOf()
    val setLogs: MutableList<WorkoutSetLog> = mutableListOf()

    override suspend fun getInProgressSession(planDayId: String): AppResult<WorkoutSession?> =
        AppResult.Success(sessions.values.find { it.planDayId == planDayId && it.status == WorkoutSessionStatus.IN_PROGRESS })

    override suspend fun getLatestSessionForDay(planDayId: String): AppResult<WorkoutSession?> =
        AppResult.Success(sessions.values.filter { it.planDayId == planDayId }.maxByOrNull { it.startedAt })

    override suspend fun createSession(session: WorkoutSession): AppResult<Unit> {
        sessions[session.id] = session
        return AppResult.Success(Unit)
    }

    override suspend fun updateSessionProgress(session: WorkoutSession): AppResult<Unit> {
        sessions[session.id] = session
        return AppResult.Success(Unit)
    }

    override suspend fun completeSession(session: WorkoutSession): AppResult<Unit> {
        sessions[session.id] = session
        return AppResult.Success(Unit)
    }

    override suspend fun upsertExerciseLog(log: WorkoutExerciseLog): AppResult<Unit> {
        val existingKey = exerciseLogs.entries.find {
            it.value.workoutSessionId == log.workoutSessionId && it.value.plannedExerciseIndex == log.plannedExerciseIndex
        }?.key
        exerciseLogs[existingKey ?: log.id] = log
        return AppResult.Success(Unit)
    }

    override suspend fun getExerciseLogsForSession(workoutSessionId: String): AppResult<List<WorkoutExerciseLog>> =
        AppResult.Success(exerciseLogs.values.filter { it.workoutSessionId == workoutSessionId })

    override suspend fun insertSetLog(log: WorkoutSetLog): AppResult<Unit> {
        setLogs.add(log)
        return AppResult.Success(Unit)
    }

    override suspend fun getSetLogsForSession(workoutSessionId: String): AppResult<List<WorkoutSetLog>> {
        val logIds = exerciseLogs.values.filter { it.workoutSessionId == workoutSessionId }.map { it.id }.toSet()
        return AppResult.Success(setLogs.filter { it.workoutExerciseLogId in logIds })
    }

    override suspend fun getSetLogsForExercise(exerciseId: String): AppResult<List<WorkoutSetLog>> {
        val logIds = exerciseLogs.values.filter { it.exerciseId == exerciseId }.map { it.id }.toSet()
        return AppResult.Success(setLogs.filter { it.workoutExerciseLogId in logIds })
    }
}
