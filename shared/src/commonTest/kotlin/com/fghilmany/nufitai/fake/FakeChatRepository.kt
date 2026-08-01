package com.fghilmany.nufitai.fake

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessage
import com.fghilmany.nufitai.domain.chatai.entity.ChatQuota
import com.fghilmany.nufitai.domain.chatai.repository.ChatRepository
import com.fghilmany.nufitai.domain.chatai.repository.ChatSendResult

class FakeChatRepository : ChatRepository {
    val messages = mutableMapOf<String, ChatMessage>()
    var sendResult: AppResult<ChatSendResult> = AppResult.Error(Failure.Network("no fake result configured"))
    var quota: ChatQuota? = null
    var sendToRemoteCallCount = 0
        private set

    override suspend fun getHistory(): AppResult<List<ChatMessage>> = AppResult.Success(messages.values.sortedBy { it.createdAt })

    override suspend fun insertMessage(message: ChatMessage): AppResult<Unit> {
        messages[message.id] = message
        return AppResult.Success(Unit)
    }

    override suspend fun updateMessage(message: ChatMessage): AppResult<Unit> {
        messages[message.id] = message
        return AppResult.Success(Unit)
    }

    override suspend fun reconcileStuckSendingMessages(): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun sendToRemote(text: String): AppResult<ChatSendResult> {
        sendToRemoteCallCount++
        return sendResult
    }

    override suspend fun getQuota(): AppResult<ChatQuota?> = AppResult.Success(quota)
}
