package com.fghilmany.nufitai.presentation.chatai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessage
import com.fghilmany.nufitai.domain.chatai.entity.ChatQuota
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.usecase.chatai.ApplyChatExerciseSuggestion
import com.fghilmany.nufitai.usecase.chatai.GetChatHistory
import com.fghilmany.nufitai.usecase.chatai.GetChatQuota
import com.fghilmany.nufitai.usecase.chatai.ReconcileStuckChatMessages
import com.fghilmany.nufitai.usecase.chatai.RetryFailedMessage
import com.fghilmany.nufitai.usecase.chatai.SendChatMessage
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExerciseDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** techspec chat-ai.md §9 item 11 -- no design/product number given for the composer cap; generous default until the dev confirms one. */
private const val COMPOSER_CHAR_LIMIT = 1000

/** techspec chat-ai.md AC-4/§9 item 12 -- "Terapkan ke Plan" result, surfaced inline on the triggering card. */
sealed interface ApplySuggestionResult {
    data class Success(val messageId: String) : ApplySuggestionResult
    data class Error(val messageId: String, val message: String) : ApplySuggestionResult
}

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    /** DoD (chat-ai.md §10) -- only exerciseIds that resolved against the local library land here; a suggestion whose id isn't in this map renders without the card. */
    val resolvedExercises: Map<String, Exercise> = emptyMap(),
    val quota: ChatQuota? = null,
    val draft: String = "",
    val isSending: Boolean = false,
    val applyResult: ApplySuggestionResult? = null,
    val loadError: String? = null,
) {
    val composerLimit: Int get() = COMPOSER_CHAR_LIMIT
    val isQuotaExhausted: Boolean get() = quota != null && quota.used >= quota.max
}

class ChatViewModel(
    private val reconcileStuckChatMessages: ReconcileStuckChatMessages,
    private val getChatHistory: GetChatHistory,
    private val sendChatMessage: SendChatMessage,
    private val retryFailedMessage: RetryFailedMessage,
    private val getChatQuota: GetChatQuota,
    private val applyChatExerciseSuggestion: ApplyChatExerciseSuggestion,
    private val getExerciseDetail: GetExerciseDetail,
) : ViewModel() {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            reconcileStuckChatMessages()
            loadHistory()
        }
        loadQuota()
    }

    fun onDraftChange(text: String) {
        if (text.length <= COMPOSER_CHAR_LIMIT) _state.update { it.copy(draft = text) }
    }

    /** AC-3/AC-6/§9 item 10 -- disabled while sending or quota-exhausted; offline sends still queue (SENDING->FAILED), never silently drop the draft. */
    fun onSend() {
        val text = _state.value.draft.trim()
        if (text.isEmpty() || _state.value.isSending || _state.value.isQuotaExhausted) return
        _state.update { it.copy(draft = "", isSending = true) }
        viewModelScope.launch {
            sendChatMessage(text)
            _state.update { it.copy(isSending = false) }
            refreshAfterSend()
        }
    }

    /** AC-6/AC-7 -- retries a FAILED message in place, same row id. */
    fun onRetry(message: ChatMessage) {
        if (_state.value.isSending) return
        _state.update { it.copy(isSending = true) }
        viewModelScope.launch {
            retryFailedMessage(message)
            _state.update { it.copy(isSending = false) }
            refreshAfterSend()
        }
    }

    fun onSuggestedReplyClick(text: String) {
        _state.update { it.copy(draft = text) }
        onSend()
    }

    /** AC-4, BLOCKING -- "Terapkan ke Plan". */
    fun onApplyToPlan(messageId: String, replacesExerciseId: String, substituteExerciseId: String) {
        viewModelScope.launch {
            when (val result = applyChatExerciseSuggestion(replacesExerciseId, substituteExerciseId)) {
                is AppResult.Error -> _state.update { it.copy(applyResult = ApplySuggestionResult.Error(messageId, result.failure.message)) }
                is AppResult.Success -> _state.update { it.copy(applyResult = ApplySuggestionResult.Success(messageId)) }
            }
        }
    }

    fun dismissApplyResult() {
        _state.update { it.copy(applyResult = null) }
    }

    private suspend fun loadHistory() {
        when (val result = getChatHistory()) {
            is AppResult.Error -> _state.update { it.copy(loadError = result.failure.message) }
            is AppResult.Success -> {
                _state.update { it.copy(messages = result.data.sortedBy { message -> message.createdAt }, loadError = null) }
                resolveSuggestionExercises(result.data)
            }
        }
    }

    private fun loadQuota() {
        viewModelScope.launch {
            when (val result = getChatQuota()) {
                is AppResult.Error -> Unit // quota banner just stays hidden -- not blocking
                is AppResult.Success -> _state.update { it.copy(quota = result.data) }
            }
        }
    }

    private fun resolveSuggestionExercises(messages: List<ChatMessage>) {
        val idsToResolve = messages.mapNotNull { it.suggestion?.exerciseId }.distinct()
            .filterNot { _state.value.resolvedExercises.containsKey(it) }
        idsToResolve.forEach { exerciseId ->
            viewModelScope.launch {
                when (val result = getExerciseDetail(exerciseId)) {
                    is AppResult.Error -> Unit
                    is AppResult.Success -> {
                        val exercise = result.data ?: return@launch
                        _state.update { it.copy(resolvedExercises = it.resolvedExercises + (exerciseId to exercise)) }
                    }
                }
            }
        }
    }

    private fun refreshAfterSend() {
        viewModelScope.launch { loadHistory() }
        loadQuota()
    }
}
