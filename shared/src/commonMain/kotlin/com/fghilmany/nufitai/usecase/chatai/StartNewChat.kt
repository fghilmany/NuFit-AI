package com.fghilmany.nufitai.usecase.chatai

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.chatai.repository.ChatRepository

/** Dev-requested "Chat Baru" (18 Nov 2026) -- resets the conversation cutoff; nothing is deleted. */
class StartNewChat(private val repository: ChatRepository) {
    suspend operator fun invoke(): AppResult<Unit> = repository.startNewChat()
}
