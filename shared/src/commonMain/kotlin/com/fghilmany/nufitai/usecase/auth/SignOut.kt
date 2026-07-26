package com.fghilmany.nufitai.usecase.auth

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.auth.repository.AuthRepository
import com.fghilmany.nufitai.domain.entitlement.repository.EntitlementRepository

/** techspec §5/§9 item 9 -- clears the session/tier only; local data stays cached (local-first). */
class SignOut(
    private val authRepository: AuthRepository,
    private val entitlementRepository: EntitlementRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> {
        val result = authRepository.signOut()
        if (result is AppResult.Success) {
            entitlementRepository.setLoggedIn(false)
        }
        return result
    }
}
