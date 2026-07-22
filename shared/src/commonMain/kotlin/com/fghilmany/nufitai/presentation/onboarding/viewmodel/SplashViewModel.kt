package com.fghilmany.nufitai.presentation.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.usecase.onboarding.GetLocalProfileStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SplashState {
    data object Loading : SplashState
    data class Routed(val hasLocalProfile: Boolean) : SplashState
}

class SplashViewModel(private val getLocalProfileStatus: GetLocalProfileStatus) : ViewModel() {
    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val result = getLocalProfileStatus()
            _state.value = when (result) {
                is AppResult.Success -> SplashState.Routed(hasLocalProfile = result.data)
                // No local profile is readable as "not found" -- route to onboarding rather than
                // surfacing a database error on the very first screen the user ever sees.
                is AppResult.Error -> SplashState.Routed(hasLocalProfile = false)
            }
        }
    }
}
