package com.fghilmany.nufitai.usecase.chatai

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessage
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessageStatus
import com.fghilmany.nufitai.domain.chatai.repository.ChatRepository

/** techspec chat-ai.md §8/§9 item 8 -- retries a FAILED message in place (same id, no duplicate row). */
class RetryFailedMessage(private val repository: ChatRepository) {
    suspend operator fun invoke(failedMessage: ChatMessage): AppResult<ChatMessage> {
        repository.updateMessage(failedMessage.copy(status = ChatMessageStatus.SENDING))

        return when (val sendResult = repository.sendToRemote(failedMessage.text)) {
            is AppResult.Error -> {
                repository.updateMessage(failedMessage.copy(status = ChatMessageStatus.FAILED))
                sendResult
            }
            is AppResult.Success -> {
                repository.updateMessage(failedMessage.copy(status = ChatMessageStatus.SENT))
                repository.insertMessage(sendResult.data.reply)
                AppResult.Success(sendResult.data.reply)
            }
        }
    }
}
