package com.fghilmany.nufitai.usecase.chatai

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.chatai.repository.ChatRepository

/** techspec chat-ai.md §8/§9 item 9 (AC-7) -- reconciles any message still SENDING (impossible after process death) to FAILED; called once at `ChatViewModel` init, before history loads. */
class ReconcileStuckChatMessages(private val repository: ChatRepository) {
    suspend operator fun invoke(): AppResult<Unit> = repository.reconcileStuckSendingMessages()
}
