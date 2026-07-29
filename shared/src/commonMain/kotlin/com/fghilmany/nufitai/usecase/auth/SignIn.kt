package com.fghilmany.nufitai.usecase.auth

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.auth.entity.AuthProvider
import com.fghilmany.nufitai.domain.auth.entity.AuthSession
import com.fghilmany.nufitai.domain.auth.repository.AuthRepository
import com.fghilmany.nufitai.domain.entitlement.repository.EntitlementRepository
import com.fghilmany.nufitai.domain.sync.repository.SyncRepository

/**
 * techspec §6 (auth-sync.md) -- signs in, flips the real `isLoggedIn` flag, then triggers the
 * first sync pass (push local guest data, pull any remote data -- LWW per record). A sync
 * failure here (e.g. placeholder SUPABASE_URL, no project configured yet) does not undo the
 * sign-in -- the session is real even if the network call isn't reachable yet.
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
    ): AppResult<AuthSession> {
        val result = authRepository.signIn(provider, idToken, displayName, profilePictureUrl)
        if (result is AppResult.Success) {
            entitlementRepository.setLoggedIn(true)
            syncRepository.syncNow()
        }
        return result
    }
}
