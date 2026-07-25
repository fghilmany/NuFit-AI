package com.fghilmany.nufitai.presentation.fullassessment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import com.fghilmany.nufitai.domain.fullassessment.entity.CapacityTestResult
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentParQAnswer
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentParQQuestionId
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentResult
import com.fghilmany.nufitai.domain.fullassessment.entity.Gender
import com.fghilmany.nufitai.domain.fullassessment.entity.PlankResult
import com.fghilmany.nufitai.domain.fullassessment.entity.PushupResult
import com.fghilmany.nufitai.domain.fullassessment.entity.PushupVersion
import com.fghilmany.nufitai.domain.fullassessment.entity.SitToStandResult
import com.fghilmany.nufitai.domain.fullassessment.entity.TestProtocol
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.usecase.fullassessment.CompleteFullAssessment
import com.fghilmany.nufitai.usecase.monthlyplan.ActivatePlanFromFullAssessment
import com.fghilmany.nufitai.usecase.fullassessment.FullAssessmentParQGateResult
import com.fghilmany.nufitai.usecase.fullassessment.SubmitFullAssessmentParQ
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Sequential wizard order -- GATE_BLOCKED is a branch, not a numbered step. */
enum class FullAssessmentPhase { PAR_Q, GATE_BLOCKED, INTERVIEW, POSTURAL, MOVEMENT, CAPACITY_TEST }

val POSTURAL_FLAG_OPTIONS: List<ExerciseFlag> = ExerciseFlag.entries.filter { it.name.startsWith("POSTURAL_") }
val MOVEMENT_FLAG_OPTIONS: List<ExerciseFlag> = ExerciseFlag.entries.filter { it.name.startsWith("MOVEMENT_") }

sealed interface FullAssessmentState {
    data class Step(
        val phase: FullAssessmentPhase = FullAssessmentPhase.PAR_Q,
        val parQAnswers: Map<FullAssessmentParQQuestionId, Boolean?> =
            FullAssessmentParQQuestionId.entries.associateWith { null },
        val gateResult: FullAssessmentParQGateResult? = null,
        val gateAcknowledged: Boolean = false,
        val age: String = "",
        val gender: Gender? = null,
        val equipmentPreference: Set<EquipmentCategory> = emptySet(),
        val injuryHistory: Set<BodyArea> = emptySet(),
        val goal: GoalCategory? = null,
        val sessionsPerWeek: String = "",
        val selectedWeekdays: Set<Int> = emptySet(),
        val sessionDurationMinutes: String = "",
        val flagsPostural: Set<ExerciseFlag> = emptySet(),
        val movementFlags: Set<ExerciseFlag> = emptySet(),
        val capacityTestSkipped: Boolean = false,
        val pushupReps: String = "",
        val plankSeconds: String = "",
        val sitToStandReps: String = "",
    ) : FullAssessmentState {
        val canSubmitParQ: Boolean get() = parQAnswers.values.all { it != null }
        val canProceedFromGate: Boolean get() = gateAcknowledged
        val canProceedFromInterview: Boolean
            get() = age.toIntOrNull() != null &&
                gender != null &&
                equipmentPreference.isNotEmpty() &&
                goal != null &&
                sessionsPerWeek.toIntOrNull() != null &&
                selectedWeekdays.isNotEmpty() &&
                sessionDurationMinutes.toIntOrNull() != null
        val canSubmit: Boolean
            get() = capacityTestSkipped ||
                pushupReps.isNotBlank() || plankSeconds.isNotBlank() || sitToStandReps.isNotBlank()
    }
    data object Submitting : FullAssessmentState
    data class Completed(val result: FullAssessmentResult) : FullAssessmentState
    data class Error(val message: String) : FullAssessmentState
}

sealed interface FullAssessmentEvent {
    data class AnswerParQ(val questionId: FullAssessmentParQQuestionId, val value: Boolean) : FullAssessmentEvent
    data object SubmitParQ : FullAssessmentEvent
    data object ToggleGateAcknowledge : FullAssessmentEvent
    data object ContinueFromGate : FullAssessmentEvent
    data class SetUsia(val value: String) : FullAssessmentEvent
    data class SetGender(val value: Gender) : FullAssessmentEvent
    data class ToggleEquipmentPreference(val value: EquipmentCategory) : FullAssessmentEvent
    data class ToggleInjuryHistory(val value: BodyArea) : FullAssessmentEvent
    data class SetGoal(val value: GoalCategory) : FullAssessmentEvent
    data class SetSessionsPerWeek(val value: String) : FullAssessmentEvent
    data class ToggleSelectedWeekday(val value: Int) : FullAssessmentEvent
    data class SetSessionDurationMinutes(val value: String) : FullAssessmentEvent
    data object NextFromInterview : FullAssessmentEvent
    data class TogglePosturalFlag(val value: ExerciseFlag) : FullAssessmentEvent
    data object NextFromPostural : FullAssessmentEvent
    data class ToggleMovementFlag(val value: ExerciseFlag) : FullAssessmentEvent
    data object NextFromMovement : FullAssessmentEvent
    data class SetPushupReps(val value: String) : FullAssessmentEvent
    data class SetPlankSeconds(val value: String) : FullAssessmentEvent
    data class SetSitToStandReps(val value: String) : FullAssessmentEvent
    data object ToggleSkipCapacityTest : FullAssessmentEvent
    data object Submit : FullAssessmentEvent
}

class FullAssessmentViewModel(
    private val submitFullAssessmentParQ: SubmitFullAssessmentParQ,
    private val completeFullAssessment: CompleteFullAssessment,
    private val activatePlanFromFullAssessment: ActivatePlanFromFullAssessment,
) : ViewModel() {
    private val _state = MutableStateFlow<FullAssessmentState>(FullAssessmentState.Step())
    val state: StateFlow<FullAssessmentState> = _state.asStateFlow()

    fun onEvent(event: FullAssessmentEvent) {
        when (event) {
            is FullAssessmentEvent.AnswerParQ -> updateStep { it.copy(parQAnswers = it.parQAnswers + (event.questionId to event.value)) }
            FullAssessmentEvent.SubmitParQ -> submitParQ()
            FullAssessmentEvent.ToggleGateAcknowledge -> updateStep { it.copy(gateAcknowledged = !it.gateAcknowledged) }
            FullAssessmentEvent.ContinueFromGate -> continueFromGate()
            is FullAssessmentEvent.SetUsia -> updateStep { it.copy(age = event.value) }
            is FullAssessmentEvent.SetGender -> updateStep { it.copy(gender = event.value) }
            is FullAssessmentEvent.ToggleEquipmentPreference -> updateStep {
                it.copy(equipmentPreference = it.equipmentPreference.toggle(event.value))
            }
            is FullAssessmentEvent.ToggleInjuryHistory -> updateStep {
                it.copy(injuryHistory = it.injuryHistory.toggle(event.value))
            }
            is FullAssessmentEvent.SetGoal -> updateStep { it.copy(goal = event.value) }
            is FullAssessmentEvent.SetSessionsPerWeek -> updateStep { it.copy(sessionsPerWeek = event.value) }
            is FullAssessmentEvent.ToggleSelectedWeekday -> updateStep { it.copy(selectedWeekdays = it.selectedWeekdays.toggle(event.value)) }
            is FullAssessmentEvent.SetSessionDurationMinutes -> updateStep { it.copy(sessionDurationMinutes = event.value) }
            FullAssessmentEvent.NextFromInterview -> updateStep {
                if (it.canProceedFromInterview) it.copy(phase = FullAssessmentPhase.POSTURAL) else it
            }
            is FullAssessmentEvent.TogglePosturalFlag -> updateStep {
                it.copy(flagsPostural = it.flagsPostural.toggle(event.value))
            }
            FullAssessmentEvent.NextFromPostural -> updateStep { it.copy(phase = FullAssessmentPhase.MOVEMENT) }
            is FullAssessmentEvent.ToggleMovementFlag -> updateStep {
                it.copy(movementFlags = it.movementFlags.toggle(event.value))
            }
            FullAssessmentEvent.NextFromMovement -> updateStep { it.copy(phase = FullAssessmentPhase.CAPACITY_TEST) }
            is FullAssessmentEvent.SetPushupReps -> updateStep { it.copy(pushupReps = event.value) }
            is FullAssessmentEvent.SetPlankSeconds -> updateStep { it.copy(plankSeconds = event.value) }
            is FullAssessmentEvent.SetSitToStandReps -> updateStep { it.copy(sitToStandReps = event.value) }
            FullAssessmentEvent.ToggleSkipCapacityTest -> updateStep { it.copy(capacityTestSkipped = !it.capacityTestSkipped) }
            FullAssessmentEvent.Submit -> submit()
        }
    }

    private fun updateStep(transform: (FullAssessmentState.Step) -> FullAssessmentState.Step) {
        _state.update { current -> if (current is FullAssessmentState.Step) transform(current) else current }
    }

    private fun submitParQ() {
        val current = _state.value
        if (current !is FullAssessmentState.Step || !current.canSubmitParQ) return
        val answers = current.parQAnswers.map { (id, value) -> FullAssessmentParQAnswer(id, value ?: false) }
        val gateResult = submitFullAssessmentParQ(answers)
        _state.value = current.copy(
            gateResult = gateResult,
            phase = if (gateResult.hardStopFlagged) FullAssessmentPhase.GATE_BLOCKED else FullAssessmentPhase.INTERVIEW,
        )
    }

    private fun continueFromGate() {
        val current = _state.value
        if (current !is FullAssessmentState.Step || !current.canProceedFromGate) return
        _state.value = current.copy(phase = FullAssessmentPhase.INTERVIEW)
    }

    private fun submit() {
        val current = _state.value
        if (current !is FullAssessmentState.Step || !current.canSubmit) return
        val gateResult = current.gateResult ?: return

        viewModelScope.launch {
            _state.value = FullAssessmentState.Submitting
            val result = completeFullAssessment(
                age = current.age.toIntOrNull(),
                gender = current.gender,
                parQAnswers = current.parQAnswers.map { (id, value) -> FullAssessmentParQAnswer(id, value ?: false) },
                parQGateResult = gateResult,
                hardStopAcknowledgedAt = if (gateResult.hardStopFlagged) currentInstant() else null,
                equipmentPreference = current.equipmentPreference,
                injuryHistory = current.injuryHistory,
                flagsPostural = current.flagsPostural,
                movementFlags = current.movementFlags,
                capacityTest = current.toCapacityTestResult(),
                goal = current.goal ?: return@launch,
                sessionsPerWeek = current.sessionsPerWeek.toIntOrNull() ?: return@launch,
                selectedWeekdays = current.selectedWeekdays,
                sessionDurationMinutes = current.sessionDurationMinutes.toIntOrNull() ?: return@launch,
            )
            when (result) {
                is AppResult.Success -> {
                    // AC-6: Full Assessment completion (not login alone) triggers the Local -> Logged-In
                    // tier transition. A failure here still leaves the assessment itself saved --
                    // surfaced as an error rather than silently discarding the completed result.
                    when (val activated = activatePlanFromFullAssessment(result.data)) {
                        is AppResult.Success -> _state.value = FullAssessmentState.Completed(result.data)
                        is AppResult.Error -> _state.value = FullAssessmentState.Error(activated.failure.message)
                    }
                }
                is AppResult.Error -> _state.value = FullAssessmentState.Error(result.failure.message)
            }
        }
    }

    private fun FullAssessmentState.Step.toCapacityTestResult(): CapacityTestResult? {
        if (capacityTestSkipped) return null
        return CapacityTestResult(
            pushup = pushupReps.toIntOrNull()?.let { PushupResult(it, PushupVersion.STANDARD, TestProtocol.MAXIMAL) },
            plank = plankSeconds.toIntOrNull()?.let { PlankResult(it, TestProtocol.MAXIMAL) },
            sitToStand = sitToStandReps.toIntOrNull()?.let { SitToStandResult(it, TestProtocol.MAXIMAL) },
        )
    }
}

private fun <T> Set<T>.toggle(value: T): Set<T> = if (value in this) this - value else this + value
