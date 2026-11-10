package com.fghilmany.nufitai.fake

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.entitlement.entity.Tier
import com.fghilmany.nufitai.domain.entitlement.repository.EntitlementRepository

class FakeEntitlementRepository(var tier: Tier = Tier.GUEST) : EntitlementRepository {
    override suspend fun getCurrentTier(): AppResult<Tier> = AppResult.Success(tier)

    override suspend fun setLoggedIn(isLoggedIn: Boolean): AppResult<Unit> {
        tier = when {
            !isLoggedIn -> Tier.GUEST
            tier == Tier.SUBSCRIBED -> Tier.SUBSCRIBED
            else -> Tier.LOGGED_IN
        }
        return AppResult.Success(Unit)
    }

    var refreshFromRemoteCallCount: Int = 0
        private set

    /** No-op besides tracking the call -- tests set [tier] directly rather than faking a network response. */
    override suspend fun refreshFromRemote(userId: String, accessToken: String): AppResult<Unit> {
        refreshFromRemoteCallCount++
        return AppResult.Success(Unit)
    }
}
