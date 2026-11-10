package com.fghilmany.nufitai.data.entitlement.datasource

import com.russhwolf.settings.Settings

private const val KEY_IS_LOGGED_IN = "entitlement_is_logged_in"
private const val KEY_IS_SUBSCRIBED = "entitlement_is_subscribed"

/**
 * issue #81 -- local cache of entitlement flags. `isLoggedIn` is set for real by SignIn/SignOut
 * (issue #83); `isSubscribed` is set for real by [setSubscribed], sourced from the
 * `user_entitlement` Supabase table (ADR-008 follow-up) -- still not RevenueCat, but no longer a
 * client-side-only debug toggle either.
 */
class EntitlementLocalDataSource(private val settings: Settings) {
    fun isLoggedIn(): Boolean = settings.getBoolean(KEY_IS_LOGGED_IN, false)
    fun isSubscribed(): Boolean = settings.getBoolean(KEY_IS_SUBSCRIBED, false)

    /** issue #83 -- real producer of this flag (SignIn/SignOut usecases). */
    fun setLoggedIn(isLoggedIn: Boolean) {
        settings.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
    }

    fun setSubscribed(isSubscribed: Boolean) {
        settings.putBoolean(KEY_IS_SUBSCRIBED, isSubscribed)
    }
}
