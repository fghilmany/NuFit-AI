package com.fghilmany.nufitai.data.chatai.sync

import com.fghilmany.nufitai.data.chatai.datasource.ChatLocalDataSource
import com.fghilmany.nufitai.data.sync.contract.SyncParticipant
import com.fghilmany.nufitai.db.Chat_message as ChatMessageRow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.time.Instant

// Migration 20261117000003 (chat_request_transactions) revoked insert/update/delete on
// chat_message for the authenticated role -- chat-send's begin_chat_request/complete_chat_request
// (service_role only) are now the sole writers, so old client-written rows can't be resurrected.
// This participant is pull-only: push must always see zero changed rows.
class ChatMessageSyncParticipant(
    private val local: ChatLocalDataSource,
) : SyncParticipant {
    override val tableName: String = "chat_message"

    override suspend fun getChangedSince(since: Instant): List<JsonObject> = emptyList()

    override suspend fun upsertRemoteRows(rows: List<JsonObject>) {
        local.upsertMessagesFromRemote(rows.map { it.toRow() })
    }

    override suspend fun backfillUserId(userId: String) {
        local.backfillUserId(userId)
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
