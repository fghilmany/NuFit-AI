package com.fghilmany.nufitai.presentation.aicustomplan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.domain.aicustomplan.entity.AiPlanFormInput
import com.fghilmany.nufitai.domain.aicustomplan.entity.AiPlanGenerationRequest
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MuscleGroup
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.usecase.aicustomplan.BuildWorkoutHistorySummary
import com.fghilmany.nufitai.usecase.aicustomplan.CheckInjuryConflicts
import com.fghilmany.nufitai.usecase.aicustomplan.DetectProfileConflicts
import com.fghilmany.nufitai.usecase.aicustomplan.GenerateAiPlan
import com.fghilmany.nufitai.usecase.aicustomplan.ValidateAndFixExerciseIds
import com.fghilmany.nufitai.usecase.fullassessment.GetLatestFullAssessmentResult
import com.fghilmany.nufitai.usecase.onboarding.GetLatestQuickAssessmentResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** P-12 B (UC-3). */
sealed interface AiCustomPlanFormState {
    data class Editing(
        val goal: GoalCategory? = null,
        val sessionsPerWeek: Int = 3,
        val sessionDurationMinutes: Int = 45,
        val equipmentPreference: Set<EquipmentCategory> = emptySet(),
        val muscleFocus: Set<MuscleGroup> = emptySet(),
        val planDurationWeeks: Int = 2,
    ) : AiCustomPlanFormState {
        val canSubmit: Boolean get() = goal != null && equipmentPreference.isNotEmpty()
    }

    data object Generating : AiCustomPlanFormState

    /** AC-8 -- [retainedInput] is shown back to the user so the form is never re-entered blank. */
    data class Error(val failure: Failure, val retainedInput: Editing) : AiCustomPlanFormState

    /** Draft is already in [AiCustomPlanDraftHolder] -- the screen navigates to P-13 on this state. */
    data object Generated : AiCustomPlanFormState
}

sealed interface AiCustomPlanFormEvent {
    data class SetGoal(val value: GoalCategory) : AiCustomPlanFormEvent
    data class SetSessionsPerWeek(val value: Int) : AiCustomPlanFormEvent
    data class SetSessionDurationMinutes(val value: Int) : AiCustomPlanFormEvent
    data class ToggleEquipment(val value: EquipmentCategory) : AiCustomPlanFormEvent
    data class ToggleMuscleFocus(val value: MuscleGroup) : AiCustomPlanFormEvent
    data class SetPlanDurationWeeks(val value: Int) : AiCustomPlanFormEvent
    data object Submit : AiCustomPlanFormEvent
}

class AiCustomPlanFormViewModel(
    private val getLatestQuickAssessmentResult: GetLatestQuickAssessmentResult,
    private val getLatestFullAssessmentResult: GetLatestFullAssessmentResult,
    private val buildWorkoutHistorySummary: BuildWorkoutHistorySummary,
    private val detectProfileConflicts: DetectProfileConflicts,
    private val generateAiPlan: GenerateAiPlan,
    private val validateAndFixExerciseIds: ValidateAndFixExerciseIds,
    private val checkInjuryConflicts: CheckInjuryConflicts,
    private val draftHolder: AiCustomPlanDraftHolder,
) : ViewModel() {
    private val _state = MutableStateFlow<AiCustomPlanFormState>(AiCustomPlanFormState.Editing())
    val state: StateFlow<AiCustomPlanFormState> = _state.asStateFlow()

    fun onEvent(event: AiCustomPlanFormEvent) {
        when (event) {
            is AiCustomPlanFormEvent.SetGoal -> updateEditing { it.copy(goal = event.value) }
            is AiCustomPlanFormEvent.SetSessionsPerWeek -> updateEditing { it.copy(sessionsPerWeek = event.value) }
            is AiCustomPlanFormEvent.SetSessionDurationMinutes -> updateEditing { it.copy(sessionDurationMinutes = event.value) }
            is AiCustomPlanFormEvent.ToggleEquipment -> updateEditing {
                it.copy(equipmentPreference = it.equipmentPreference.toggle(event.value))
            }
            is AiCustomPlanFormEvent.ToggleMuscleFocus -> updateEditing {
                it.copy(muscleFocus = it.muscleFocus.toggle(event.value))
            }
            is AiCustomPlanFormEvent.SetPlanDurationWeeks -> updateEditing { it.copy(planDurationWeeks = event.value) }
            AiCustomPlanFormEvent.Submit -> submit()
        }
    }

    private fun updateEditing(transform: (AiCustomPlanFormState.Editing) -> AiCustomPlanFormState.Editing) {
        _state.update { current ->
            when (current) {
                is AiCustomPlanFormState.Editing -> transform(current)
                is AiCustomPlanFormState.Error -> transform(current.retainedInput)
                else -> current
            }
        }
    }

    private fun submit() {
        val editing = when (val current = _state.value) {
            is AiCustomPlanFormState.Editing -> current
            is AiCustomPlanFormState.Error -> current.retainedInput
            else -> return
        }
        if (!editing.canSubmit) return

        viewModelScope.launch {
            _state.value = AiCustomPlanFormState.Generating

            val level = when (val result = getLatestQuickAssessmentResult()) {
                is AppResult.Success -> result.data?.level ?: Level.BEGINNER
                is AppResult.Error -> return@launch fail(result.failure, editing)
            }
            val fullAssessment = when (val result = getLatestFullAssessmentResult()) {
                is AppResult.Success -> result.data ?: return@launch fail(Failure.Validation("Full Assessment not found"), editing)
                is AppResult.Error -> return@launch fail(result.failure, editing)
            }
            val historySummary = when (val result = buildWorkoutHistorySummary()) {
                is AppResult.Success -> result.data
                is AppResult.Error -> return@launch fail(result.failure, editing)
            }

            val formInput = AiPlanFormInput(
                goal = editing.goal ?: return@launch,
                sessionsPerWeek = editing.sessionsPerWeek,
                sessionDurationMinutes = editing.sessionDurationMinutes,
                equipmentPreference = editing.equipmentPreference,
                muscleFocus = editing.muscleFocus,
                planDurationWeeks = editing.planDurationWeeks,
            )
            val clientConflicts = detectProfileConflicts(formInput, level)

            val draft = when (val result = generateAiPlan(AiPlanGenerationRequest(formInput, fullAssessment, historySummary))) {
                is AppResult.Success -> result.data
                is AppResult.Error -> return@launch fail(result.failure, editing)
            }
            val validated = when (val result = validateAndFixExerciseIds(draft.days)) {
                is AppResult.Success -> result.data
                is AppResult.Error -> return@launch fail(result.failure, editing)
            }
            val activeFlags = fullAssessment.parQCategoryB + fullAssessment.flagsPostural + fullAssessment.movementFlags
            val injuryConflicts = when (val result = checkInjuryConflicts(validated.days, activeFlags, fullAssessment.injuryHistory)) {
                is AppResult.Success -> result.data
                is AppResult.Error -> return@launch fail(result.failure, editing)
            }

            draftHolder.current = PendingAiPlanDraft(
                formInput = formInput,
                smartGoalSummary = draft.smartGoalSummary,
                days = validated.days,
                conflicts = clientConflicts,
                serverNotes = draft.serverNotes,
                exerciseWarnings = validated.warnings,
                injuryConflicts = injuryConflicts,
            )
            _state.value = AiCustomPlanFormState.Generated
        }
    }

    private fun fail(failure: Failure, retainedInput: AiCustomPlanFormState.Editing) {
        _state.value = AiCustomPlanFormState.Error(failure, retainedInput)
    }
}

private fun <T> Set<T>.toggle(value: T): Set<T> = if (value in this) this - value else this + value
