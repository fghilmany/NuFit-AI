package com.fghilmany.nufitai.usecase.auth

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.auth.entity.AuthProvider
import com.fghilmany.nufitai.domain.auth.entity.AuthSession
import com.fghilmany.nufitai.domain.auth.repository.AuthRepository
import com.fghilmany.nufitai.domain.entitlement.repository.EntitlementRepository
import com.fghilmany.nufitai.domain.sync.repository.SyncRepository

sealed interface SignInOutcome {
    data class SignedIn(val session: AuthSession) : SignInOutcome

    /** This is an EXISTING account (not a first-ever sign-in) and this device already has local
     * guest activity -- that guest data doesn't belong to the account just signed into, so it
     * must not be silently pushed/merged into it. The caller must ask the user to pick
     * [DiscardLocalDataAndSync] (adopt the cloud account, local guest data is lost) or
     * [CancelConflictingSignIn] (abort, go back to guest with local data untouched) before any
     * sync happens. */
    data class LocalDataConflict(val session: AuthSession) : SignInOutcome
}

/**
 * techspec §6 (auth-sync.md) -- signs in, flips the real `isLoggedIn` flag, refreshes the real
 * Subscribe status from Supabase (ADR-008 follow-up -- replaces the old client-side debug
 * toggle), then triggers the first sync pass (push local guest data, pull any remote data -- LWW
 * per record) -- UNLESS this is an existing account and local guest data already exists (see
 * [SignInOutcome.LocalDataConflict]), in which case sync is deliberately skipped pending the
 * user's choice. A sync/entitlement-fetch failure here (e.g. no network) does not undo the
 * sign-in -- the session is real even if a network call isn't reachable yet.
 */
class SignIn(
    private val authRepository: AuthRepository,
    private val entitlementRepository: EntitlementRepository,
    private val syncRepository: SyncRepository,
) {
    suspend operator fun invoke(
        provider: AuthProvider,
        idToken: String,
        displayName: String? = null,
        profilePictureUrl: String? = null,
    ): AppResult<SignInOutcome> {
        val session = when (val result = authRepository.signIn(provider, idToken, displayName, profilePictureUrl)) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return result
        }
        entitlementRepository.setLoggedIn(true)
        entitlementRepository.refreshFromRemote(session.userId, session.accessToken)

        if (!session.isNewUser) {
            val hasLocalData = when (val check = syncRepository.hasLocalUserData()) {
                is AppResult.Success -> check.data
                // Can't tell -- fail open to a normal sync rather than blocking a real sign-in on
                // an unrelated local-read error.
                is AppResult.Error -> false
            }
            if (hasLocalData) {
                return AppResult.Success(SignInOutcome.LocalDataConflict(session))
            }
        }

        syncRepository.syncNow()
        return AppResult.Success(SignInOutcome.SignedIn(session))
    }
}
