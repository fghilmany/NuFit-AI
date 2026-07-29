package com.fghilmany.nufitai.usecase.auth

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.core.util.generateId
import com.fghilmany.nufitai.domain.auth.entity.AuthSession
import com.fghilmany.nufitai.domain.auth.repository.AuthRepository
import com.fghilmany.nufitai.domain.entitlement.repository.EntitlementRepository
import com.fghilmany.nufitai.domain.sync.repository.SyncRepository
import kotlin.time.Duration.Companion.hours

/**
 * DEBUG ONLY -- remove once real Google/Apple SDKs are wired (techspec §3, auth-sync.md).
 * Bypasses the stubbed native sign-in launcher entirely with a local fake session, so the rest
 * of the flow (entitlement gating, navigation, Account screen, sync engine) is testable before
 * a real Supabase project exists. Mirrors the existing `SetDebugEntitlement`/
 * `debugSimulateSubscriber()` pattern in AiCustomPlanGateViewModel.
 */
class DebugSimulateSignIn(
    private val authRepository: AuthRepository,
    private val entitlementRepository: EntitlementRepository,
    private val syncRepository: SyncRepository,
) {
    suspend operator fun invoke(): AppResult<AuthSession> {
        val session = AuthSession(
            userId = "debug-${generateId()}",
            accessToken = "debug-access-token",
            refreshToken = "debug-refresh-token",
            expiresAt = currentInstant() + 1.hours,
            displayName = "Debug User",
        )
        authRepository.debugSetSession(session)
        entitlementRepository.setLoggedIn(true)
        syncRepository.syncNow()
        return AppResult.Success(session)
    }
}
