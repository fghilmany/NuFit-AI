package com.fghilmany.nufitai.domain.chatai.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessage
import com.fghilmany.nufitai.domain.chatai.entity.ChatQuota

data class ChatSendResult(val reply: ChatMessage, val quota: ChatQuota)

/** techspec chat-ai.md §6/§7. */
interface ChatRepository {
    suspend fun getHistory(): AppResult<List<ChatMessage>>
    suspend fun insertMessage(message: ChatMessage): AppResult<Unit>
    suspend fun updateMessage(message: ChatMessage): AppResult<Unit>

    /** §8/§9 item 9 -- any message still SENDING at startup could not have survived process death mid-request. */
    suspend fun reconcileStuckSendingMessages(): AppResult<Unit>

    /** Calls the remote endpoint (§7); does not persist -- the caller (SendChatMessage) decides what to write. */
    suspend fun sendToRemote(text: String): AppResult<ChatSendResult>

    suspend fun getQuota(): AppResult<ChatQuota?>
}
