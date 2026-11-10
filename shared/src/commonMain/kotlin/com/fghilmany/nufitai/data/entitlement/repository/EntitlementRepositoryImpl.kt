package com.fghilmany.nufitai.data.entitlement.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.runCatchingDatabase
import com.fghilmany.nufitai.data.entitlement.datasource.EntitlementLocalDataSource
import com.fghilmany.nufitai.data.entitlement.datasource.EntitlementRemoteDataSource
import com.fghilmany.nufitai.domain.entitlement.entity.Tier
import com.fghilmany.nufitai.domain.entitlement.repository.EntitlementRepository

class EntitlementRepositoryImpl(
    private val local: EntitlementLocalDataSource,
    private val remote: EntitlementRemoteDataSource,
) : EntitlementRepository {
    override suspend fun getCurrentTier(): AppResult<Tier> = runCatchingDatabase {
        when {
            !local.isLoggedIn() -> Tier.GUEST
            !local.isSubscribed() -> Tier.LOGGED_IN
            else -> Tier.SUBSCRIBED
        }
    }

    override suspend fun setLoggedIn(isLoggedIn: Boolean): AppResult<Unit> = runCatchingDatabase {
        local.setLoggedIn(isLoggedIn)
    }

    override suspend fun refreshFromRemote(userId: String, accessToken: String): AppResult<Unit> = runCatchingDatabase {
        // No row yet (server lazily creates one on first AI Edge Function call) -> treated as
        // not-subscribed, matching entitlement.ts's own LOGGED_IN default.
        val tier = remote.getTier(userId, accessToken)
        local.setSubscribed(tier == "SUBSCRIBED")
    }
}
