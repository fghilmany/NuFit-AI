package com.fghilmany.nufitai.presentation.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.auth.AppleSignInLauncher
import com.fghilmany.nufitai.core.auth.GoogleSignInLauncher
import com.fghilmany.nufitai.core.auth.NativeSignInResult
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.auth.entity.AuthProvider
import com.fghilmany.nufitai.usecase.auth.CancelConflictingSignIn
import com.fghilmany.nufitai.usecase.auth.DebugSimulateSignIn
import com.fghilmany.nufitai.usecase.auth.DiscardLocalDataAndSync
import com.fghilmany.nufitai.usecase.auth.SignIn
import com.fghilmany.nufitai.usecase.auth.SignInOutcome
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

    /** This is an existing account and this device already has local guest data -- [SignIn]
     * deliberately skipped syncing pending the user's choice (see [SignInOutcome.LocalDataConflict]). */
    data object LocalDataConflict : LoginState
    data object ResolvingConflict : LoginState
}

class LoginViewModel(
    private val googleSignInLauncher: GoogleSignInLauncher,
    private val appleSignInLauncher: AppleSignInLauncher,
    private val signIn: SignIn,
    private val debugSimulateSignIn: DebugSimulateSignIn,
    private val getProfileBasics: GetProfileBasics,
    private val discardLocalDataAndSync: DiscardLocalDataAndSync,
    private val cancelConflictingSignIn: CancelConflictingSignIn,
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

    /** User chose to adopt the cloud account on a [LoginState.LocalDataConflict] -- wipes this
     * device's local guest data (it isn't the account's) and pulls the account's real data down. */
    fun confirmDiscardLocalData() {
        if (_state.value != LoginState.LocalDataConflict) return
        _state.value = LoginState.ResolvingConflict
        viewModelScope.launch {
            when (val result = discardLocalDataAndSync()) {
                is AppResult.Error -> _state.value = LoginState.Error(result.failure.message)
                is AppResult.Success -> onSignedIn()
            }
        }
    }

    /** User chose to abort on a [LoginState.LocalDataConflict] -- reverts to guest, local guest
     * data left untouched (it was never the signed-into account's to begin with). */
    fun cancelSignInConflict() {
        if (_state.value != LoginState.LocalDataConflict) return
        _state.value = LoginState.ResolvingConflict
        viewModelScope.launch {
            cancelConflictingSignIn()
            _state.value = LoginState.Idle
        }
    }

    private fun signInWith(provider: AuthProvider, launchNative: suspend () -> AppResult<NativeSignInResult>) {
        if (_state.value == LoginState.SigningIn) return
        _state.value = LoginState.SigningIn
        viewModelScope.launch {
            when (val nativeResult = launchNative()) {
                is AppResult.Error -> _state.value = LoginState.Error(nativeResult.failure.message)
                is AppResult.Success -> when (
                    val signInResult = signIn(
                        provider,
                        nativeResult.data.idToken,
                        nativeResult.data.displayName,
                        nativeResult.data.profilePictureUrl,
                    )
                ) {
                    is AppResult.Error -> _state.value = LoginState.Error(signInResult.failure.message)
                    is AppResult.Success -> when (signInResult.data) {
                        is SignInOutcome.SignedIn -> onSignedIn()
                        is SignInOutcome.LocalDataConflict -> _state.value = LoginState.LocalDataConflict
                    }
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
