package com.fghilmany.nufitai.domain.entitlement.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.entitlement.entity.Tier

/**
 * issue #81 -- thin read over local settings (`isLoggedIn`/`isSubscribed`). Until real
 * auth/billing ship, nothing in this app sets those flags; see techspec §9 item 8.
 */
interface EntitlementRepository {
    suspend fun getCurrentTier(): AppResult<Tier>

    /** issue #83 -- the real producer of the `isLoggedIn` flag now that Auth & Sync exists (SignIn/SignOut). */
    suspend fun setLoggedIn(isLoggedIn: Boolean): AppResult<Unit>

    /** DEBUG ONLY -- remove once real billing sets `isSubscribed` for real (RevenueCat, see class doc). */
    suspend fun setDebugEntitlement(isLoggedIn: Boolean, isSubscribed: Boolean): AppResult<Unit>
}
