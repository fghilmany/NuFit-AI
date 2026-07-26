package com.fghilmany.nufitai.presentation.aicustomplan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.entitlement.entity.Tier
import com.fghilmany.nufitai.usecase.entitlement.GetCurrentTier
import com.fghilmany.nufitai.usecase.entitlement.SetDebugEntitlement
import com.fghilmany.nufitai.usecase.fullassessment.HasCompletedFullAssessment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** P-12 A (UC-1). */
sealed interface AiCustomPlanGateState {
    data object Loading : AiCustomPlanGateState
    data object Guest : AiCustomPlanGateState
    data object NonSubscriberPaywall : AiCustomPlanGateState
    /** AC-11 -- subscriber, Full Assessment not yet complete. */
    data object NeedsFullAssessment : AiCustomPlanGateState
    data object PassThrough : AiCustomPlanGateState
    data class Error(val message: String) : AiCustomPlanGateState
}

class AiCustomPlanGateViewModel(
    private val getCurrentTier: GetCurrentTier,
    private val hasCompletedFullAssessment: HasCompletedFullAssessment,
    private val setDebugEntitlement: SetDebugEntitlement,
) : ViewModel() {
    private val _state = MutableStateFlow<AiCustomPlanGateState>(AiCustomPlanGateState.Loading)
    val state: StateFlow<AiCustomPlanGateState> = _state.asStateFlow()

    init {
        resolve()
    }

    /** DEBUG ONLY -- remove once real auth/billing land (techspec §9 item 8). Simulates a subscriber. */
    fun debugSimulateSubscriber() {
        viewModelScope.launch {
            setDebugEntitlement(isLoggedIn = true, isSubscribed = true)
            resolve()
        }
    }

    private fun resolve() {
        viewModelScope.launch {
            when (val tierResult = getCurrentTier()) {
                is AppResult.Error -> _state.value = AiCustomPlanGateState.Error(tierResult.failure.message)
                is AppResult.Success -> when (tierResult.data) {
                    Tier.GUEST -> _state.value = AiCustomPlanGateState.Guest
                    Tier.LOGGED_IN -> _state.value = AiCustomPlanGateState.NonSubscriberPaywall
                    Tier.SUBSCRIBED -> resolveFullAssessmentGate()
                }
            }
        }
    }

    private suspend fun resolveFullAssessmentGate() {
        when (val result = hasCompletedFullAssessment()) {
            is AppResult.Error -> _state.value = AiCustomPlanGateState.Error(result.failure.message)
            is AppResult.Success -> {
                _state.value = if (result.data) AiCustomPlanGateState.PassThrough else AiCustomPlanGateState.NeedsFullAssessment
            }
        }
    }
}
