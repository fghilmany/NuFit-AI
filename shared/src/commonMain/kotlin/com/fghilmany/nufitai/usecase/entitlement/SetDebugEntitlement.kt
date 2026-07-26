package com.fghilmany.nufitai.usecase.entitlement

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.entitlement.repository.EntitlementRepository

/** DEBUG ONLY -- remove once real auth/billing set entitlement flags for real. */
class SetDebugEntitlement(private val repository: EntitlementRepository) {
    suspend operator fun invoke(isLoggedIn: Boolean, isSubscribed: Boolean): AppResult<Unit> =
        repository.setDebugEntitlement(isLoggedIn, isSubscribed)
}
