package com.fghilmany.nufitai.presentation.aicustomplan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.entitlement.entity.Tier
import com.fghilmany.nufitai.usecase.entitlement.GetCurrentTier
import com.fghilmany.nufitai.usecase.fullassessment.HasCompletedFullAssessment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** P-12 A (UC-1). */
sealed interface AiCustomPlanGateState {
    data object Loading : AiCustomPlanGateState
    data object Guest : AiCustomPlanGateState
    /** ADR-009: unreachable during the free MVP showcase phase (LOGGED_IN passes through) -- revive when billing ships. */
    data object NonSubscriberPaywall : AiCustomPlanGateState
    /** AC-11 -- subscriber, Full Assessment not yet complete. */
    data object NeedsFullAssessment : AiCustomPlanGateState
    data object PassThrough : AiCustomPlanGateState
    data class Error(val message: String) : AiCustomPlanGateState
}

class AiCustomPlanGateViewModel(
    private val getCurrentTier: GetCurrentTier,
    private val hasCompletedFullAssessment: HasCompletedFullAssessment,
) : ViewModel() {
    private val _state = MutableStateFlow<AiCustomPlanGateState>(AiCustomPlanGateState.Loading)
    val state: StateFlow<AiCustomPlanGateState> = _state.asStateFlow()

    init {
        resolve()
    }

    private fun resolve() {
        viewModelScope.launch {
            when (val tierResult = getCurrentTier()) {
                is AppResult.Error -> _state.value = AiCustomPlanGateState.Error(tierResult.failure.message)
                is AppResult.Success -> when (tierResult.data) {
                    Tier.GUEST -> _state.value = AiCustomPlanGateState.Guest
                    Tier.LOGGED_IN, Tier.SUBSCRIBED -> resolveFullAssessmentGate()
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
