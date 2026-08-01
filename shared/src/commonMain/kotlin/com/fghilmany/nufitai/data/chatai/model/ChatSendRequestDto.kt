package com.fghilmany.nufitai.data.chatai.model

import kotlinx.serialization.Serializable

/** techspec chat-ai.md §7. */
@Serializable
data class ChatSendRequestDto(val message: String, val conversationHistoryLimit: Int = 20)
