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
        local.upsertMessagesFromRemote(rows.map { it.toRow() })
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

    private fun JsonObject.toRow(): ChatMessageRow = ChatMessageRow(
        id = getValue("id").jsonPrimitive.content,
        role = getValue("role").jsonPrimitive.content,
        text = getValue("text").jsonPrimitive.content,
        suggestion_exercise_id = get("suggestion_exercise_id")?.jsonPrimitive?.contentOrNull,
        suggestion_replaces_exercise_id = get("suggestion_replaces_exercise_id")?.jsonPrimitive?.contentOrNull,
        suggested_replies = get("suggested_replies")?.jsonPrimitive?.contentOrNull,
        status = getValue("status").jsonPrimitive.content,
        created_at = getValue("created_at").jsonPrimitive.long,
        user_id = get("user_id")?.jsonPrimitive?.contentOrNull,
        updated_at = getValue("updated_at").jsonPrimitive.long,
    )
}
