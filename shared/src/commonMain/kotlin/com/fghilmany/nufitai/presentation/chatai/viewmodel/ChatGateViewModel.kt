package com.fghilmany.nufitai.presentation.chatai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.entitlement.entity.Tier
import com.fghilmany.nufitai.usecase.entitlement.GetCurrentTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** P-16 A -- entitlement gate (techspec chat-ai.md AC-1, BLOCKING). LOGGED_IN and SUBSCRIBED both pass through -- keputusan #12, the tab is never hidden, only the quota/model tier differs (server-side). */
sealed interface ChatGateState {
    data object Loading : ChatGateState
    data object Guest : ChatGateState
    data object PassThrough : ChatGateState
    data class Error(val message: String) : ChatGateState
}

class ChatGateViewModel(
    private val getCurrentTier: GetCurrentTier,
) : ViewModel() {
    private val _state = MutableStateFlow<ChatGateState>(ChatGateState.Loading)
    val state: StateFlow<ChatGateState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = getCurrentTier()) {
                is AppResult.Error -> _state.value = ChatGateState.Error(result.failure.message)
                is AppResult.Success -> _state.value = when (result.data) {
                    Tier.GUEST -> ChatGateState.Guest
                    Tier.LOGGED_IN, Tier.SUBSCRIBED -> ChatGateState.PassThrough
                }
            }
        }
    }
}
