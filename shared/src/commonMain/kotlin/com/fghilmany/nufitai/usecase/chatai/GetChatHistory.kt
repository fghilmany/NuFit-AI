package com.fghilmany.nufitai.usecase.chatai

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessage
import com.fghilmany.nufitai.domain.chatai.repository.ChatRepository

class GetChatHistory(private val repository: ChatRepository) {
    suspend operator fun invoke(): AppResult<List<ChatMessage>> = repository.getHistory()
}
