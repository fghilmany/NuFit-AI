package com.fghilmany.nufitai.usecase.chatai

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.chatai.entity.ChatQuota
import com.fghilmany.nufitai.domain.chatai.repository.ChatRepository

/** techspec chat-ai.md §5 -- reads the last quota reported by the server; never computed locally. */
class GetChatQuota(private val repository: ChatRepository) {
    suspend operator fun invoke(): AppResult<ChatQuota?> = repository.getQuota()
}
