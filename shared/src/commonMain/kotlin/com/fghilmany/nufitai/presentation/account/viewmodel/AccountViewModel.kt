package com.fghilmany.nufitai.presentation.account.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.entitlement.entity.Tier
import com.fghilmany.nufitai.domain.sync.entity.SyncStatus
import com.fghilmany.nufitai.usecase.entitlement.GetCurrentTier
import com.fghilmany.nufitai.usecase.auth.SignOut
import com.fghilmany.nufitai.usecase.sync.ObserveSyncStatus
import com.fghilmany.nufitai.usecase.sync.SyncNow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** P-15 stub (techspec §3/§6, auth-sync.md) -- minimal Account screen; full scope stays in 09-profile-settings.md. */
sealed interface AccountState {
    data object Loading : AccountState
    data object Guest : AccountState
    data class LoggedIn(val syncStatus: SyncStatus) : AccountState
    data class Error(val message: String) : AccountState
}

class AccountViewModel(
    private val getCurrentTier: GetCurrentTier,
    private val signOut: SignOut,
    private val syncNow: SyncNow,
    private val observeSyncStatus: ObserveSyncStatus,
) : ViewModel() {
    private val _state = MutableStateFlow<AccountState>(AccountState.Loading)
    val state: StateFlow<AccountState> = _state.asStateFlow()

    init {
        resolve()
    }

    fun signOutNow() {
        viewModelScope.launch {
            signOut()
            resolve()
        }
    }

    fun syncNowManually() {
        viewModelScope.launch {
            syncNow()
        }
    }

    private fun resolve() {
        viewModelScope.launch {
            when (val tierResult = getCurrentTier()) {
                is AppResult.Error -> _state.value = AccountState.Error(tierResult.failure.message)
                is AppResult.Success -> when (tierResult.data) {
                    Tier.GUEST -> _state.value = AccountState.Guest
                    Tier.LOGGED_IN, Tier.SUBSCRIBED -> observeLoggedInSyncStatus()
                }
            }
        }
    }

    private suspend fun observeLoggedInSyncStatus() {
        val statusFlow = observeSyncStatus()
        _state.value = AccountState.LoggedIn(statusFlow.value)
        viewModelScope.launch {
            statusFlow.collect { status -> _state.value = AccountState.LoggedIn(status) }
        }
    }
}
