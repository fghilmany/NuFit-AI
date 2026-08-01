package com.fghilmany.nufitai.data.chatai.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.runCatchingDatabase
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.core.util.generateId
import com.fghilmany.nufitai.data.chatai.datasource.ChatExerciseOverrideLocalDataSource
import com.fghilmany.nufitai.domain.chatai.repository.ChatExerciseOverrideRepository

class ChatExerciseOverrideRepositoryImpl(
    private val local: ChatExerciseOverrideLocalDataSource,
) : ChatExerciseOverrideRepository {
    override suspend fun createOverride(planDayId: String, plannedExerciseIndex: Int, substituteExerciseId: String): AppResult<Unit> =
        runCatchingDatabase {
            local.insert(
                id = generateId(),
                planDayId = planDayId,
                plannedExerciseIndex = plannedExerciseIndex.toLong(),
                substituteExerciseId = substituteExerciseId,
                createdAt = currentInstant().toEpochMilliseconds(),
            )
        }

    override suspend fun consumeOverride(planDayId: String, plannedExerciseIndex: Int): AppResult<String?> = runCatchingDatabase {
        val row = local.getForSlot(planDayId, plannedExerciseIndex.toLong()) ?: return@runCatchingDatabase null
        local.delete(row.id)
        row.substitute_exercise_id
    }
}
