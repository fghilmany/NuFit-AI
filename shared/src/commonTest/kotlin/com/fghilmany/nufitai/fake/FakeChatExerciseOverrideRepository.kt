package com.fghilmany.nufitai.fake

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.chatai.repository.ChatExerciseOverrideRepository

class FakeChatExerciseOverrideRepository : ChatExerciseOverrideRepository {
    /** Keyed by "planDayId:plannedExerciseIndex" -> substituteExerciseId. */
    val overrides = mutableMapOf<String, String>()

    private fun key(planDayId: String, plannedExerciseIndex: Int) = "$planDayId:$plannedExerciseIndex"

    override suspend fun createOverride(planDayId: String, plannedExerciseIndex: Int, substituteExerciseId: String): AppResult<Unit> {
        overrides[key(planDayId, plannedExerciseIndex)] = substituteExerciseId
        return AppResult.Success(Unit)
    }

    override suspend fun consumeOverride(planDayId: String, plannedExerciseIndex: Int): AppResult<String?> {
        val substituteExerciseId = overrides.remove(key(planDayId, plannedExerciseIndex))
        return AppResult.Success(substituteExerciseId)
    }
}
