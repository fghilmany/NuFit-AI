package com.fghilmany.nufitai.domain.auth.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.auth.entity.AuthProvider
import com.fghilmany.nufitai.domain.auth.entity.AuthSession

/** techspec §5/§7 (auth-sync.md, issue #83) -- Supabase Auth REST, no supabase-kt SDK (ADR-005). */
interface AuthRepository {
    suspend fun signIn(
        provider: AuthProvider,
        idToken: String,
        displayName: String? = null,
        profilePictureUrl: String? = null,
    ): AppResult<AuthSession>

    suspend fun getCurrentSession(): AuthSession?

    /** Refreshes only if the stored session is expired/near-expiry; returns null if there's no session at all. */
    suspend fun refreshSessionIfNeeded(): AppResult<AuthSession?>

    /** Clears the local session only -- techspec §5: local data stays cached, never wiped. */
    suspend fun signOut(): AppResult<Unit>

    /** DEBUG ONLY -- remove once real Google/Apple SDKs are wired (see DebugSimulateSignIn). */
    suspend fun debugSetSession(session: AuthSession)
}
