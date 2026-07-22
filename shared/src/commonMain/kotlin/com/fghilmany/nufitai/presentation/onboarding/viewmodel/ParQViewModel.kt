package com.fghilmany.nufitai.presentation.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.onboarding.entity.ParQAnswer
import com.fghilmany.nufitai.domain.onboarding.entity.ParQQuestionId
import com.fghilmany.nufitai.usecase.onboarding.SubmitParQAnswers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ParQState {
    data class Questions(
        val answers: Map<ParQQuestionId, Boolean?> = ParQQuestionId.entries.associateWith { null },
    ) : ParQState {
        val canProceed: Boolean get() = answers.values.all { it != null }
    }
    data object Submitting : ParQState
    data class Submitted(val requiresDoctorConsult: Boolean, val parQResultId: String) : ParQState
    data class Error(val message: String) : ParQState
}

sealed interface ParQEvent {
    data class Answer(val questionId: ParQQuestionId, val value: Boolean) : ParQEvent
    data object Submit : ParQEvent
}

class ParQViewModel(private val submitParQAnswers: SubmitParQAnswers) : ViewModel() {
    private val _state = MutableStateFlow<ParQState>(ParQState.Questions())
    val state: StateFlow<ParQState> = _state.asStateFlow()

    fun onEvent(event: ParQEvent) {
        when (event) {
            is ParQEvent.Answer -> answer(event.questionId, event.value)
            ParQEvent.Submit -> submit()
        }
    }

    private fun answer(questionId: ParQQuestionId, value: Boolean) {
        val current = _state.value
        if (current !is ParQState.Questions) return
        _state.update { ParQState.Questions(current.answers + (questionId to value)) }
    }

    private fun submit() {
        val current = _state.value
        if (current !is ParQState.Questions || !current.canProceed) return

        viewModelScope.launch {
            _state.value = ParQState.Submitting
            val answers = current.answers.map { (questionId, value) -> ParQAnswer(questionId, value!!) }
            when (val result = submitParQAnswers(answers)) {
                is AppResult.Success -> _state.value = ParQState.Submitted(
                    requiresDoctorConsult = result.data.requiresDoctorConsult,
                    parQResultId = result.data.id,
                )
                is AppResult.Error -> _state.value = ParQState.Error(result.failure.message)
            }
        }
    }
}
