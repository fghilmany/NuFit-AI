package com.fghilmany.nufitai.data.chatai.sync

import com.fghilmany.nufitai.data.chatai.datasource.ChatLocalDataSource
import com.fghilmany.nufitai.data.sync.contract.SyncParticipant
import com.fghilmany.nufitai.db.Chat_message as ChatMessageRow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.time.Instant

/** techspec chat-ai.md §6/§7 -- mirrors `MonthlyPlanSyncParticipant`'s worked-example pattern exactly. */
class ChatMessageSyncParticipant(
    private val local: ChatLocalDataSource,
) : SyncParticipant {
    override val tableName: String = "chat_message"

    override suspend fun getChangedSince(since: Instant): List<JsonObject> =
        local.getChangedSince(since.toEpochMilliseconds()).map { it.toJson() }

    override suspend fun upsertRemoteRows(rows: List<JsonObject>) {
        rows.forEach { row ->
            val id = row.getValue("id").jsonPrimitive.content
            val remoteUpdatedAt = row.getValue("updated_at").jsonPrimitive.long
            val existing = local.getById(id)
            // LWW per record (techspec §5/§9 item 19 precedent, auth-sync.md): only apply if remote is newer.
            if (existing == null || existing.updated_at < remoteUpdatedAt) {
                local.upsertMessage(
                    id = id,
                    role = row.getValue("role").jsonPrimitive.content,
                    text = row.getValue("text").jsonPrimitive.content,
                    suggestionExerciseId = row["suggestion_exercise_id"]?.jsonPrimitive?.contentOrNull,
                    suggestionReplacesExerciseId = row["suggestion_replaces_exercise_id"]?.jsonPrimitive?.contentOrNull,
                    suggestedRepliesJson = row["suggested_replies"]?.jsonPrimitive?.contentOrNull,
                    status = row.getValue("status").jsonPrimitive.content,
                    createdAt = row.getValue("created_at").jsonPrimitive.long,
                    userId = row["user_id"]?.jsonPrimitive?.contentOrNull,
                    updatedAt = remoteUpdatedAt,
                )
            }
        }
    }

    override suspend fun backfillUserId(userId: String) {
        local.backfillUserId(userId)
    }

    private fun ChatMessageRow.toJson(): JsonObject = buildJsonObject {
        put("id", JsonPrimitive(id))
        put("role", JsonPrimitive(role))
        put("text", JsonPrimitive(text))
        put("suggestion_exercise_id", JsonPrimitive(suggestion_exercise_id))
        put("suggestion_replaces_exercise_id", JsonPrimitive(suggestion_replaces_exercise_id))
        put("suggested_replies", JsonPrimitive(suggested_replies))
        put("status", JsonPrimitive(status))
        put("created_at", JsonPrimitive(created_at))
        put("user_id", JsonPrimitive(user_id))
        put("updated_at", JsonPrimitive(updated_at))
    }
}
