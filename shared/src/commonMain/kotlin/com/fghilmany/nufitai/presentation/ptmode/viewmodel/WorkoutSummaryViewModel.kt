package com.fghilmany.nufitai.presentation.ptmode.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSession
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSummary
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExercisePool
import com.fghilmany.nufitai.usecase.monthlyplan.GetActivePlanOverview
import com.fghilmany.nufitai.usecase.ptmode.GetLatestSessionForDay
import com.fghilmany.nufitai.usecase.ptmode.GetWorkoutSummary
import com.fghilmany.nufitai.usecase.ptmode.SaveWorkoutSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.ptmode_error_session_not_found
import org.jetbrains.compose.resources.getString

sealed interface WorkoutSummaryState {
    data object Loading : WorkoutSummaryState
    data class Loaded(val summary: WorkoutSummary, val selectedRpe: Int?) : WorkoutSummaryState
    data object Saved : WorkoutSummaryState
    data class Error(val message: String) : WorkoutSummaryState
}

sealed interface WorkoutSummaryEvent {
    data class SelectRpe(val value: Int) : WorkoutSummaryEvent
    data object Save : WorkoutSummaryEvent
}

/**
 * issue #80 P-06. AC-9: the session is saved (PlanDaySessionLog.completedAt) as soon as this
 * loads, regardless of whether the user ever picks an RPE or presses "Simpan" -- "Tutup"
 * must not lose the session. "Simpan" only adds the RPE on top via the same idempotent id.
 */
class WorkoutSummaryViewModel(
    private val getLatestSessionForDay: GetLatestSessionForDay,
    private val getWorkoutSummary: GetWorkoutSummary,
    private val saveWorkoutSummary: SaveWorkoutSummary,
    private val getActivePlanOverview: GetActivePlanOverview,
    private val getExercisePool: GetExercisePool,
) : ViewModel() {
    private val _state = MutableStateFlow<WorkoutSummaryState>(WorkoutSummaryState.Loading)
    val state: StateFlow<WorkoutSummaryState> = _state.asStateFlow()

    private var session: WorkoutSession? = null

    fun load(planDayId: String) {
        viewModelScope.launch {
            val overview = when (val result = getActivePlanOverview()) {
                is AppResult.Error -> return@launch fail(result.failure.message)
                is AppResult.Success -> result.data
            }
            val totalExerciseCount = overview?.days?.find { it.id == planDayId }?.mainExercises?.size ?: 0

            val pool = when (val result = getExercisePool()) {
                is AppResult.Error -> return@launch fail(result.failure.message)
                is AppResult.Success -> result.data
            }
            val exercisesById = pool.associateBy { it.id }

            val fetchedSession = when (val result = getLatestSessionForDay(planDayId)) {
                is AppResult.Error -> return@launch fail(result.failure.message)
                is AppResult.Success -> result.data
            }
            if (fetchedSession == null) return@launch fail(getString(Res.string.ptmode_error_session_not_found))
            session = fetchedSession

            when (val saveResult = saveWorkoutSummary(fetchedSession, rpeReported = null)) {
                is AppResult.Error -> return@launch fail(saveResult.failure.message)
                is AppResult.Success -> Unit
            }

            when (val result = getWorkoutSummary(fetchedSession, exercisesById, totalExerciseCount)) {
                is AppResult.Error -> fail(result.failure.message)
                is AppResult.Success -> _state.value = WorkoutSummaryState.Loaded(result.data, selectedRpe = null)
            }
        }
    }

    fun onEvent(event: WorkoutSummaryEvent) {
        val current = _state.value as? WorkoutSummaryState.Loaded ?: return
        when (event) {
            is WorkoutSummaryEvent.SelectRpe -> _state.value = current.copy(selectedRpe = event.value)
            WorkoutSummaryEvent.Save -> onSave(current)
        }
    }

    private fun onSave(current: WorkoutSummaryState.Loaded) {
        val activeSession = session ?: return
        viewModelScope.launch {
            when (val result = saveWorkoutSummary(activeSession, current.selectedRpe)) {
                is AppResult.Error -> fail(result.failure.message)
                is AppResult.Success -> _state.value = WorkoutSummaryState.Saved
            }
        }
    }

    private fun fail(message: String) {
        _state.value = WorkoutSummaryState.Error(message)
    }
}
