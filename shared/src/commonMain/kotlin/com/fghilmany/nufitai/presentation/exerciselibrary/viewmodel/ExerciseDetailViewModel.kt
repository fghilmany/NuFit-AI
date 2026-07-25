package com.fghilmany.nufitai.presentation.exerciselibrary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExerciseAlternatives
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExerciseDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.exercisedetail_not_found
import org.jetbrains.compose.resources.getString

sealed interface ExerciseDetailState {
    data object Loading : ExerciseDetailState
    data class Loaded(val exercise: Exercise, val alternatives: List<Exercise>) : ExerciseDetailState
    data class Error(val message: String) : ExerciseDetailState
}

/** P-08 (issue #79). Mirrors SessionDetailViewModel's `load(id)` pattern. */
class ExerciseDetailViewModel(
    private val getExerciseDetail: GetExerciseDetail,
    private val getExerciseAlternatives: GetExerciseAlternatives,
) : ViewModel() {
    private val _state = MutableStateFlow<ExerciseDetailState>(ExerciseDetailState.Loading)
    val state: StateFlow<ExerciseDetailState> = _state.asStateFlow()

    fun load(exerciseId: String) {
        viewModelScope.launch {
            val exercise = when (val result = getExerciseDetail(exerciseId)) {
                is AppResult.Success -> result.data ?: run {
                    _state.value = ExerciseDetailState.Error(getString(Res.string.exercisedetail_not_found))
                    return@launch
                }
                is AppResult.Error -> {
                    _state.value = ExerciseDetailState.Error(result.failure.message)
                    return@launch
                }
            }

            when (val altResult = getExerciseAlternatives(exercise)) {
                is AppResult.Success -> _state.value = ExerciseDetailState.Loaded(exercise, altResult.data)
                is AppResult.Error -> _state.value = ExerciseDetailState.Error(altResult.failure.message)
            }
        }
    }
}
