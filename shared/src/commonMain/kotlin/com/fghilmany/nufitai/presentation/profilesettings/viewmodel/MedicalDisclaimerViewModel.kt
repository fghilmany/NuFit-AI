package com.fghilmany.nufitai.presentation.profilesettings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.usecase.profilesettings.MarkMedicalDisclaimerSeen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * techspec profile-settings.md §6 -- shared by both the blocking first-run screen (Splash routes
 * here when `!hasSeenMedicalDisclaimer`) and the always-non-blocking Informasi entry. The screen
 * itself decides whether "acknowledge" is required to proceed (first-run) or just a read-only
 * view (re-read) -- this ViewModel only owns the acknowledge checkbox + the "mark seen" write.
 */
sealed interface MedicalDisclaimerState {
    data class Content(val acknowledged: Boolean = false) : MedicalDisclaimerState
    data object Acknowledged : MedicalDisclaimerState
}

class MedicalDisclaimerViewModel(private val markMedicalDisclaimerSeen: MarkMedicalDisclaimerSeen) : ViewModel() {
    private val _state = MutableStateFlow<MedicalDisclaimerState>(MedicalDisclaimerState.Content())
    val state: StateFlow<MedicalDisclaimerState> = _state.asStateFlow()

    fun toggleAcknowledge() {
        _state.update { current -> if (current is MedicalDisclaimerState.Content) current.copy(acknowledged = !current.acknowledged) else current }
    }

    fun continueClicked() {
        viewModelScope.launch {
            markMedicalDisclaimerSeen()
            _state.value = MedicalDisclaimerState.Acknowledged
        }
    }
}
