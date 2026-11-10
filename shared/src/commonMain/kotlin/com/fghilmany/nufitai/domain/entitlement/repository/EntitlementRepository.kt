package com.fghilmany.nufitai.domain.entitlement.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.entitlement.entity.Tier

/**
 * issue #81 -- thin read over local settings (`isLoggedIn`/`isSubscribed`).
 */
interface EntitlementRepository {
    suspend fun getCurrentTier(): AppResult<Tier>

    /** issue #83 -- the real producer of the `isLoggedIn` flag now that Auth & Sync exists (SignIn/SignOut). */
    suspend fun setLoggedIn(isLoggedIn: Boolean): AppResult<Unit>

    /** ADR-008 follow-up -- fetches this user's real tier from the `user_entitlement` Supabase
     * table and persists it locally. Called right after sign-in. Not RevenueCat -- the
     * `user_entitlement` row itself is still set manually (SQL) until real billing exists -- but
     * the app now reflects whatever that row says instead of a client-side debug toggle. */
    suspend fun refreshFromRemote(userId: String, accessToken: String): AppResult<Unit>
}
