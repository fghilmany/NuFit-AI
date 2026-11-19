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

    suspend fun backfillUserId(userId: String) = withContext(Dispatchers.Default) {
        database.chatMessageQueries.backfillUserId(userId)
    }

    /** Sync pull -- batched in one transaction (not one commit per row). */
    suspend fun upsertMessagesFromRemote(rows: List<ChatMessageRow>) = withContext(Dispatchers.Default) {
        database.transaction {
            rows.forEach { row ->
                val existing = database.chatMessageQueries.selectById(row.id).executeAsOneOrNull()
                if (existing == null || existing.updated_at < row.updated_at) {
                    database.chatMessageQueries.upsertChatMessage(
                        id = row.id,
                        role = row.role,
                        text = row.text,
                        suggestion_exercise_id = row.suggestion_exercise_id,
                        suggestion_replaces_exercise_id = row.suggestion_replaces_exercise_id,
                        suggested_replies = row.suggested_replies,
                        status = row.status,
                        created_at = row.created_at,
                        user_id = row.user_id,
                        updated_at = row.updated_at,
                    )
                }
            }
        }
    }
}
