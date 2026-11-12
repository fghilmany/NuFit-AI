package com.fghilmany.nufitai.usecase.auth

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.auth.repository.AuthRepository
import com.fghilmany.nufitai.domain.entitlement.repository.EntitlementRepository
import com.fghilmany.nufitai.domain.sync.repository.SyncRepository

/**
 * Syncs first (dev decision, 2026-11-12) so nothing unpushed is lost, then clears the session
 * AND wipes local account data -- a different device/guest using this app afterward must not see
 * the departing user's plans/chat/assessments. If the sync fails (e.g. offline), nothing is
 * cleared and the failure is returned so the caller can ask the user to confirm signing out
 * anyway; that confirmation re-invokes this with [force] = true, which skips the sync attempt
 * and clears unconditionally.
 */
class SignOut(
    private val authRepository: AuthRepository,
    private val entitlementRepository: EntitlementRepository,
    private val syncRepository: SyncRepository,
) {
    suspend operator fun invoke(force: Boolean = false): AppResult<Unit> {
        if (!force) {
            val syncResult = syncRepository.syncNow()
            if (syncResult is AppResult.Error) return syncResult
        }

        val result = authRepository.signOut()
        if (result is AppResult.Success) {
            entitlementRepository.setLoggedIn(false)
            syncRepository.clearAllLocalData()
        }
        return result
    }
}
