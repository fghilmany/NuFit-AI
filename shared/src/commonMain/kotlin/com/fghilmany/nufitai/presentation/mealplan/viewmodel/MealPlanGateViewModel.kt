package com.fghilmany.nufitai.presentation.mealplan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.entitlement.entity.Tier
import com.fghilmany.nufitai.usecase.entitlement.GetCurrentTier
import com.fghilmany.nufitai.usecase.fullassessment.GetLatestFullAssessmentResult
import com.fghilmany.nufitai.usecase.onboarding.GetLatestBodyMeasurement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** P-17 gate (AC-1/AC-2/AC-3, BLOCKING). Mirrors `AiCustomPlanGateViewModel`/`ChatGateViewModel`. */
sealed interface MealPlanGateState {
    data object Loading : MealPlanGateState
    data object Guest : MealPlanGateState
    /** ADR-009: unreachable during the free MVP showcase phase (LOGGED_IN passes through) -- revive when billing ships. */
    data object NonSubscriberPaywall : MealPlanGateState
    /** AC-3 -- height/weight or activityLevel missing; TDEE can't be computed. Flags name exactly
     * which field(s) are missing so the gate screen doesn't have to guess. */
    data class NeedsBodyData(
        val missingHeight: Boolean,
        val missingWeight: Boolean,
        val missingActivityLevel: Boolean,
    ) : MealPlanGateState
    data object PassThrough : MealPlanGateState
    data class Error(val message: String) : MealPlanGateState
}

class MealPlanGateViewModel(
    private val getCurrentTier: GetCurrentTier,
    private val getLatestBodyMeasurement: GetLatestBodyMeasurement,
    private val getLatestFullAssessmentResult: GetLatestFullAssessmentResult,
) : ViewModel() {
    private val _state = MutableStateFlow<MealPlanGateState>(MealPlanGateState.Loading)
    val state: StateFlow<MealPlanGateState> = _state.asStateFlow()

    init {
        resolve()
    }

    private fun resolve() {
        viewModelScope.launch {
            when (val tierResult = getCurrentTier()) {
                is AppResult.Error -> _state.value = MealPlanGateState.Error(tierResult.failure.message)
                is AppResult.Success -> when (tierResult.data) {
                    Tier.GUEST -> _state.value = MealPlanGateState.Guest
                    Tier.LOGGED_IN, Tier.SUBSCRIBED -> resolveBodyDataGate()
                }
            }
        }
    }

    private suspend fun resolveBodyDataGate() {
        val bodyMeasurement = when (val result = getLatestBodyMeasurement()) {
            is AppResult.Error -> { _state.value = MealPlanGateState.Error(result.failure.message); return }
            is AppResult.Success -> result.data
        }
        val fullAssessment = when (val result = getLatestFullAssessmentResult()) {
            is AppResult.Error -> { _state.value = MealPlanGateState.Error(result.failure.message); return }
            is AppResult.Success -> result.data
        }
        val missingHeight = bodyMeasurement?.heightCm == null
        val missingWeight = bodyMeasurement?.weightKg == null
        val missingActivityLevel = fullAssessment?.activityLevel == null
        _state.value = if (!missingHeight && !missingWeight && !missingActivityLevel) {
            MealPlanGateState.PassThrough
        } else {
            MealPlanGateState.NeedsBodyData(missingHeight, missingWeight, missingActivityLevel)
        }
    }
}
