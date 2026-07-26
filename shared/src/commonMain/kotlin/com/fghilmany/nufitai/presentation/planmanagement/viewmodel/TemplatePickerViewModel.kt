package com.fghilmany.nufitai.presentation.planmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.domain.onboarding.entity.ResolvedSplit
import com.fghilmany.nufitai.usecase.planmanagement.CreateTemplatePlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** P-14 UC-4 -- 3-step manual picker (Level -> Goal -> Split), no assessment. */
sealed interface TemplatePickerState {
    data class Step(
        val level: Level? = null,
        val goal: GoalCategory? = null,
        val resolvedSplit: ResolvedSplit? = null,
    ) : TemplatePickerState {
        val canSubmit: Boolean get() = level != null && goal != null && resolvedSplit != null
    }
    data object Submitting : TemplatePickerState
    data class Completed(val plan: MonthlyPlan) : TemplatePickerState
    data class Error(val message: String) : TemplatePickerState
}

sealed interface TemplatePickerEvent {
    data class SetLevel(val value: Level) : TemplatePickerEvent
    data class SetGoal(val value: GoalCategory) : TemplatePickerEvent
    data class SetSplit(val value: ResolvedSplit) : TemplatePickerEvent
    /** AC-7 alternate flow -- "Simpan Draft" (no activation) vs "Aktifkan Sekarang". */
    data class Submit(val activateNow: Boolean) : TemplatePickerEvent
}

class TemplatePickerViewModel(
    private val createTemplatePlan: CreateTemplatePlan,
) : ViewModel() {
    private val _state = MutableStateFlow<TemplatePickerState>(TemplatePickerState.Step())
    val state: StateFlow<TemplatePickerState> = _state.asStateFlow()

    fun onEvent(event: TemplatePickerEvent) {
        when (event) {
            is TemplatePickerEvent.SetLevel -> updateStep { it.copy(level = event.value) }
            is TemplatePickerEvent.SetGoal -> updateStep { it.copy(goal = event.value) }
            is TemplatePickerEvent.SetSplit -> updateStep { it.copy(resolvedSplit = event.value) }
            is TemplatePickerEvent.Submit -> submit(event.activateNow)
        }
    }

    private fun updateStep(transform: (TemplatePickerState.Step) -> TemplatePickerState.Step) {
        _state.update { current -> if (current is TemplatePickerState.Step) transform(current) else current }
    }

    private fun submit(activateNow: Boolean) {
        val current = _state.value
        if (current !is TemplatePickerState.Step || !current.canSubmit) return

        viewModelScope.launch {
            _state.value = TemplatePickerState.Submitting
            val result = createTemplatePlan(
                level = current.level ?: return@launch,
                goal = current.goal ?: return@launch,
                resolvedSplit = current.resolvedSplit ?: return@launch,
                activateNow = activateNow,
            )
            when (result) {
                is AppResult.Success -> _state.value = TemplatePickerState.Completed(result.data)
                is AppResult.Error -> _state.value = TemplatePickerState.Error(result.failure.message)
            }
        }
    }
}
