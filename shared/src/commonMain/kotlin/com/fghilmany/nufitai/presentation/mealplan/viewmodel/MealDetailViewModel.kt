package com.fghilmany.nufitai.presentation.mealplan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.mealplan.entity.MealLogStatus
import com.fghilmany.nufitai.domain.mealplan.entity.PlannedMeal
import com.fghilmany.nufitai.usecase.mealplan.GetMealLogForPlannedMeal
import com.fghilmany.nufitai.usecase.mealplan.GetPlannedMealById
import com.fghilmany.nufitai.usecase.mealplan.LogMealStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MealDetailState {
    data object Loading : MealDetailState
    data class Loaded(val plannedMeal: PlannedMeal, val status: MealLogStatus) : MealDetailState
    data class Error(val message: String) : MealDetailState
}

/** AC-7, BLOCKING. Mirrors `ExerciseDetailViewModel`'s `load(id)` pattern. */
class MealDetailViewModel(
    private val getPlannedMealById: GetPlannedMealById,
    private val getMealLogForPlannedMeal: GetMealLogForPlannedMeal,
    private val logMealStatus: LogMealStatus,
) : ViewModel() {
    private val _state = MutableStateFlow<MealDetailState>(MealDetailState.Loading)
    val state: StateFlow<MealDetailState> = _state.asStateFlow()

    fun load(plannedMealId: String) {
        viewModelScope.launch {
            val meal = when (val result = getPlannedMealById(plannedMealId)) {
                is AppResult.Error -> { _state.value = MealDetailState.Error(result.failure.message); return@launch }
                is AppResult.Success -> result.data ?: run { _state.value = MealDetailState.Error("Menu tidak ditemukan"); return@launch }
            }
            val status = when (val result = getMealLogForPlannedMeal(plannedMealId)) {
                is AppResult.Error -> { _state.value = MealDetailState.Error(result.failure.message); return@launch }
                is AppResult.Success -> result.data?.status ?: MealLogStatus.PENDING
            }
            _state.value = MealDetailState.Loaded(meal, status)
        }
    }

    /** Local-first commit -- never fails silently offline (§8). */
    fun onSetStatus(status: MealLogStatus) {
        val current = _state.value
        if (current !is MealDetailState.Loaded) return
        viewModelScope.launch {
            when (val result = logMealStatus(current.plannedMeal.id, status)) {
                is AppResult.Error -> _state.value = MealDetailState.Error(result.failure.message)
                is AppResult.Success -> _state.value = current.copy(status = status)
            }
        }
    }
}
