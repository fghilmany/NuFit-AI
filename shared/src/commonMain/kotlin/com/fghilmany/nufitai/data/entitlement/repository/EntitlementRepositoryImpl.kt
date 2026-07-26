package com.fghilmany.nufitai.data.entitlement.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.runCatchingDatabase
import com.fghilmany.nufitai.data.entitlement.datasource.EntitlementLocalDataSource
import com.fghilmany.nufitai.domain.entitlement.entity.Tier
import com.fghilmany.nufitai.domain.entitlement.repository.EntitlementRepository

class EntitlementRepositoryImpl(
    private val local: EntitlementLocalDataSource,
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

    override suspend fun setDebugEntitlement(isLoggedIn: Boolean, isSubscribed: Boolean): AppResult<Unit> = runCatchingDatabase {
        local.setDebugEntitlement(isLoggedIn, isSubscribed)
    }
}
