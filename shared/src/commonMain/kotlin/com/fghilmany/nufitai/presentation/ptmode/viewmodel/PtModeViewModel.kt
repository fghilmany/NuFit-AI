package com.fghilmany.nufitai.presentation.ptmode.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.core.util.generateId
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlannedExercise
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutExerciseLog
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSession
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSessionStatus
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExerciseAlternatives
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExercisePool
import com.fghilmany.nufitai.usecase.monthlyplan.GetActivePlanOverview
import com.fghilmany.nufitai.usecase.ptmode.ConfirmSet
import com.fghilmany.nufitai.usecase.ptmode.EndSessionEarly
import com.fghilmany.nufitai.usecase.ptmode.GetLatestLoggedSet
import com.fghilmany.nufitai.usecase.ptmode.GetOrCreateExerciseLog
import com.fghilmany.nufitai.usecase.ptmode.GetOrCreateWorkoutSession
import com.fghilmany.nufitai.usecase.ptmode.SkipExercise
import com.fghilmany.nufitai.usecase.ptmode.SkipRestTimer
import com.fghilmany.nufitai.usecase.ptmode.SwapExercise
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.ptmode_error_exercise_not_found
import nufitai.shared.generated.resources.ptmode_error_session_not_found
import org.jetbrains.compose.resources.getString

private const val DEFAULT_REST_SECONDS = 90
private const val DEFAULT_REPS_FALLBACK = 10
private const val WEIGHT_STEP_KG = 2.5
private const val REPS_STEP = 1

sealed interface PtModeState {
    data object Loading : PtModeState
    data class Active(
        val session: WorkoutSession,
        val totalExerciseCount: Int,
        val currentPlanned: PlannedExercise,
        val currentExercise: Exercise,
        val currentExerciseLog: WorkoutExerciseLog,
        val weightKg: Double,
        val reps: Int,
        val restRemainingSeconds: Int?,
        val swapSheetVisible: Boolean,
        val swapCandidates: List<Exercise>,
    ) : PtModeState
    data class Completed(val planDayId: String) : PtModeState
    data class Error(val message: String) : PtModeState
}

sealed interface PtModeEvent {
    data class AdjustWeight(val deltaKg: Double) : PtModeEvent
    data class AdjustReps(val delta: Int) : PtModeEvent
    data object ConfirmSet : PtModeEvent
    data object SkipRest : PtModeEvent
    data object SkipExercise : PtModeEvent
    data object OpenSwapSheet : PtModeEvent
    data object CloseSwapSheet : PtModeEvent
    data class SelectSwapCandidate(val exerciseId: String) : PtModeEvent
    data object EndSessionNow : PtModeEvent
}

/**
 * issue #80 P-05. mainExercises-only (warmup/cooldown out of scope, matches
 * PlanDay.exerciseCount()). Rest timer is wall-clock-anchored (WorkoutSession.restEndAt) so
 * it survives backgrounding without drift (AC-1, DoD).
 */
class PtModeViewModel(
    private val getOrCreateWorkoutSession: GetOrCreateWorkoutSession,
    private val getOrCreateExerciseLog: GetOrCreateExerciseLog,
    private val confirmSetUseCase: ConfirmSet,
    private val skipRestTimerUseCase: SkipRestTimer,
    private val skipExerciseUseCase: SkipExercise,
    private val swapExerciseUseCase: SwapExercise,
    private val endSessionEarlyUseCase: EndSessionEarly,
    private val getLatestLoggedSet: GetLatestLoggedSet,
    private val getActivePlanOverview: GetActivePlanOverview,
    private val getExercisePool: GetExercisePool,
    private val getExerciseAlternatives: GetExerciseAlternatives,
) : ViewModel() {
    private val _state = MutableStateFlow<PtModeState>(PtModeState.Loading)
    val state: StateFlow<PtModeState> = _state.asStateFlow()

    private var mainExercises: List<PlannedExercise> = emptyList()
    private var exercisesById: Map<String, Exercise> = emptyMap()
    private var restTickerJob: Job? = null

    fun load(planDayId: String) {
        viewModelScope.launch {
            val overview = when (val result = getActivePlanOverview()) {
                is AppResult.Error -> return@launch fail(result.failure.message)
                is AppResult.Success -> result.data
            }
            val day = overview?.days?.find { it.id == planDayId }
            val exercises = day?.mainExercises
            if (day == null || exercises.isNullOrEmpty()) {
                return@launch fail(getString(Res.string.ptmode_error_session_not_found))
            }
            mainExercises = exercises

            val pool = when (val result = getExercisePool()) {
                is AppResult.Error -> return@launch fail(result.failure.message)
                is AppResult.Success -> result.data
            }
            exercisesById = pool.associateBy { it.id }

            val session = when (val result = getOrCreateWorkoutSession(planDayId)) {
                is AppResult.Error -> return@launch fail(result.failure.message)
                is AppResult.Success -> result.data
            }

            enterExercise(session)
        }
    }

    fun onEvent(event: PtModeEvent) {
        val current = _state.value as? PtModeState.Active ?: return
        when (event) {
            is PtModeEvent.AdjustWeight -> _state.value = current.copy(weightKg = (current.weightKg + event.deltaKg).coerceAtLeast(0.0))
            is PtModeEvent.AdjustReps -> _state.value = current.copy(reps = (current.reps + event.delta).coerceAtLeast(0))
            PtModeEvent.ConfirmSet -> onConfirmSet(current)
            PtModeEvent.SkipRest -> onSkipRest(current)
            PtModeEvent.SkipExercise -> onSkipExercise(current)
            PtModeEvent.OpenSwapSheet -> onOpenSwapSheet(current)
            PtModeEvent.CloseSwapSheet -> _state.value = current.copy(swapSheetVisible = false, swapCandidates = emptyList())
            is PtModeEvent.SelectSwapCandidate -> onSelectSwapCandidate(current, event.exerciseId)
            PtModeEvent.EndSessionNow -> onEndSessionNow(current)
        }
    }

    private suspend fun enterExercise(session: WorkoutSession) {
        if (session.currentExerciseIndex >= mainExercises.size) {
            // Defensive only -- ConfirmSet/SkipExercise finalize the session themselves on the last exercise.
            _state.value = PtModeState.Completed(session.planDayId)
            return
        }
        val planned = mainExercises[session.currentExerciseIndex]

        val exerciseLog = when (val result = getOrCreateExerciseLog(session, session.currentExerciseIndex, planned.exerciseId)) {
            is AppResult.Error -> return fail(result.failure.message)
            is AppResult.Success -> result.data
        }
        val exercise = exercisesById[exerciseLog.exerciseId] ?: return fail(getString(Res.string.ptmode_error_exercise_not_found))

        val (defaultWeight, defaultReps) = resolveDefaults(exerciseLog.exerciseId, planned)

        _state.value = PtModeState.Active(
            session = session,
            totalExerciseCount = mainExercises.size,
            currentPlanned = planned,
            currentExercise = exercise,
            currentExerciseLog = exerciseLog,
            weightKg = defaultWeight,
            reps = defaultReps,
            restRemainingSeconds = session.restEndAt?.let { remainingSecondsFrom(it) },
            swapSheetVisible = false,
            swapCandidates = emptyList(),
        )
        startRestTickerIfNeeded(session)
    }

    private suspend fun resolveDefaults(exerciseId: String, planned: PlannedExercise): Pair<Double, Int> {
        val latest = when (val result = getLatestLoggedSet(exerciseId)) {
            is AppResult.Success -> result.data
            is AppResult.Error -> null
        }
        if (latest != null) return latest.weightKg to latest.reps
        val targetReps = planned.repRangeOrDuration.takeWhile { it.isDigit() }.toIntOrNull() ?: DEFAULT_REPS_FALLBACK
        return 0.0 to targetReps
    }

    private fun onConfirmSet(current: PtModeState.Active) {
        viewModelScope.launch {
            val isLast = current.session.currentExerciseIndex == mainExercises.size - 1
            val restSeconds = current.currentPlanned.restSeconds ?: DEFAULT_REST_SECONDS
            when (
                val result = confirmSetUseCase(
                    session = current.session,
                    exerciseLog = current.currentExerciseLog,
                    weightKg = current.weightKg,
                    reps = current.reps,
                    totalSetsForExercise = current.currentPlanned.sets,
                    restSeconds = restSeconds,
                    isLastExercise = isLast,
                )
            ) {
                is AppResult.Error -> fail(result.failure.message)
                is AppResult.Success -> {
                    val updated = result.data
                    when {
                        updated.status == WorkoutSessionStatus.COMPLETED -> _state.value = PtModeState.Completed(updated.planDayId)
                        updated.currentExerciseIndex != current.session.currentExerciseIndex -> enterExercise(updated)
                        else -> {
                            _state.value = current.copy(session = updated, restRemainingSeconds = restSeconds)
                            startRestTickerIfNeeded(updated)
                        }
                    }
                }
            }
        }
    }

    private fun onSkipRest(current: PtModeState.Active) {
        viewModelScope.launch {
            restTickerJob?.cancel()
            when (val result = skipRestTimerUseCase(current.session)) {
                is AppResult.Error -> fail(result.failure.message)
                is AppResult.Success -> _state.value = current.copy(session = result.data, restRemainingSeconds = null)
            }
        }
    }

    private fun onSkipExercise(current: PtModeState.Active) {
        viewModelScope.launch {
            val isLast = current.session.currentExerciseIndex == mainExercises.size - 1
            when (val result = skipExerciseUseCase(current.session, current.currentExerciseLog, isLastExercise = isLast)) {
                is AppResult.Error -> fail(result.failure.message)
                is AppResult.Success -> {
                    val updated = result.data
                    if (updated.status == WorkoutSessionStatus.COMPLETED) {
                        _state.value = PtModeState.Completed(updated.planDayId)
                    } else {
                        enterExercise(updated)
                    }
                }
            }
        }
    }

    private fun onOpenSwapSheet(current: PtModeState.Active) {
        viewModelScope.launch {
            when (val result = getExerciseAlternatives(current.currentExercise)) {
                is AppResult.Error -> fail(result.failure.message)
                is AppResult.Success -> _state.value = current.copy(swapSheetVisible = true, swapCandidates = result.data)
            }
        }
    }

    private fun onSelectSwapCandidate(current: PtModeState.Active, exerciseId: String) {
        viewModelScope.launch {
            when (val result = swapExerciseUseCase(current.currentExerciseLog, exerciseId)) {
                is AppResult.Error -> fail(result.failure.message)
                is AppResult.Success -> {
                    val newExercise = exercisesById[exerciseId] ?: return@launch fail(getString(Res.string.ptmode_error_exercise_not_found))
                    val (defaultWeight, defaultReps) = resolveDefaults(exerciseId, current.currentPlanned)
                    _state.value = current.copy(
                        currentExerciseLog = result.data,
                        currentExercise = newExercise,
                        weightKg = defaultWeight,
                        reps = defaultReps,
                        swapSheetVisible = false,
                        swapCandidates = emptyList(),
                    )
                }
            }
        }
    }

    private fun onEndSessionNow(current: PtModeState.Active) {
        viewModelScope.launch {
            // Slots before currentExerciseIndex are already resolved (finished or explicitly
            // skipped). The current slot reuses its existing log (any sets already logged this
            // exercise stay attached, decision #7); everything after it was never touched, so
            // fresh not-yet-persisted logs are built for those.
            val futureSlots = (current.session.currentExerciseIndex + 1 until mainExercises.size).map { index ->
                WorkoutExerciseLog(
                    id = generateId(),
                    workoutSessionId = current.session.id,
                    plannedExerciseIndex = index,
                    exerciseId = mainExercises[index].exerciseId,
                    skippedAt = null,
                )
            }
            val logsToClose = listOf(current.currentExerciseLog) + futureSlots
            when (val result = endSessionEarlyUseCase(current.session, logsToClose)) {
                is AppResult.Error -> fail(result.failure.message)
                is AppResult.Success -> _state.value = PtModeState.Completed(result.data.planDayId)
            }
        }
    }

    private fun startRestTickerIfNeeded(session: WorkoutSession) {
        restTickerJob?.cancel()
        val restEndAt = session.restEndAt ?: return
        restTickerJob = viewModelScope.launch {
            while (true) {
                val remaining = remainingSecondsFrom(restEndAt)
                if (remaining <= 0) {
                    val active = _state.value as? PtModeState.Active ?: return@launch
                    when (val result = skipRestTimerUseCase(active.session)) {
                        is AppResult.Error -> fail(result.failure.message)
                        is AppResult.Success -> _state.value = active.copy(session = result.data, restRemainingSeconds = null)
                    }
                    return@launch
                }
                val active = _state.value as? PtModeState.Active ?: return@launch
                _state.value = active.copy(restRemainingSeconds = remaining)
                delay(1000)
            }
        }
    }

    private fun remainingSecondsFrom(restEndAt: kotlin.time.Instant): Int =
        (restEndAt - currentInstant()).inWholeSeconds.toInt().coerceAtLeast(0)

    private fun fail(message: String) {
        _state.value = PtModeState.Error(message)
    }
}
