package com.fghilmany.nufitai.data.chatai.datasource

import com.fghilmany.nufitai.db.Chat_exercise_override as ChatExerciseOverrideRow
import com.fghilmany.nufitai.db.NuFitDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatExerciseOverrideLocalDataSource(private val database: NuFitDatabase) {
    suspend fun insert(id: String, planDayId: String, plannedExerciseIndex: Long, substituteExerciseId: String, createdAt: Long) =
        withContext(Dispatchers.Default) {
            database.chatExerciseOverrideQueries.insertChatExerciseOverride(
                id = id,
                plan_day_id = planDayId,
                planned_exercise_index = plannedExerciseIndex,
                substitute_exercise_id = substituteExerciseId,
                created_at = createdAt,
            )
        }

    suspend fun getForSlot(planDayId: String, plannedExerciseIndex: Long): ChatExerciseOverrideRow? = withContext(Dispatchers.Default) {
        database.chatExerciseOverrideQueries.getOverrideForSlot(planDayId, plannedExerciseIndex).executeAsOneOrNull()
    }

    suspend fun delete(id: String) = withContext(Dispatchers.Default) {
        database.chatExerciseOverrideQueries.deleteOverride(id)
    }
}
