package com.fghilmany.nufitai.presentation.aicustomplan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.core.util.generateId
import com.fghilmany.nufitai.domain.aicustomplan.entity.AiPlanGenerationRequest
import com.fghilmany.nufitai.domain.aicustomplan.entity.ExerciseValidationWarning
import com.fghilmany.nufitai.domain.aicustomplan.entity.InjuryConflict
import com.fghilmany.nufitai.domain.aicustomplan.entity.ProfileConflict
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentResult
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanSource
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanStatus
import com.fghilmany.nufitai.domain.monthlyplan.entity.ProgressionMode
import com.fghilmany.nufitai.usecase.aicustomplan.ActivateAiCustomPlan
import com.fghilmany.nufitai.usecase.aicustomplan.BuildWorkoutHistorySummary
import com.fghilmany.nufitai.usecase.aicustomplan.CheckDraftQuota
import com.fghilmany.nufitai.usecase.aicustomplan.CheckInjuryConflicts
import com.fghilmany.nufitai.usecase.aicustomplan.GenerateAiPlan
import com.fghilmany.nufitai.usecase.aicustomplan.SaveAiCustomPlanDraft
import com.fghilmany.nufitai.usecase.aicustomplan.ValidateAndFixExerciseIds
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExerciseAlternatives
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExercisePool
import com.fghilmany.nufitai.usecase.fullassessment.GetLatestFullAssessmentResult
import com.fghilmany.nufitai.usecase.planmanagement.DeletePlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val AI_CUSTOM_LEVEL_META = "AI_CUSTOM"

/** P-13 (UC-4). */
sealed interface AiCustomPlanPreviewState {
    data object Loading : AiCustomPlanPreviewState
    data class Loaded(
        val smartGoalSummary: String,
        val sessionDurationMinutes: Int,
        val days: List<PlanDay>,
        val exercisesById: Map<String, Exercise>,
        val conflicts: List<ProfileConflict>,
        val serverNotes: List<String>,
        val exerciseWarnings: List<ExerciseValidationWarning>,
        val injuryConflicts: List<InjuryConflict>,
        val swapDialog: SwapDialogState? = null,
        val adjustSetDialog: AdjustSetDialogState? = null,
    ) : AiCustomPlanPreviewState {
        /** AC-5/AC-14 (BLOCKING) -- activation is disabled while an injury conflict is unresolved. */
        val canActivate: Boolean get() = injuryConflicts.isEmpty()
    }
    data object Regenerating : AiCustomPlanPreviewState
    data class QuotaFull(val existingAiPlans: List<MonthlyPlan>) : AiCustomPlanPreviewState
    data object Saved : AiCustomPlanPreviewState
    data object Activated : AiCustomPlanPreviewState
    data class Error(val message: String) : AiCustomPlanPreviewState
}

data class SwapDialogState(val dayNumber: Int, val exerciseId: String, val candidates: List<Exercise>)
data class AdjustSetDialogState(val dayNumber: Int, val exerciseId: String, val sets: Int, val repRangeOrDuration: String)

sealed interface AiCustomPlanPreviewEvent {
    data object Regenerate : AiCustomPlanPreviewEvent
    data object SaveDraft : AiCustomPlanPreviewEvent
    data object Activate : AiCustomPlanPreviewEvent
    data class DeleteExistingDraft(val planId: String) : AiCustomPlanPreviewEvent
    data class RemoveExercise(val dayNumber: Int, val exerciseId: String) : AiCustomPlanPreviewEvent
    /** "Ganti Gerakan". */
    data class RequestSwap(val dayNumber: Int, val exerciseId: String) : AiCustomPlanPreviewEvent
    data class SelectSwapCandidate(val newExerciseId: String) : AiCustomPlanPreviewEvent
    data object DismissSwapDialog : AiCustomPlanPreviewEvent
    /** "Sesuaikan Set". */
    data class RequestAdjustSet(val dayNumber: Int, val exerciseId: String) : AiCustomPlanPreviewEvent
    data class ChangeAdjustSetSets(val delta: Int) : AiCustomPlanPreviewEvent
    data class ChangeAdjustSetRepRange(val value: String) : AiCustomPlanPreviewEvent
    data object ConfirmAdjustSet : AiCustomPlanPreviewEvent
    data object DismissAdjustSetDialog : AiCustomPlanPreviewEvent
}

class AiCustomPlanPreviewViewModel(
    private val checkDraftQuota: CheckDraftQuota,
    private val saveAiCustomPlanDraft: SaveAiCustomPlanDraft,
    private val activateAiCustomPlan: ActivateAiCustomPlan,
    private val deletePlan: DeletePlan,
    private val getLatestFullAssessmentResult: GetLatestFullAssessmentResult,
    private val buildWorkoutHistorySummary: BuildWorkoutHistorySummary,
    private val generateAiPlan: GenerateAiPlan,
    private val validateAndFixExerciseIds: ValidateAndFixExerciseIds,
    private val checkInjuryConflicts: CheckInjuryConflicts,
    private val getExercisePool: GetExercisePool,
    private val getExerciseAlternatives: GetExerciseAlternatives,
    private val draftHolder: AiCustomPlanDraftHolder,
) : ViewModel() {
    private val _state = MutableStateFlow<AiCustomPlanPreviewState>(AiCustomPlanPreviewState.Loading)
    val state: StateFlow<AiCustomPlanPreviewState> = _state.asStateFlow()

    init {
        loadExercisesThenShowDraft()
    }

    private fun loadExercisesThenShowDraft() {
        viewModelScope.launch {
            val draft = draftHolder.current
            if (draft == null) {
                _state.value = AiCustomPlanPreviewState.Error("No generated plan to preview")
                return@launch
            }
            val exercisesById = when (val result = getExercisePool()) {
                is AppResult.Success -> result.data.associateBy { it.id }
                is AppResult.Error -> return@launch fail(result.failure.message)
            }
            _state.value = draft.toLoadedState(exercisesById)
        }
    }

    fun onEvent(event: AiCustomPlanPreviewEvent) {
        when (event) {
            AiCustomPlanPreviewEvent.Regenerate -> regenerate()
            AiCustomPlanPreviewEvent.SaveDraft -> persist(activate = false)
            AiCustomPlanPreviewEvent.Activate -> persist(activate = true)
            is AiCustomPlanPreviewEvent.DeleteExistingDraft -> deleteThenRetryPersist(event.planId)
            is AiCustomPlanPreviewEvent.RemoveExercise -> removeExercise(event.dayNumber, event.exerciseId)
            is AiCustomPlanPreviewEvent.RequestSwap -> requestSwap(event.dayNumber, event.exerciseId)
            is AiCustomPlanPreviewEvent.SelectSwapCandidate -> selectSwapCandidate(event.newExerciseId)
            AiCustomPlanPreviewEvent.DismissSwapDialog -> updateLoaded { it.copy(swapDialog = null) }
            is AiCustomPlanPreviewEvent.RequestAdjustSet -> requestAdjustSet(event.dayNumber, event.exerciseId)
            is AiCustomPlanPreviewEvent.ChangeAdjustSetSets -> updateLoaded { current ->
                val dialog = current.adjustSetDialog ?: return@updateLoaded current
                current.copy(adjustSetDialog = dialog.copy(sets = (dialog.sets + event.delta).coerceAtLeast(1)))
            }
            is AiCustomPlanPreviewEvent.ChangeAdjustSetRepRange -> updateLoaded { current ->
                val dialog = current.adjustSetDialog ?: return@updateLoaded current
                current.copy(adjustSetDialog = dialog.copy(repRangeOrDuration = event.value))
            }
            AiCustomPlanPreviewEvent.ConfirmAdjustSet -> confirmAdjustSet()
            AiCustomPlanPreviewEvent.DismissAdjustSetDialog -> updateLoaded { it.copy(adjustSetDialog = null) }
        }
    }

    private fun updateLoaded(transform: (AiCustomPlanPreviewState.Loaded) -> AiCustomPlanPreviewState.Loaded) {
        _state.update { current -> if (current is AiCustomPlanPreviewState.Loaded) transform(current) else current }
    }

    private fun removeExercise(dayNumber: Int, exerciseId: String) {
        val draft = draftHolder.current ?: return
        val updatedDays = draft.days.map { day ->
            if (day.dayNumber != dayNumber) day else day.copy(mainExercises = day.mainExercises?.filterNot { it.exerciseId == exerciseId })
        }
        val updatedInjuryConflicts = draft.injuryConflicts.filterNot { it.dayNumber == dayNumber && it.exerciseId == exerciseId }
        draftHolder.current = draft.copy(days = updatedDays, injuryConflicts = updatedInjuryConflicts)
        updateLoaded { it.copy(days = updatedDays, injuryConflicts = updatedInjuryConflicts) }
    }

    private fun requestSwap(dayNumber: Int, exerciseId: String) {
        val current = _state.value as? AiCustomPlanPreviewState.Loaded ?: return
        val exercise = current.exercisesById[exerciseId] ?: return
        viewModelScope.launch {
            when (val result = getExerciseAlternatives(exercise)) {
                is AppResult.Success -> updateLoaded {
                    it.copy(swapDialog = SwapDialogState(dayNumber, exerciseId, result.data))
                }
                is AppResult.Error -> fail(result.failure.message)
            }
        }
    }

    private fun selectSwapCandidate(newExerciseId: String) {
        val current = _state.value as? AiCustomPlanPreviewState.Loaded ?: return
        val dialog = current.swapDialog ?: return
        val draft = draftHolder.current ?: return

        val updatedDays = draft.days.map { day ->
            if (day.dayNumber != dialog.dayNumber) {
                day
            } else {
                day.copy(
                    mainExercises = day.mainExercises?.map { plannedExercise ->
                        if (plannedExercise.exerciseId == dialog.exerciseId) {
                            plannedExercise.copy(exerciseId = newExerciseId, reasonRuleIds = plannedExercise.reasonRuleIds + "MANUAL_SWAP")
                        } else {
                            plannedExercise
                        }
                    },
                )
            }
        }
        val updatedInjuryConflicts = draft.injuryConflicts.filterNot { it.dayNumber == dialog.dayNumber && it.exerciseId == dialog.exerciseId }
        draftHolder.current = draft.copy(days = updatedDays, injuryConflicts = updatedInjuryConflicts)
        updateLoaded { it.copy(days = updatedDays, injuryConflicts = updatedInjuryConflicts, swapDialog = null) }
    }

    private fun requestAdjustSet(dayNumber: Int, exerciseId: String) {
        val draft = draftHolder.current ?: return
        val plannedExercise = draft.days.find { it.dayNumber == dayNumber }
            ?.mainExercises?.find { it.exerciseId == exerciseId } ?: return
        updateLoaded { it.copy(adjustSetDialog = AdjustSetDialogState(dayNumber, exerciseId, plannedExercise.sets, plannedExercise.repRangeOrDuration)) }
    }

    private fun confirmAdjustSet() {
        val current = _state.value as? AiCustomPlanPreviewState.Loaded ?: return
        val dialog = current.adjustSetDialog ?: return
        val draft = draftHolder.current ?: return

        val updatedDays = draft.days.map { day ->
            if (day.dayNumber != dialog.dayNumber) {
                day
            } else {
                day.copy(
                    mainExercises = day.mainExercises?.map { plannedExercise ->
                        if (plannedExercise.exerciseId == dialog.exerciseId) {
                            plannedExercise.copy(sets = dialog.sets, repRangeOrDuration = dialog.repRangeOrDuration)
                        } else {
                            plannedExercise
                        }
                    },
                )
            }
        }
        draftHolder.current = draft.copy(days = updatedDays)
        updateLoaded { it.copy(days = updatedDays, adjustSetDialog = null) }
    }

    private fun regenerate() {
        val draft = draftHolder.current ?: return
        viewModelScope.launch {
            _state.value = AiCustomPlanPreviewState.Regenerating
            val fullAssessment = when (val result = getLatestFullAssessmentResult()) {
                is AppResult.Success -> result.data
                is AppResult.Error -> return@launch fail(result.failure.message)
            } ?: return@launch fail("Full Assessment not found")
            val historySummary = when (val result = buildWorkoutHistorySummary()) {
                is AppResult.Success -> result.data
                is AppResult.Error -> return@launch fail(result.failure.message)
            }
            val generated = when (
                val result = generateAiPlan(AiPlanGenerationRequest(draft.formInput, fullAssessment, historySummary))
            ) {
                is AppResult.Success -> result.data
                is AppResult.Error -> return@launch fail(result.failure.message)
            }
            val validated = when (val result = validateAndFixExerciseIds(generated.days)) {
                is AppResult.Success -> result.data
                is AppResult.Error -> return@launch fail(result.failure.message)
            }
            val injuryConflicts = when (
                val result = checkInjuryConflicts(validated.days, activeFlags(fullAssessment), fullAssessment.injuryHistory)
            ) {
                is AppResult.Success -> result.data
                is AppResult.Error -> return@launch fail(result.failure.message)
            }

            draftHolder.current = draft.copy(
                smartGoalSummary = generated.smartGoalSummary,
                days = validated.days,
                serverNotes = generated.serverNotes,
                exerciseWarnings = validated.warnings,
                injuryConflicts = injuryConflicts,
            )
            loadExercisesThenShowDraft()
        }
    }

    private fun persist(activate: Boolean) {
        val draft = draftHolder.current ?: return
        viewModelScope.launch {
            when (val quota = checkDraftQuota()) {
                is AppResult.Error -> return@launch fail(quota.failure.message)
                is AppResult.Success -> if (quota.data.isFull) {
                    _state.value = AiCustomPlanPreviewState.QuotaFull(quota.data.existingAiPlans)
                    return@launch
                }
            }

            val fullAssessment = when (val result = getLatestFullAssessmentResult()) {
                is AppResult.Success -> result.data
                is AppResult.Error -> return@launch fail(result.failure.message)
            } ?: return@launch fail("Full Assessment not found")

            val plan = MonthlyPlan(
                id = generateId(),
                startedAt = currentInstant(),
                cycleNumber = 1,
                source = PlanSource.AI_GENERATED,
                status = if (activate) PlanStatus.ACTIVE else PlanStatus.DRAFT,
                levelMeta = AI_CUSTOM_LEVEL_META,
                goalMeta = draft.formInput.goal,
                smartGoalMeta = draft.smartGoalSummary,
                activeFlags = activeFlags(fullAssessment),
                startingLevelPerPattern = emptyMap(),
                mode = ProgressionMode.NORMAL,
                checkpointDays = emptyList(),
            )

            val result = if (activate) activateAiCustomPlan(plan, draft.days) else saveAiCustomPlanDraft(plan, draft.days)
            when (result) {
                is AppResult.Error -> fail(result.failure.message)
                is AppResult.Success -> {
                    draftHolder.current = null
                    _state.value = if (activate) AiCustomPlanPreviewState.Activated else AiCustomPlanPreviewState.Saved
                }
            }
        }
    }

    private fun deleteThenRetryPersist(planId: String) {
        viewModelScope.launch {
            when (val result = deletePlan(planId)) {
                is AppResult.Error -> fail(result.failure.message)
                is AppResult.Success -> persist(activate = false)
            }
        }
    }

    private fun activeFlags(fullAssessment: FullAssessmentResult) =
        fullAssessment.parQCategoryB + fullAssessment.flagsPostural + fullAssessment.movementFlags

    private fun PendingAiPlanDraft.toLoadedState(exercisesById: Map<String, Exercise>) = AiCustomPlanPreviewState.Loaded(
        smartGoalSummary = smartGoalSummary,
        sessionDurationMinutes = formInput.sessionDurationMinutes,
        days = days,
        exercisesById = exercisesById,
        conflicts = conflicts,
        serverNotes = serverNotes,
        exerciseWarnings = exerciseWarnings,
        injuryConflicts = injuryConflicts,
    )

    private fun fail(message: String) {
        _state.value = AiCustomPlanPreviewState.Error(message)
    }
}
