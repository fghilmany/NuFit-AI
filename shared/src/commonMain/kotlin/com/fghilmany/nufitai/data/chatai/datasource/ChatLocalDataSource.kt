package com.fghilmany.nufitai.data.chatai.datasource

import com.fghilmany.nufitai.db.Chat_message as ChatMessageRow
import com.fghilmany.nufitai.db.NuFitDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatLocalDataSource(private val database: NuFitDatabase) {
    suspend fun upsertMessage(
        id: String,
        role: String,
        text: String,
        suggestionExerciseId: String?,
        suggestionReplacesExerciseId: String?,
        suggestedRepliesJson: String?,
        status: String,
        createdAt: Long,
        userId: String?,
        updatedAt: Long,
    ) = withContext(Dispatchers.Default) {
        database.chatMessageQueries.upsertChatMessage(
            id = id,
            role = role,
            text = text,
            suggestion_exercise_id = suggestionExerciseId,
            suggestion_replaces_exercise_id = suggestionReplacesExerciseId,
            suggested_replies = suggestedRepliesJson,
            status = status,
            created_at = createdAt,
            user_id = userId,
            updated_at = updatedAt,
        )
    }

    suspend fun getAllMessages(): List<ChatMessageRow> = withContext(Dispatchers.Default) {
        database.chatMessageQueries.getAllMessages().executeAsList()
    }

    suspend fun getSendingMessages(): List<ChatMessageRow> = withContext(Dispatchers.Default) {
        database.chatMessageQueries.getSendingMessages().executeAsList()
    }

    /** techspec chat-ai.md §7 -- sync engine cursor query, mirrors MonthlyPlanLocalDataSource's shape. */
    suspend fun getChangedSince(updatedAfter: Long): List<ChatMessageRow> = withContext(Dispatchers.Default) {
        database.chatMessageQueries.selectChangedSince(updatedAfter).executeAsList()
    }

    suspend fun getById(id: String): ChatMessageRow? = withContext(Dispatchers.Default) {
        database.chatMessageQueries.selectById(id).executeAsOneOrNull()
    }

    suspend fun backfillUserId(userId: String) = withContext(Dispatchers.Default) {
        database.chatMessageQueries.backfillUserId(userId)
    }
}
