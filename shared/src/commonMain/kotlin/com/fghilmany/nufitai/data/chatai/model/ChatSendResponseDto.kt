package com.fghilmany.nufitai.data.chatai.model

import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.core.util.generateId
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessage
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessageRole
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessageStatus
import com.fghilmany.nufitai.domain.chatai.entity.ChatQuota
import com.fghilmany.nufitai.domain.chatai.entity.ExerciseSuggestion
import com.fghilmany.nufitai.domain.chatai.repository.ChatSendResult
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** techspec chat-ai.md §7. */
@Serializable
data class ChatSendResponseDto(val reply: ChatReplyDto, val quota: ChatQuotaDto) {
    fun toEntity(): ChatSendResult = ChatSendResult(reply = reply.toEntity(), quota = quota.toEntity())
}

@Serializable
data class ChatReplyDto(
    val text: String,
    val exerciseSuggestion: ExerciseSuggestionDto? = null,
    val suggestedReplies: List<String> = emptyList(),
) {
    fun toEntity(): ChatMessage = ChatMessage(
        id = generateId(),
        role = ChatMessageRole.ASSISTANT,
        text = text,
        suggestion = exerciseSuggestion?.toEntity(),
        suggestedReplies = suggestedReplies,
        status = ChatMessageStatus.SENT,
        createdAt = currentInstant(),
    )
}

@Serializable
data class ExerciseSuggestionDto(val exerciseId: String, val replacesExerciseId: String? = null) {
    fun toEntity(): ExerciseSuggestion = ExerciseSuggestion(exerciseId = exerciseId, replacesExerciseId = replacesExerciseId)
}

@Serializable
data class ChatQuotaDto(val used: Int, val max: Int, val resetsAt: String) {
    fun toEntity(): ChatQuota = ChatQuota(used = used, max = max, resetsAt = Instant.parse(resetsAt))
}
