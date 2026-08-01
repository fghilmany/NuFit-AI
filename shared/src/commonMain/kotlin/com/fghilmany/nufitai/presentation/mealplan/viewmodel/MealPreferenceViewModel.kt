package com.fghilmany.nufitai.presentation.mealplan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.mealplan.entity.MealMood
import com.fghilmany.nufitai.domain.mealplan.entity.MealPreference
import com.fghilmany.nufitai.domain.mealplan.entity.MealScheduleMode
import com.fghilmany.nufitai.usecase.mealplan.GetMealPreference
import com.fghilmany.nufitai.usecase.mealplan.SaveMealPreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MealPreferenceState(
    val cuisineThemes: Set<String> = emptySet(),
    val mood: MealMood? = null,
    val allergies: List<String> = emptyList(),
    val allergyDraft: String = "",
    val scheduleMode: MealScheduleMode = MealScheduleMode.AI_RANDOM,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
)

/** P-17 B (AC-5). */
class MealPreferenceViewModel(
    private val getMealPreference: GetMealPreference,
    private val saveMealPreference: SaveMealPreference,
) : ViewModel() {
    private val _state = MutableStateFlow(MealPreferenceState())
    val state: StateFlow<MealPreferenceState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = getMealPreference()) {
                is AppResult.Error -> _state.update { it.copy(isLoading = false, error = result.failure.message) }
                is AppResult.Success -> {
                    val preference = result.data
                    _state.update {
                        it.copy(
                            isLoading = false,
                            cuisineThemes = preference?.cuisineThemes ?: emptySet(),
                            mood = preference?.mood,
                            allergies = preference?.allergies ?: emptyList(),
                            scheduleMode = preference?.scheduleMode ?: MealScheduleMode.AI_RANDOM,
                        )
                    }
                }
            }
        }
    }

    fun onToggleCuisineTheme(theme: String) {
        _state.update { it.copy(cuisineThemes = if (theme in it.cuisineThemes) it.cuisineThemes - theme else it.cuisineThemes + theme) }
    }

    fun onSetMood(mood: MealMood) = _state.update { it.copy(mood = mood) }

    fun onAllergyDraftChange(value: String) = _state.update { it.copy(allergyDraft = value) }

    fun onAddAllergy() {
        val value = _state.value.allergyDraft.trim()
        if (value.isEmpty()) return
        _state.update { it.copy(allergies = it.allergies + value, allergyDraft = "") }
    }

    fun onRemoveAllergy(value: String) = _state.update { it.copy(allergies = it.allergies - value) }

    fun onSetScheduleMode(mode: MealScheduleMode) = _state.update { it.copy(scheduleMode = mode) }

    fun onSave() {
        val current = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            val preference = MealPreference(
                cuisineThemes = current.cuisineThemes,
                mood = current.mood,
                allergies = current.allergies,
                scheduleMode = current.scheduleMode,
            )
            when (val result = saveMealPreference(preference)) {
                is AppResult.Error -> _state.update { it.copy(isSaving = false, error = result.failure.message) }
                is AppResult.Success -> _state.update { it.copy(isSaving = false, saved = true) }
            }
        }
    }
}
