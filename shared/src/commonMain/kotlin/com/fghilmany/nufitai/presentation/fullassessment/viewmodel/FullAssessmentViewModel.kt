package com.fghilmany.nufitai.presentation.fullassessment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import com.fghilmany.nufitai.domain.fullassessment.entity.ActivityLevel
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
import com.fghilmany.nufitai.domain.fullassessment.entity.TrainingLocation
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.domain.onboarding.entity.ParQQuestionId
import com.fghilmany.nufitai.usecase.fullassessment.CompleteFullAssessment
import com.fghilmany.nufitai.usecase.monthlyplan.ActivatePlanFromFullAssessment
import com.fghilmany.nufitai.usecase.fullassessment.FullAssessmentParQGateResult
import com.fghilmany.nufitai.usecase.fullassessment.SubmitFullAssessmentParQ
import com.fghilmany.nufitai.usecase.onboarding.GetLatestParQResult
import com.fghilmany.nufitai.usecase.onboarding.GetLatestQuickAssessmentResult
import com.fghilmany.nufitai.usecase.profile.GetProfileBasics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.hours

/** Sequential wizard order -- GATE_BLOCKED is a branch, not a numbered step. */
enum class FullAssessmentPhase { PAR_Q, GATE_BLOCKED, INTERVIEW, POSTURAL, MOVEMENT, CAPACITY_TEST }

val POSTURAL_FLAG_OPTIONS: List<ExerciseFlag> = ExerciseFlag.entries.filter { it.name.startsWith("POSTURAL_") }
val MOVEMENT_FLAG_OPTIONS: List<ExerciseFlag> = ExerciseFlag.entries.filter { it.name.startsWith("MOVEMENT_") }

/** Equipment realistically available at home -- excludes gym-only machines so a beginner training
 * at home isn't shown options they can't act on. Gym covers everything, home is the narrower set. */
val HOME_EQUIPMENT_OPTIONS: Set<EquipmentCategory> = setOf(
    EquipmentCategory.BODYWEIGHT,
    EquipmentCategory.DUMBBELL,
    EquipmentCategory.KETTLEBELL,
    EquipmentCategory.RESISTANCE_BAND,
    EquipmentCategory.STABILITY_BALL,
    EquipmentCategory.MEDICINE_BALL,
    EquipmentCategory.ROLLER,
)
val GYM_EQUIPMENT_OPTIONS: Set<EquipmentCategory> = EquipmentCategory.entries.toSet()

fun TrainingLocation.equipmentOptions(): Set<EquipmentCategory> = when (this) {
    TrainingLocation.HOME -> HOME_EQUIPMENT_OPTIONS
    TrainingLocation.GYM -> GYM_EQUIPMENT_OPTIONS
}

sealed interface FullAssessmentState {
    data class Step(
        val phase: FullAssessmentPhase = FullAssessmentPhase.PAR_Q,
        val parQAnswers: Map<FullAssessmentParQQuestionId, Boolean?> =
            FullAssessmentParQQuestionId.entries.associateWith { null },
        /** True once any PAR-Q answer was auto-filled from a recent Quick Assessment result --
         * drives the "cek ulang ya" notice on the PAR-Q screen. */
        val parQPrefilled: Boolean = false,
        val gateResult: FullAssessmentParQGateResult? = null,
        val gateAcknowledged: Boolean = false,
        val age: String = "",
        val gender: Gender? = null,
        val trainingLocation: TrainingLocation? = null,
        val equipmentPreference: Set<EquipmentCategory> = emptySet(),
        val injuryHistory: Set<BodyArea> = emptySet(),
        /** issue #81 -- free-text detail under the "cedera/nyeri sendi" toggle. */
        val injuryHistoryNote: String = "",
        val goals: Set<GoalCategory> = emptySet(),
        /** Frequency is derived from [selectedWeekdays].size -- no separate number to type that
         * could disagree with the days actually picked (max 7, same as a calendar week). */
        val selectedWeekdays: Set<Int> = emptySet(),
        val sessionDurationMinutes: Int? = null,
        /** techspec meal-plan.md §3/§5 -- TDEE calculation basis, asked alongside the rest of the interview step. */
        val activityLevel: ActivityLevel? = null,
        /** "Tidak ada cedera/nyeri" -- mutually exclusive with [injuryHistory]; picking a real
         * area clears this, and checking this clears [injuryHistory]/[injuryHistoryNote]. */
        val noInjuryHistory: Boolean = false,
        val flagsPostural: Set<ExerciseFlag> = emptySet(),
        /** "Tidak ada temuan" -- mutually exclusive with [flagsPostural], same pattern as [noInjuryHistory]. */
        val noPosturalFindings: Boolean = false,
        val movementFlags: Set<ExerciseFlag> = emptySet(),
        /** "Tidak ada temuan" -- mutually exclusive with [movementFlags], same pattern as [noInjuryHistory]. */
        val noMovementFindings: Boolean = false,
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
                trainingLocation != null &&
                equipmentPreference.isNotEmpty() &&
                goals.isNotEmpty() &&
                selectedWeekdays.isNotEmpty() &&
                sessionDurationMinutes != null &&
                (noInjuryHistory || injuryHistory.isNotEmpty())
        val canProceedFromPostural: Boolean get() = noPosturalFindings || flagsPostural.isNotEmpty()
        val canProceedFromMovement: Boolean get() = noMovementFindings || movementFlags.isNotEmpty()
        // activityLevel is intentionally NOT required to proceed -- mirrors BodyMeasurement's
        // existing optional-everywhere convention. Meal Plan's own NeedsBodyData gate (meal-plan.md
        // §8) enforces it specifically for that feature, without adding friction to the Full
        // Assessment flow itself (used by the Logged-In tier's rule engine too, not just Meal Plan).
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
    data class SetTrainingLocation(val value: TrainingLocation) : FullAssessmentEvent
    data class ToggleEquipmentPreference(val value: EquipmentCategory) : FullAssessmentEvent
    data class SelectAllEquipment(val options: Set<EquipmentCategory>) : FullAssessmentEvent
    data class ToggleInjuryHistory(val value: BodyArea) : FullAssessmentEvent
    data object ToggleNoInjuryHistory : FullAssessmentEvent
    data class SetInjuryHistoryNote(val value: String) : FullAssessmentEvent
    data class ToggleGoal(val value: GoalCategory) : FullAssessmentEvent
    data class ToggleSelectedWeekday(val value: Int) : FullAssessmentEvent
    data class SetSessionDurationMinutes(val value: Int) : FullAssessmentEvent
    data class SetActivityLevel(val value: ActivityLevel) : FullAssessmentEvent
    data object NextFromInterview : FullAssessmentEvent
    data class TogglePosturalFlag(val value: ExerciseFlag) : FullAssessmentEvent
    data object ToggleNoPosturalFindings : FullAssessmentEvent
    data object NextFromPostural : FullAssessmentEvent
    data class ToggleMovementFlag(val value: ExerciseFlag) : FullAssessmentEvent
    data object ToggleNoMovementFindings : FullAssessmentEvent
    data object NextFromMovement : FullAssessmentEvent
    data class SetPushupReps(val value: String) : FullAssessmentEvent
    data class SetPlankSeconds(val value: String) : FullAssessmentEvent
    data class SetSitToStandReps(val value: String) : FullAssessmentEvent
    /** Skip + submit in one step -- there's nothing left to fill in on this screen once skipped. */
    data object SkipCapacityTestAndSubmit : FullAssessmentEvent
    data object Submit : FullAssessmentEvent
}

class FullAssessmentViewModel(
    private val submitFullAssessmentParQ: SubmitFullAssessmentParQ,
    private val completeFullAssessment: CompleteFullAssessment,
    private val activatePlanFromFullAssessment: ActivatePlanFromFullAssessment,
    private val getProfileBasics: GetProfileBasics,
    private val getLatestQuickAssessmentResult: GetLatestQuickAssessmentResult,
    private val getLatestParQResult: GetLatestParQResult,
) : ViewModel() {
    private val _state = MutableStateFlow<FullAssessmentState>(FullAssessmentState.Step())
    val state: StateFlow<FullAssessmentState> = _state.asStateFlow()

    init {
        prefillFromExistingData()
    }

    /** Reuse age/gender from "Lengkapi Profil" and goal/equipment/frequency from Quick Assessment
     * instead of asking the user to type them again -- both are already stored locally by the time
     * Full Assessment (login tier) runs. */
    private fun prefillFromExistingData() {
        viewModelScope.launch {
            (getProfileBasics().let { it as? AppResult.Success }?.data)?.let { basics ->
                updateStep {
                    it.copy(
                        age = it.age.ifBlank { basics.age.toString() },
                        gender = it.gender ?: basics.gender,
                        // Q6_PREGNANCY is irrelevant for a male user -- answer it "tidak" up front
                        // (the correct answer, not a placeholder) so the PAR-Q screen can hide the
                        // question without leaving canSubmitParQ permanently blocked on it.
                        parQAnswers = if (basics.gender == Gender.MALE && it.parQAnswers[FullAssessmentParQQuestionId.Q6_PREGNANCY] == null) {
                            it.parQAnswers + (FullAssessmentParQQuestionId.Q6_PREGNANCY to false)
                        } else {
                            it.parQAnswers
                        },
                    )
                }
            }
            (getLatestQuickAssessmentResult().let { it as? AppResult.Success }?.data)?.let { quickResult ->
                updateStep {
                    it.copy(
                        goals = it.goals.ifEmpty { setOf(quickResult.input.goal) },
                        equipmentPreference = it.equipmentPreference.ifEmpty { quickResult.input.equipment },
                    )
                }
            }
            // Prefill the ~7 PAR-Q questions Full Assessment re-asks verbatim from Quick
            // Assessment, but ONLY if answered within the last 24h -- health state can genuinely
            // change, so a stale answer must NOT be silently reused (the user would just be
            // clicking through a safety gate without re-checking). Past that window the question
            // is left blank like normal.
            (getLatestParQResult().let { it as? AppResult.Success }?.data)?.let { parQResult ->
                if (currentInstant() - parQResult.answeredAt <= 24.hours) {
                    val prefillable = parQResult.answers.mapNotNull { quickAnswer ->
                        quickAnswer.questionId.toFullAssessmentEquivalentOrNull()?.let { it to quickAnswer.answer }
                    }
                    updateStep { step ->
                        val toApply = prefillable.filter { (id, _) -> step.parQAnswers[id] == null }
                        if (toApply.isEmpty()) {
                            step
                        } else {
                            step.copy(parQAnswers = step.parQAnswers + toApply, parQPrefilled = true)
                        }
                    }
                }
            }
        }
    }

    fun onEvent(event: FullAssessmentEvent) {
        when (event) {
            is FullAssessmentEvent.AnswerParQ -> updateStep { it.copy(parQAnswers = it.parQAnswers + (event.questionId to event.value)) }
            FullAssessmentEvent.SubmitParQ -> submitParQ()
            FullAssessmentEvent.ToggleGateAcknowledge -> updateStep { it.copy(gateAcknowledged = !it.gateAcknowledged) }
            FullAssessmentEvent.ContinueFromGate -> continueFromGate()
            is FullAssessmentEvent.SetUsia -> updateStep { it.copy(age = event.value) }
            is FullAssessmentEvent.SetGender -> updateStep { it.copy(gender = event.value) }
            is FullAssessmentEvent.SetTrainingLocation -> updateStep {
                // Equipment choices only make sense for the location they were picked under --
                // switching location clears them rather than leaving a stale gym/home mix.
                if (it.trainingLocation == event.value) it else it.copy(trainingLocation = event.value, equipmentPreference = emptySet())
            }
            is FullAssessmentEvent.ToggleEquipmentPreference -> updateStep {
                it.copy(equipmentPreference = it.equipmentPreference.toggle(event.value))
            }
            is FullAssessmentEvent.SelectAllEquipment -> updateStep {
                val allSelected = event.options.isNotEmpty() && it.equipmentPreference.containsAll(event.options)
                it.copy(
                    equipmentPreference = if (allSelected) it.equipmentPreference - event.options else it.equipmentPreference + event.options,
                )
            }
            is FullAssessmentEvent.ToggleInjuryHistory -> updateStep {
                it.copy(injuryHistory = it.injuryHistory.toggle(event.value), noInjuryHistory = false)
            }
            FullAssessmentEvent.ToggleNoInjuryHistory -> updateStep {
                val turningOn = !it.noInjuryHistory
                it.copy(
                    noInjuryHistory = turningOn,
                    injuryHistory = if (turningOn) emptySet() else it.injuryHistory,
                    injuryHistoryNote = if (turningOn) "" else it.injuryHistoryNote,
                )
            }
            is FullAssessmentEvent.SetInjuryHistoryNote -> updateStep { it.copy(injuryHistoryNote = event.value) }
            is FullAssessmentEvent.ToggleGoal -> updateStep { it.copy(goals = it.goals.toggle(event.value)) }
            is FullAssessmentEvent.ToggleSelectedWeekday -> updateStep { it.copy(selectedWeekdays = it.selectedWeekdays.toggle(event.value)) }
            is FullAssessmentEvent.SetSessionDurationMinutes -> updateStep { it.copy(sessionDurationMinutes = event.value) }
            is FullAssessmentEvent.SetActivityLevel -> updateStep { it.copy(activityLevel = event.value) }
            FullAssessmentEvent.NextFromInterview -> updateStep {
                if (it.canProceedFromInterview) it.copy(phase = FullAssessmentPhase.POSTURAL) else it
            }
            is FullAssessmentEvent.TogglePosturalFlag -> updateStep {
                it.copy(flagsPostural = it.flagsPostural.toggle(event.value), noPosturalFindings = false)
            }
            FullAssessmentEvent.ToggleNoPosturalFindings -> updateStep {
                val turningOn = !it.noPosturalFindings
                it.copy(noPosturalFindings = turningOn, flagsPostural = if (turningOn) emptySet() else it.flagsPostural)
            }
            FullAssessmentEvent.NextFromPostural -> updateStep {
                if (it.canProceedFromPostural) it.copy(phase = FullAssessmentPhase.MOVEMENT) else it
            }
            is FullAssessmentEvent.ToggleMovementFlag -> updateStep {
                it.copy(movementFlags = it.movementFlags.toggle(event.value), noMovementFindings = false)
            }
            FullAssessmentEvent.ToggleNoMovementFindings -> updateStep {
                val turningOn = !it.noMovementFindings
                it.copy(noMovementFindings = turningOn, movementFlags = if (turningOn) emptySet() else it.movementFlags)
            }
            FullAssessmentEvent.NextFromMovement -> updateStep {
                if (it.canProceedFromMovement) it.copy(phase = FullAssessmentPhase.CAPACITY_TEST) else it
            }
            is FullAssessmentEvent.SetPushupReps -> updateStep { it.copy(pushupReps = event.value) }
            is FullAssessmentEvent.SetPlankSeconds -> updateStep { it.copy(plankSeconds = event.value) }
            is FullAssessmentEvent.SetSitToStandReps -> updateStep { it.copy(sitToStandReps = event.value) }
            FullAssessmentEvent.SkipCapacityTestAndSubmit -> {
                updateStep { it.copy(capacityTestSkipped = true) }
                submit()
            }
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
                injuryHistoryNote = current.injuryHistoryNote.ifBlank { null },
                flagsPostural = current.flagsPostural,
                movementFlags = current.movementFlags,
                capacityTest = current.toCapacityTestResult(),
                goals = current.goals.ifEmpty { return@launch },
                sessionsPerWeek = current.selectedWeekdays.size,
                selectedWeekdays = current.selectedWeekdays,
                sessionDurationMinutes = current.sessionDurationMinutes ?: return@launch,
                activityLevel = current.activityLevel,
                trainingLocation = current.trainingLocation,
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

/** Quick Assessment's PAR-Q ringkas (onboarding.entity.ParQQuestionId) and Full Assessment's PAR-Q
 * (FullAssessmentParQQuestionId) share 7 near-identical questions -- mapped here for the recency-
 * gated prefill above. Quick-only (heart diagnosis, chest pain at rest, joint problem, routine
 * medication, other condition) and Full-only (breathless light activity, asthma, old injury,
 * osteoporosis) questions have no equivalent and are intentionally left unmapped. */
private fun ParQQuestionId.toFullAssessmentEquivalentOrNull(): FullAssessmentParQQuestionId? = when (this) {
    ParQQuestionId.Q2_CHEST_PAIN_ACTIVITY -> FullAssessmentParQQuestionId.Q1_CHEST_PAIN
    ParQQuestionId.Q4_DIZZINESS_FAINTING -> FullAssessmentParQQuestionId.Q2_FAINTING_DURING_ACTIVITY
    ParQQuestionId.Q7_RECENT_SURGERY -> FullAssessmentParQQuestionId.Q4_MAJOR_SURGERY_NOT_RECOVERED
    ParQQuestionId.Q10_ACTIVE_CANCER -> FullAssessmentParQQuestionId.Q5_ACTIVE_CANCER_TREATMENT
    ParQQuestionId.Q11_PREGNANCY -> FullAssessmentParQQuestionId.Q6_PREGNANCY
    ParQQuestionId.Q9_DIABETES -> FullAssessmentParQQuestionId.Q7_DIABETES_CONTROLLED
    ParQQuestionId.Q5_BLOOD_PRESSURE -> FullAssessmentParQQuestionId.Q8_HYPERTENSION_CONTROLLED
    else -> null
}
