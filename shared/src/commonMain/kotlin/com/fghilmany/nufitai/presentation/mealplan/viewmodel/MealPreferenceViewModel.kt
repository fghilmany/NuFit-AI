package com.fghilmany.nufitai.presentation.mealplan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.designsystem.snackbar.SnackbarController
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.mealplan.entity.MealMood
import com.fghilmany.nufitai.domain.mealplan.entity.MealPreference
import com.fghilmany.nufitai.domain.mealplan.entity.MealScheduleMode
import com.fghilmany.nufitai.presentation.mealplan.component.CUISINE_THEME_FREE
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
    val notes: String = "",
    val scheduleMode: MealScheduleMode = MealScheduleMode.AI_RANDOM,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
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
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    SnackbarController.showError(result.failure.message)
                }
                is AppResult.Success -> {
                    val preference = result.data
                    _state.update {
                        it.copy(
                            isLoading = false,
                            cuisineThemes = preference?.cuisineThemes ?: emptySet(),
                            mood = preference?.mood,
                            allergies = preference?.allergies ?: emptyList(),
                            notes = preference?.notes ?: "",
                            scheduleMode = preference?.scheduleMode ?: MealScheduleMode.AI_RANDOM,
                        )
                    }
                }
            }
        }
    }

    /** "Bebas" (no preference) is mutually exclusive with every named theme -- picking one clears the other. */
    fun onToggleCuisineTheme(theme: String) {
        _state.update {
            when {
                theme == CUISINE_THEME_FREE -> it.copy(cuisineThemes = if (theme in it.cuisineThemes) emptySet() else setOf(theme))
                theme in it.cuisineThemes -> it.copy(cuisineThemes = it.cuisineThemes - theme)
                else -> it.copy(cuisineThemes = (it.cuisineThemes - CUISINE_THEME_FREE) + theme)
            }
        }
    }

    fun onSetMood(mood: MealMood) = _state.update { it.copy(mood = mood) }

    fun onAllergyDraftChange(value: String) = _state.update { it.copy(allergyDraft = value) }

    fun onAddAllergy() {
        val value = _state.value.allergyDraft.trim()
        if (value.isEmpty()) return
        _state.update { it.copy(allergies = it.allergies + value, allergyDraft = "") }
    }

    fun onRemoveAllergy(value: String) = _state.update { it.copy(allergies = it.allergies - value) }

    fun onNotesChange(value: String) = _state.update { it.copy(notes = value) }

    fun onSave() {
        val current = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val preference = MealPreference(
                cuisineThemes = current.cuisineThemes,
                mood = current.mood,
                allergies = current.allergies,
                notes = current.notes.trim().ifBlank { null },
                scheduleMode = current.scheduleMode,
            )
            when (val result = saveMealPreference(preference)) {
                is AppResult.Error -> {
                    _state.update { it.copy(isSaving = false) }
                    SnackbarController.showError(result.failure.message)
                }
                is AppResult.Success -> _state.update { it.copy(isSaving = false, saved = true) }
            }
        }
    }
}
