package com.fghilmany.nufitai.presentation.monthlyplan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExercisePool
import com.fghilmany.nufitai.usecase.monthlyplan.GetActivePlanOverview
import com.fghilmany.nufitai.usecase.monthlyplan.GetSessionStatus
import com.fghilmany.nufitai.usecase.monthlyplan.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.monthlyplan_session_detail_not_found
import org.jetbrains.compose.resources.getString

sealed interface SessionDetailState {
    data object Loading : SessionDetailState
    data class Loaded(val planDay: PlanDay, val status: SessionStatus, val exercisesById: Map<String, Exercise>) : SessionDetailState
    data class Error(val message: String) : SessionDetailState
}

/**
 * issue #76 P-04 -- `actualResults` (target replaced by logged actuals once completed, per
 * issue #16's alternate flow) is NOT implemented: `PlanDaySessionLog` is session-level only
 * (completedAt/rpe/pain), it has no per-exercise actual weight/rep data to swap in -- that
 * needs PT Mode's per-set logging table (`05-pt-mode.md`, not built yet).
 */
class SessionDetailViewModel(
    private val getActivePlanOverview: GetActivePlanOverview,
    private val getSessionStatus: GetSessionStatus,
    private val getExercisePool: GetExercisePool,
) : ViewModel() {
    private val _state = MutableStateFlow<SessionDetailState>(SessionDetailState.Loading)
    val state: StateFlow<SessionDetailState> = _state.asStateFlow()

    fun load(planDayId: String) {
        viewModelScope.launch {
            val overviewResult = getActivePlanOverview()
            val overview = when (overviewResult) {
                is AppResult.Success -> overviewResult.data
                is AppResult.Error -> {
                    _state.value = SessionDetailState.Error(overviewResult.failure.message)
                    return@launch
                }
            }
            val planDay = overview?.days?.find { it.id == planDayId }
            if (overview == null || planDay == null) {
                _state.value = SessionDetailState.Error(getString(Res.string.monthlyplan_session_detail_not_found))
                return@launch
            }

            val poolResult = getExercisePool()
            val exercisesById = when (poolResult) {
                is AppResult.Success -> poolResult.data.associateBy { it.id }
                is AppResult.Error -> {
                    _state.value = SessionDetailState.Error(poolResult.failure.message)
                    return@launch
                }
            }

            val log = overview.logs.find { it.planDayId == planDayId }
            val status = getSessionStatus(planDay, log, overview.plan.startedAt, currentInstant())
            _state.value = SessionDetailState.Loaded(planDay, status, exercisesById)
        }
    }
}
