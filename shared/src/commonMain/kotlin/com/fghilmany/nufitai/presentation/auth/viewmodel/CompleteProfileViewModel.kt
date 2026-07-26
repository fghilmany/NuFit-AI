package com.fghilmany.nufitai.presentation.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.fullassessment.entity.Gender
import com.fghilmany.nufitai.usecase.profile.SaveProfileBasics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** "Lengkapi Profil" (Keputusan #11, techspec §6 auth-sync.md) -- age/gender, once, post-first-login. */
sealed interface CompleteProfileState {
    data class Editing(val ageInput: String = "", val gender: Gender? = null, val error: String? = null) : CompleteProfileState
    data object Saving : CompleteProfileState
    data object Saved : CompleteProfileState
}

class CompleteProfileViewModel(private val saveProfileBasics: SaveProfileBasics) : ViewModel() {
    private val _state = MutableStateFlow<CompleteProfileState>(CompleteProfileState.Editing())
    val state: StateFlow<CompleteProfileState> = _state.asStateFlow()

    fun onAgeChanged(value: String) {
        _state.update { current -> (current as? CompleteProfileState.Editing)?.copy(ageInput = value, error = null) ?: current }
    }

    fun onGenderChanged(gender: Gender) {
        _state.update { current -> (current as? CompleteProfileState.Editing)?.copy(gender = gender, error = null) ?: current }
    }

    fun save() {
        val current = _state.value as? CompleteProfileState.Editing ?: return
        val age = current.ageInput.toIntOrNull()
        val gender = current.gender
        if (age == null || age <= 0 || gender == null) {
            _state.value = current.copy(error = "Please enter a valid age and select a gender")
            return
        }
        _state.value = CompleteProfileState.Saving
        viewModelScope.launch {
            when (val result = saveProfileBasics(age, gender)) {
                is AppResult.Error -> _state.value = current.copy(error = result.failure.message)
                is AppResult.Success -> _state.value = CompleteProfileState.Saved
            }
        }
    }
}
