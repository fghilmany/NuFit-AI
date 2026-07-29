package com.fghilmany.nufitai.presentation.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.usecase.onboarding.GetLocalProfileStatus
import com.fghilmany.nufitai.usecase.profilesettings.HasSeenMedicalDisclaimer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SplashState {
    data object Loading : SplashState
    data class Routed(val hasLocalProfile: Boolean) : SplashState
    /** techspec profile-settings.md §7 -- takes priority over the ParQ/Home branch, first launch ever. */
    data object NeedsMedicalDisclaimer : SplashState
}

class SplashViewModel(
    private val getLocalProfileStatus: GetLocalProfileStatus,
    private val hasSeenMedicalDisclaimer: HasSeenMedicalDisclaimer,
) : ViewModel() {
    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val hasSeenDisclaimer = when (val result = hasSeenMedicalDisclaimer()) {
                is AppResult.Success -> result.data
                // Fail-open on a read error would keep re-showing the gate every launch, but
                // fail-closed (treat as seen) risks a compliance gap -- re-showing once more is
                // the safer default for a medical disclaimer.
                is AppResult.Error -> false
            }
            if (!hasSeenDisclaimer) {
                _state.value = SplashState.NeedsMedicalDisclaimer
                return@launch
            }
            routeToLocalProfileCheck()
        }
    }

    fun onMedicalDisclaimerAcknowledged() {
        viewModelScope.launch { routeToLocalProfileCheck() }
    }

    private suspend fun routeToLocalProfileCheck() {
        val result = getLocalProfileStatus()
        _state.value = when (result) {
            is AppResult.Success -> SplashState.Routed(hasLocalProfile = result.data)
            // No local profile is readable as "not found" -- route to onboarding rather than
            // surfacing a database error on the very first screen the user ever sees.
            is AppResult.Error -> SplashState.Routed(hasLocalProfile = false)
        }
    }
}
