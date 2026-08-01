package com.fghilmany.nufitai.usecase.chatai

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.core.util.generateId
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessage
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessageRole
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessageStatus
import com.fghilmany.nufitai.domain.chatai.repository.ChatRepository

/**
 * techspec chat-ai.md §8 -- optimistic insert (SENDING) so the message never appears to vanish;
 * on success the same row flips to SENT and the assistant reply is appended; on failure it
 * flips to FAILED (retryable via [RetryFailedMessage]) rather than being silently dropped.
 */
class SendChatMessage(private val repository: ChatRepository) {
    suspend operator fun invoke(text: String): AppResult<ChatMessage> {
        val userMessage = ChatMessage(
            id = generateId(),
            role = ChatMessageRole.USER,
            text = text,
            suggestion = null,
            suggestedReplies = emptyList(),
            status = ChatMessageStatus.SENDING,
            createdAt = currentInstant(),
        )
        when (val insertResult = repository.insertMessage(userMessage)) {
            is AppResult.Error -> return insertResult
            is AppResult.Success -> Unit
        }

        return when (val sendResult = repository.sendToRemote(text)) {
            is AppResult.Error -> {
                repository.updateMessage(userMessage.copy(status = ChatMessageStatus.FAILED))
                sendResult
            }
            is AppResult.Success -> {
                repository.updateMessage(userMessage.copy(status = ChatMessageStatus.SENT))
                repository.insertMessage(sendResult.data.reply)
                AppResult.Success(sendResult.data.reply)
            }
        }
    }
}
