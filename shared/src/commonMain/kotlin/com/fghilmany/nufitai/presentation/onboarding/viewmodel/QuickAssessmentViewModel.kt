package com.fghilmany.nufitai.presentation.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.onboarding.entity.Experience
import com.fghilmany.nufitai.domain.onboarding.entity.FrequencyBucket
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.domain.onboarding.entity.QuickAssessmentAnswer
import com.fghilmany.nufitai.domain.onboarding.entity.QuickAssessmentResult
import com.fghilmany.nufitai.domain.onboarding.entity.SplitPreference
import com.fghilmany.nufitai.domain.monthlyplan.entity.TemplatePlanInput
import com.fghilmany.nufitai.usecase.monthlyplan.GenerateLocalTemplatePlan
import com.fghilmany.nufitai.usecase.onboarding.SubmitQuickAssessment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val QUICK_ASSESSMENT_TOTAL_STEPS = 5

sealed interface QuickAssessmentState {
    data class Step(
        val stepIndex: Int = 1,
        val experience: Experience? = null,
        val goal: GoalCategory? = null,
        val equipment: Set<EquipmentCategory> = emptySet(),
        val frequency: FrequencyBucket? = null,
        val splitPreference: SplitPreference? = null,
    ) : QuickAssessmentState {
        val canGoBack: Boolean get() = stepIndex > 1
        val canGoNext: Boolean
            get() = when (stepIndex) {
                1 -> experience != null
                2 -> goal != null
                3 -> equipment.isNotEmpty()
                4 -> frequency != null
                5 -> splitPreference != null
                else -> false
            }
    }
    data object Generating : QuickAssessmentState
    data class Completed(val result: QuickAssessmentResult) : QuickAssessmentState
    data class Error(val message: String) : QuickAssessmentState
}

/**
 * The equipment actually offered as toggleable cards in this wizard (`equipmentGridOptions()` +
 * the always-visible Cardio card) -- NOT `EquipmentCategory.entries` (ADR-007 added 4 more
 * values with no card here; using `.entries` would silently include equipment the user was
 * never shown, breaking "Select All"'s all-selected detection).
 */
private val SELECT_ALL_EQUIPMENT = setOf(
    EquipmentCategory.BODYWEIGHT,
    EquipmentCategory.DUMBBELL,
    EquipmentCategory.BARBELL,
    EquipmentCategory.KETTLEBELL,
    EquipmentCategory.MACHINE_CABLE,
    EquipmentCategory.PULL_UP_BAR,
    EquipmentCategory.CARDIO_EQUIPMENT,
)

sealed interface QuickAssessmentEvent {
    data class SelectExperience(val value: Experience) : QuickAssessmentEvent
    data class SelectGoal(val value: GoalCategory) : QuickAssessmentEvent
    data class ToggleEquipment(val value: EquipmentCategory) : QuickAssessmentEvent
    data object SelectAllEquipment : QuickAssessmentEvent
    data class SelectFrequency(val value: FrequencyBucket) : QuickAssessmentEvent
    data class SelectSplit(val value: SplitPreference) : QuickAssessmentEvent
    data object NextStep : QuickAssessmentEvent
    data object PreviousStep : QuickAssessmentEvent
}

class QuickAssessmentViewModel(
    private val submitQuickAssessment: SubmitQuickAssessment,
    private val generateLocalTemplatePlan: GenerateLocalTemplatePlan,
) : ViewModel() {
    private val _state = MutableStateFlow<QuickAssessmentState>(QuickAssessmentState.Step())
    val state: StateFlow<QuickAssessmentState> = _state.asStateFlow()

    fun onEvent(event: QuickAssessmentEvent) {
        when (event) {
            is QuickAssessmentEvent.SelectExperience -> updateStep { it.copy(experience = event.value) }
            is QuickAssessmentEvent.SelectGoal -> updateStep { it.copy(goal = event.value) }
            is QuickAssessmentEvent.ToggleEquipment -> toggleEquipment(event.value)
            QuickAssessmentEvent.SelectAllEquipment -> selectAllEquipment()
            is QuickAssessmentEvent.SelectFrequency -> updateStep { it.copy(frequency = event.value) }
            is QuickAssessmentEvent.SelectSplit -> updateStep { it.copy(splitPreference = event.value) }
            QuickAssessmentEvent.NextStep -> nextStep()
            QuickAssessmentEvent.PreviousStep -> previousStep()
        }
    }

    private fun updateStep(transform: (QuickAssessmentState.Step) -> QuickAssessmentState.Step) {
        _state.update { current -> if (current is QuickAssessmentState.Step) transform(current) else current }
    }

    private fun toggleEquipment(value: EquipmentCategory) = updateStep { step ->
        val updated = if (value in step.equipment) step.equipment - value else step.equipment + value
        step.copy(equipment = updated)
    }

    private fun selectAllEquipment() = updateStep { step ->
        val allSelected = SELECT_ALL_EQUIPMENT.all { it in step.equipment }
        step.copy(equipment = if (allSelected) emptySet() else step.equipment + SELECT_ALL_EQUIPMENT)
    }

    private fun nextStep() {
        val current = _state.value
        if (current !is QuickAssessmentState.Step || !current.canGoNext) return

        if (current.stepIndex < QUICK_ASSESSMENT_TOTAL_STEPS) {
            _state.value = current.copy(stepIndex = current.stepIndex + 1)
        } else {
            submit(current)
        }
    }

    private fun previousStep() {
        val current = _state.value
        if (current !is QuickAssessmentState.Step || !current.canGoBack) return
        _state.value = current.copy(stepIndex = current.stepIndex - 1)
    }

    private fun submit(step: QuickAssessmentState.Step) {
        val answer = QuickAssessmentAnswer(
            experience = step.experience ?: return,
            goal = step.goal ?: return,
            equipment = step.equipment,
            frequency = step.frequency ?: return,
            splitPreference = step.splitPreference ?: return,
        )

        viewModelScope.launch {
            _state.value = QuickAssessmentState.Generating
            when (val result = submitQuickAssessment(answer)) {
                is AppResult.Success -> {
                    // Local tier: template-matrix plan generation happens right after Quick
                    // Assessment, so P-03 has an active plan the moment onboarding finishes.
                    val templateInput = TemplatePlanInput(
                        level = result.data.level,
                        goal = result.data.input.goal,
                        equipment = result.data.input.equipment,
                        sessionsPerWeek = result.data.input.frequency.sessionsPerWeek,
                        resolvedSplit = result.data.resolvedSplit,
                    )
                    when (val planResult = generateLocalTemplatePlan(templateInput)) {
                        is AppResult.Success -> _state.value = QuickAssessmentState.Completed(result.data)
                        is AppResult.Error -> _state.value = QuickAssessmentState.Error(planResult.failure.message)
                    }
                }
                is AppResult.Error -> _state.value = QuickAssessmentState.Error(result.failure.message)
            }
        }
    }
}
