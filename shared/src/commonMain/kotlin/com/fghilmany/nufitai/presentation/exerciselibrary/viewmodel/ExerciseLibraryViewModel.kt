package com.fghilmany.nufitai.presentation.exerciselibrary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MuscleGroup
import com.fghilmany.nufitai.usecase.exerciselibrary.ExerciseFilter
import com.fghilmany.nufitai.usecase.exerciselibrary.FilterExercises
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExercisePool
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ExerciseLibraryState {
    data object Loading : ExerciseLibraryState
    data class Loaded(val exercises: List<Exercise>, val query: String, val filter: ExerciseFilter) : ExerciseLibraryState
    data class Empty(val query: String, val filter: ExerciseFilter, val hasActiveFilters: Boolean) : ExerciseLibraryState
    data class Error(val message: String) : ExerciseLibraryState
}

sealed interface ExerciseLibraryEvent {
    data class SearchQueryChanged(val query: String) : ExerciseLibraryEvent
    data class ToggleMuscleGroup(val value: MuscleGroup) : ExerciseLibraryEvent
    data class ToggleEquipment(val value: EquipmentCategory) : ExerciseLibraryEvent
    data class ToggleLevel(val value: ExerciseLevel) : ExerciseLibraryEvent
    data object ClearFilters : ExerciseLibraryEvent
}

/** P-07 (issue #79). */
class ExerciseLibraryViewModel(
    private val getExercisePool: GetExercisePool,
    private val filterExercises: FilterExercises,
) : ViewModel() {
    private val _state = MutableStateFlow<ExerciseLibraryState>(ExerciseLibraryState.Loading)
    val state: StateFlow<ExerciseLibraryState> = _state.asStateFlow()

    /** Full unfiltered pool -- fetched once, re-filtered in-memory on every search/filter change (AC-1). */
    private var pool: List<Exercise> = emptyList()
    private var query: String = ""
    private var filter: ExerciseFilter = ExerciseFilter()

    init {
        load()
    }

    fun onEvent(event: ExerciseLibraryEvent) {
        when (event) {
            is ExerciseLibraryEvent.SearchQueryChanged -> {
                query = event.query
                applyFilters()
            }
            is ExerciseLibraryEvent.ToggleMuscleGroup -> {
                filter = filter.copy(muscleGroups = filter.muscleGroups.toggled(event.value))
                applyFilters()
            }
            is ExerciseLibraryEvent.ToggleEquipment -> {
                filter = filter.copy(equipment = filter.equipment.toggled(event.value))
                applyFilters()
            }
            is ExerciseLibraryEvent.ToggleLevel -> {
                filter = filter.copy(levels = filter.levels.toggled(event.value))
                applyFilters()
            }
            ExerciseLibraryEvent.ClearFilters -> {
                query = ""
                filter = ExerciseFilter()
                applyFilters()
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            when (val result = getExercisePool()) {
                is AppResult.Success -> {
                    pool = result.data
                    applyFilters()
                }
                is AppResult.Error -> _state.value = ExerciseLibraryState.Error(result.failure.message)
            }
        }
    }

    private fun applyFilters() {
        val visible = filterExercises(pool, query, filter)
        val hasActiveFilters = query.isNotBlank() || filter != ExerciseFilter()
        _state.value = if (visible.isEmpty()) {
            ExerciseLibraryState.Empty(query, filter, hasActiveFilters)
        } else {
            ExerciseLibraryState.Loaded(visible, query, filter)
        }
    }
}

private fun <T> Set<T>.toggled(value: T): Set<T> = if (value in this) this - value else this + value
