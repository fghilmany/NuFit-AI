package com.fghilmany.nufitai.domain.chatai.entity

import kotlin.time.Instant

enum class ChatMessageRole { USER, ASSISTANT }

enum class ChatMessageStatus { SENDING, SENT, FAILED }

/**
 * techspec chat-ai.md §5 -- [replacesExerciseId] identifies which exercise in the user's plan
 * this suggestion substitutes for "Terapkan ke Plan"; null means informational only (the button
 * is hidden in that case).
 */
data class ExerciseSuggestion(val exerciseId: String, val replacesExerciseId: String?)

/**
 * techspec chat-ai.md §5. [suggestedReplies] is only ever meaningful on the most recent
 * ASSISTANT message -- the UI does not re-render chips on older messages.
 */
data class ChatMessage(
    val id: String,
    val role: ChatMessageRole,
    val text: String,
    val suggestion: ExerciseSuggestion?,
    val suggestedReplies: List<String>,
    val status: ChatMessageStatus,
    val createdAt: Instant,
)
