package com.fghilmany.nufitai.usecase.auth

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.auth.repository.AuthRepository
import com.fghilmany.nufitai.domain.entitlement.repository.EntitlementRepository

/** User's choice on [SignInOutcome.LocalDataConflict]: abort -- the account that was signed into
 * wasn't the one they meant to use. Reverts to guest WITHOUT wiping local data (unlike [SignOut],
 * this user never got to actually use the account, so there's nothing of theirs to sync/lose --
 * the local data being protected here is the guest's own, not the account's). */
class CancelConflictingSignIn(
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
