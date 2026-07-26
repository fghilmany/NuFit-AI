package com.fghilmany.nufitai.presentation.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.auth.AppleSignInLauncher
import com.fghilmany.nufitai.core.auth.GoogleSignInLauncher
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.auth.entity.AuthProvider
import com.fghilmany.nufitai.usecase.auth.DebugSimulateSignIn
import com.fghilmany.nufitai.usecase.auth.SignIn
import com.fghilmany.nufitai.usecase.profile.GetProfileBasics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** P-10 (techspec §6, auth-sync.md). */
sealed interface LoginState {
    data object Idle : LoginState
    data object SigningIn : LoginState
    data class Error(val message: String) : LoginState
    data class Success(val needsCompleteProfile: Boolean) : LoginState
}

class LoginViewModel(
    private val googleSignInLauncher: GoogleSignInLauncher,
    private val appleSignInLauncher: AppleSignInLauncher,
    private val signIn: SignIn,
    private val debugSimulateSignIn: DebugSimulateSignIn,
    private val getProfileBasics: GetProfileBasics,
) : ViewModel() {
    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun signInWithGoogle() = signInWith(AuthProvider.GOOGLE) { googleSignInLauncher.launch() }

    fun signInWithApple() = signInWith(AuthProvider.APPLE) { appleSignInLauncher.launch() }

    /** DEBUG ONLY -- remove once real Google/Apple SDKs are wired (techspec §3). */
    fun debugSignIn() {
        if (_state.value == LoginState.SigningIn) return
        _state.value = LoginState.SigningIn
        viewModelScope.launch {
            when (val result = debugSimulateSignIn()) {
                is AppResult.Error -> _state.value = LoginState.Error(result.failure.message)
                is AppResult.Success -> onSignedIn()
            }
        }
    }

    private fun signInWith(provider: AuthProvider, launchNative: suspend () -> AppResult<String>) {
        if (_state.value == LoginState.SigningIn) return
        _state.value = LoginState.SigningIn
        viewModelScope.launch {
            when (val tokenResult = launchNative()) {
                is AppResult.Error -> _state.value = LoginState.Error(tokenResult.failure.message)
                is AppResult.Success -> when (val signInResult = signIn(provider, tokenResult.data)) {
                    is AppResult.Error -> _state.value = LoginState.Error(signInResult.failure.message)
                    is AppResult.Success -> onSignedIn()
                }
            }
        }
    }

    private suspend fun onSignedIn() {
        val needsCompleteProfile = when (val result = getProfileBasics()) {
            is AppResult.Error -> true
            is AppResult.Success -> result.data == null
        }
        _state.value = LoginState.Success(needsCompleteProfile)
    }
}
