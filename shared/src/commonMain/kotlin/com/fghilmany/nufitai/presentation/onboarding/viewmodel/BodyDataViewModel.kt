package com.fghilmany.nufitai.presentation.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.usecase.onboarding.SaveBodyMeasurement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface BodyDataState {
    data class Content(val heightCm: String = "", val weightKg: String = "") : BodyDataState
    data object Saved : BodyDataState
    data object Skipped : BodyDataState
    data class Error(val message: String) : BodyDataState
}

sealed interface BodyDataEvent {
    data class UpdateHeight(val value: String) : BodyDataEvent
    data class UpdateWeight(val value: String) : BodyDataEvent
    data object Save : BodyDataEvent
    data object Skip : BodyDataEvent
}

class BodyDataViewModel(private val saveBodyMeasurement: SaveBodyMeasurement) : ViewModel() {
    private val _state = MutableStateFlow<BodyDataState>(BodyDataState.Content())
    val state: StateFlow<BodyDataState> = _state.asStateFlow()

    fun onEvent(event: BodyDataEvent) {
        when (event) {
            is BodyDataEvent.UpdateHeight -> updateContent { it.copy(heightCm = event.value) }
            is BodyDataEvent.UpdateWeight -> updateContent { it.copy(weightKg = event.value) }
            BodyDataEvent.Save -> save()
            BodyDataEvent.Skip -> _state.value = BodyDataState.Skipped
        }
    }

    private fun updateContent(transform: (BodyDataState.Content) -> BodyDataState.Content) {
        _state.update { current -> if (current is BodyDataState.Content) transform(current) else current }
    }

    private fun save() {
        val current = _state.value
        if (current !is BodyDataState.Content) return

        val heightCm = current.heightCm.toDoubleOrNull()
        val weightKg = current.weightKg.toDoubleOrNull()

        viewModelScope.launch {
            when (val result = saveBodyMeasurement(heightCm, weightKg)) {
                is AppResult.Success -> _state.value = BodyDataState.Saved
                is AppResult.Error -> _state.value = BodyDataState.Error(result.failure.message)
            }
        }
    }
}
