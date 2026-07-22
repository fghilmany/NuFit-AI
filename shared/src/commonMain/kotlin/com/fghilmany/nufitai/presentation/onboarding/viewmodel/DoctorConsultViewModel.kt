package com.fghilmany.nufitai.presentation.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.onboarding.entity.ParQQuestionId
import com.fghilmany.nufitai.usecase.onboarding.AcknowledgeDoctorConsult
import com.fghilmany.nufitai.usecase.onboarding.GetLatestParQResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface DoctorConsultState {
    data object Loading : DoctorConsultState
    data class Content(
        val parQResultId: String,
        val flaggedQuestions: List<ParQQuestionId>,
        val acknowledged: Boolean = false,
    ) : DoctorConsultState
    data object Continued : DoctorConsultState
    data class Error(val message: String) : DoctorConsultState
}

sealed interface DoctorConsultEvent {
    data object ToggleAcknowledge : DoctorConsultEvent
    data object Continue : DoctorConsultEvent
}

class DoctorConsultViewModel(
    private val getLatestParQResult: GetLatestParQResult,
    private val acknowledgeDoctorConsult: AcknowledgeDoctorConsult,
) : ViewModel() {
    private val _state = MutableStateFlow<DoctorConsultState>(DoctorConsultState.Loading)
    val state: StateFlow<DoctorConsultState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = getLatestParQResult()) {
                is AppResult.Success -> {
                    val parQResult = result.data
                    _state.value = if (parQResult != null) {
                        DoctorConsultState.Content(
                            parQResultId = parQResult.id,
                            flaggedQuestions = parQResult.answers.filter { it.answer }.map { it.questionId },
                        )
                    } else {
                        DoctorConsultState.Error("PAR-Q result tidak ditemukan")
                    }
                }
                is AppResult.Error -> _state.value = DoctorConsultState.Error(result.failure.message)
            }
        }
    }

    fun onEvent(event: DoctorConsultEvent) {
        when (event) {
            DoctorConsultEvent.ToggleAcknowledge -> toggleAcknowledge()
            DoctorConsultEvent.Continue -> continueFlow()
        }
    }

    private fun toggleAcknowledge() {
        _state.update { current ->
            if (current is DoctorConsultState.Content) current.copy(acknowledged = !current.acknowledged) else current
        }
    }

    private fun continueFlow() {
        val current = _state.value
        if (current !is DoctorConsultState.Content || !current.acknowledged) return

        viewModelScope.launch {
            when (val result = acknowledgeDoctorConsult(current.parQResultId)) {
                is AppResult.Success -> _state.value = DoctorConsultState.Continued
                is AppResult.Error -> _state.value = DoctorConsultState.Error(result.failure.message)
            }
        }
    }
}
