package com.fghilmany.nufitai.domain.sync.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.sync.entity.SyncStatus
import kotlinx.coroutines.flow.StateFlow

/** techspec §5/§7 (auth-sync.md) -- pushes local changes then pulls remote ones, LWW per record via `updated_at`. */
interface SyncRepository {
    val status: StateFlow<SyncStatus>
    suspend fun syncNow(): AppResult<Unit>

    /** Wipes every local account-data table + resets the sync cursor -- used on sign-out
     * (privacy: the next guest on this device shouldn't see the previous user's data) and when
     * sign-in conflicts with pre-existing local guest data. */
    suspend fun clearAllLocalData(): AppResult<Unit>

    /** True if this device already has local guest activity (used by [com.fghilmany.nufitai.usecase.auth.SignIn]
     * to detect a conflict when signing into an EXISTING account -- that guest data doesn't belong
     * to the account being signed into and must not be silently pushed/merged into it). */
    suspend fun hasLocalUserData(): AppResult<Boolean>

    /** True if the signed-into account has ANY existing cloud history. Used by
     * [com.fghilmany.nufitai.usecase.auth.SignIn] to tell an account with real data to protect
     * apart from one that's never actually been used (brand-new, or existing-but-empty) -- only
     * the former should force the discard/cancel choice. */
    suspend fun hasRemoteUserData(userId: String, accessToken: String): AppResult<Boolean>
}
