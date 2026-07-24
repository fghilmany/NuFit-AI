package com.fghilmany.nufitai.presentation.monthlyplan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay
import com.fghilmany.nufitai.usecase.monthlyplan.GetActivePlanOverview
import com.fghilmany.nufitai.usecase.monthlyplan.GetSessionStatus
import com.fghilmany.nufitai.usecase.monthlyplan.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeekEntry(val weekNumber: Int, val days: List<PlanDay>, val statuses: Map<String, SessionStatus>)

sealed interface HomeState {
    data object Loading : HomeState
    data class Loaded(
        val currentWeekNumber: Int,
        val progressPercent: Int,
        val weeks: List<WeekEntry>,
        val todaySessionId: String?,
        /** Which OTHER (non-current) week's accordion is expanded, if any -- current week is always shown. */
        val expandedWeek: Int?,
        val weekCompleteMessage: String?,
    ) : HomeState
    data object Empty : HomeState
    data class Error(val message: String) : HomeState
}

sealed interface HomeEvent {
    data class ToggleWeekExpanded(val weekNumber: Int) : HomeEvent
}

/** issue #76 P-03 -- AC-1: session status is derived (`GetSessionStatus`), never stored. */
class HomeViewModel(
    private val getActivePlanOverview: GetActivePlanOverview,
    private val getSessionStatus: GetSessionStatus,
) : ViewModel() {
    private val _state = MutableStateFlow<HomeState>(HomeState.Loading)
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        load()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.ToggleWeekExpanded -> _state.update { current ->
                if (current is HomeState.Loaded) {
                    current.copy(expandedWeek = if (current.expandedWeek == event.weekNumber) null else event.weekNumber)
                } else {
                    current
                }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            when (val result = getActivePlanOverview()) {
                is AppResult.Success -> {
                    val overview = result.data
                    _state.value = if (overview == null) {
                        HomeState.Empty
                    } else {
                        val now = currentInstant()
                        val statuses = overview.days.associate { day ->
                            val log = overview.logs.find { it.planDayId == day.id }
                            day.id to getSessionStatus(day, log, overview.plan.startedAt, now)
                        }
                        val weeks = overview.days
                            .groupBy { weekNumberFor(it.dayNumber) }
                            .toSortedMap()
                            .map { (weekNumber, days) -> WeekEntry(weekNumber, days.sortedBy { it.dayNumber }, statuses) }
                        val todayDay = overview.days.find { statuses[it.id] == SessionStatus.TODAY }
                        val currentWeek = todayDay?.let { weekNumberFor(it.dayNumber) } ?: weeks.firstOrNull()?.weekNumber ?: 1
                        val totalSessions = overview.days.count { it.mainExercises != null }
                        val doneSessions = statuses.values.count { it == SessionStatus.DONE }
                        val progressPercent = if (totalSessions == 0) 0 else (doneSessions * 100) / totalSessions
                        val currentWeekDone = weeks.find { it.weekNumber == currentWeek }
                            ?.days.orEmpty()
                            .filter { it.mainExercises != null }
                            .all { statuses[it.id] == SessionStatus.DONE || statuses[it.id] == SessionStatus.SKIPPED }

                        HomeState.Loaded(
                            currentWeekNumber = currentWeek,
                            progressPercent = progressPercent,
                            weeks = weeks,
                            todaySessionId = todayDay?.id,
                            expandedWeek = null,
                            weekCompleteMessage = if (currentWeekDone) "Minggu ini selesai, kerja bagus!" else null,
                        )
                    }
                }
                is AppResult.Error -> _state.value = HomeState.Error(result.failure.message)
            }
        }
    }
}

private fun weekNumberFor(dayNumber: Int): Int = ((dayNumber - 1) / 7) + 1
